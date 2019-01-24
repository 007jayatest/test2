package com.addteq.confluence.plugin.excellentable.rest;

import com.addteq.confluence.plugin.excellentable.ao.ETShare;
import com.addteq.confluence.plugin.excellentable.ao.ETShareDetails;
import com.addteq.confluence.plugin.excellentable.ao.ExcellentableDB;
import com.addteq.confluence.plugin.excellentable.model.ShareFilterModel;
import com.addteq.confluence.plugin.excellentable.permission.ExcPermissionManager;
import com.addteq.service.excellentable.exc_io.utils.Gzip;
import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.confluence.core.DataSourceFactory;
import com.atlassian.confluence.mail.template.PreRenderedMailNotificationQueueItem;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.core.task.MultiQueueTaskManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.user.EntityException;
import com.atlassian.user.Group;
import com.atlassian.user.GroupManager;
import com.atlassian.user.User;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.activation.DataSource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static com.opensymphony.xwork.Action.SUCCESS;

@Path("/share")
public class ShareFilterRest {

    private final ActiveObjects ao;
    private final UserAccessor userAccessor;
    private final PageManager pageManager;
    private final GroupManager groupManager;
    private final SettingsManager settingsManager;
    private final DataSourceFactory dataSourceFactory;
    private final MultiQueueTaskManager taskManager;
    private final ExcPermissionManager excPermissionManager;
    public static final String MAIL = "mail";
    private boolean filterApplied;
    static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ShareFilterRest.class);
    private static final int singleSheetFilterVersion = 2;
    private static final int multiSheetFilterVersion = 3;
    /*  Defined type of the user with whom filter is shared like below:
        1 for User
        2 for Group
        3 for email
    */
    private enum UserType {

        USER(1), GROUP(2), ANONYMOUS(3);
        private final int userCode;

        UserType(int userCode) {
            this.userCode = userCode;
        }

        public int getUserType() {
            return this.userCode;
        }
    }
    
    @Autowired
    public ShareFilterRest(
            @ComponentImport ActiveObjects ao, 
            @ComponentImport UserAccessor userAccessor, 
            @ComponentImport PageManager pageManager, 
            @ComponentImport GroupManager groupManager,
            @ComponentImport SettingsManager settingsManager, 
            @ComponentImport DataSourceFactory dataSourceFactory, 
            @ComponentImport MultiQueueTaskManager taskManager,
            ExcPermissionManager excPermissionManager) {
        
        this.ao = ao;
        this.userAccessor = userAccessor;
        this.pageManager = pageManager;
        this.groupManager = groupManager;
        this.settingsManager = settingsManager;
        this.dataSourceFactory = dataSourceFactory;
        this.taskManager = taskManager;
        this.excPermissionManager = excPermissionManager;
    }
    
    /**
     * Share Excellentable along with applied filter with different users
     *
     * @param shareFilterModel
     * @return Response
     * @throws com.atlassian.user.EntityException
     */
    @POST
    @AnonymousAllowed
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/filter")
    public Response shareFilter(final ShareFilterModel shareFilterModel) throws EntityException {
        ExcellentableDB[] excellentableDbInsert = ao.find(ExcellentableDB.class, "ID = ?", shareFilterModel.getID());
        JsonObject checkPermission = excPermissionManager.hasPermissionOnExcellentable(excellentableDbInsert, "VIEW");
        if (checkPermission.get("status").getAsBoolean()) {
            ConfluenceUser user = AuthenticatedUserThreadLocal.get();
            UserKey loggedInUserName;
            if (user != null) { //If user is not anonymous
                loggedInUserName = user.getKey();
            } else {
                loggedInUserName = new UserKey("anonymous");
            }
            int excId = shareFilterModel.getID();
            String note = shareFilterModel.getNote();

            Set<String> users = shareFilterModel.getUsers();
            Set<String> groups = shareFilterModel.getGroups();
            Set<String> emails = shareFilterModel.getEmails();
            Set<User> uniqueUsers = new HashSet<>();

            ETShare shared = addToShare(excId, shareFilterModel.getFilterString(), loggedInUserName.toString());

            for (String userKey : users) { //List of users with whom filter is shared.
                User receiver = userAccessor.getUserByKey(new UserKey(userKey));
                uniqueUsers.add(receiver);
                addToShareDetails(shared, note, userKey, UserType.USER.getUserType());
            }
            for (String groupName : groups) {  //List of groups with whom filter is shared.
                Group group = groupManager.getGroup(groupName);
                Set<String> groupMembers = new HashSet<>(groupManager.getMemberNames(group).getCurrentPage());
                for (String groupMember : groupMembers) {
                    User receiver = userAccessor.getUserByName(groupMember);
                    uniqueUsers.add(receiver);
                }
                addToShareDetails(shared, note, groupName, UserType.GROUP.getUserType());
            }
            Iterator<String> emailIterator = emails.iterator(); 
            while (emailIterator.hasNext()) { //List of email Ids with whom filter is shared.
                String email = emailIterator.next();
                AnonymousUser receiver = new AnonymousUser(email);
                uniqueUsers.add(receiver);
                addToShareDetails(shared, note, email, UserType.ANONYMOUS.getUserType());
            }

            sendEmail(shareFilterModel, user, uniqueUsers, shared.getSecretKey());
            return Response.ok(SUCCESS).build();
        } else {
            int statusCode = checkPermission.get("status-code").getAsInt();
                String statusMsg = checkPermission.get("message").getAsString();
            return Response.status(Response.Status.fromStatusCode(statusCode)).entity(statusMsg).build();
        }
    }
    
    /**
     * retrieve metaData of shared Excellentable from DB
     *
     * @param excId
     * @param secretKey
     * @return Response
     */
    @GET
    @AnonymousAllowed
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/filter")
    public Response retrieveSharedTable(@QueryParam("excId") Integer excId,@QueryParam("secretKey") String secretKey) {
        filterApplied = false;
        ShareFilterModel shareFilterModel = new ShareFilterModel();
        try {
            ETShare[] shareFilterDb = ao.find(ETShare.class, "SECRET_KEY = ? AND EXCELLENTABLE_ID = ?", secretKey,excId);
            ExcellentableDB excellentableDb;
            shareFilterModel.setID(excId);
            if(shareFilterDb.length ==0){ //If the shared table entry does not exist
                excellentableDb = ao.find(ExcellentableDB.class, "ID = ? ", excId)[0];
                shareFilterModel.setSecretKey(""); //If secret key is invalid then send blank secretKey in response.
            }else{
                excellentableDb  = shareFilterDb[0].getExcellentable();
                shareFilterModel.setSecretKey(secretKey);
            }    
            ExcellentableDB[] excDB = new ExcellentableDB[]{excellentableDb};
            JsonObject checkPermission = excPermissionManager.hasPermissionOnExcellentable(excDB, "VIEW");
            if (checkPermission.get("status").getAsBoolean()) {
                String metaData = excellentableDb.getMetaData(); //get whole table data of shared Excellentable.
                String compressedMetaData = Gzip.uncompressString(metaData);
                JsonParser jsonParser = new JsonParser();
                JsonObject metadataJSON = jsonParser.parse(compressedMetaData).getAsJsonObject();

                //If the shared table entry exists then only add filterString & globalSearchString in the response else return the whole metaData.
                if(shareFilterDb.length != 0){ 
                    String filterString = shareFilterDb[0].getFilterString(); // get filterJSON of shared Excellentable.
                    JsonObject filterJSON = jsonParser.parse(filterString).getAsJsonObject();

                    JsonObject sheetsJSON = metadataJSON.getAsJsonObject("sheets");
                    String firstSheetName = sheetsJSON.keySet().iterator().next();
                    JsonObject activeSheetObj = metadataJSON.getAsJsonObject("sheets").getAsJsonObject(firstSheetName);
                    
                    if (filterJSON.has("globalSearch")) { //If global search i.e LiveSearch is shared.
                        String globalSearchString = filterJSON.get("globalSearch").getAsString();
                        shareFilterModel.setGlobalSearchString(globalSearchString);
                        filterApplied = true;
                    }
                                      
                    if (filterJSON.has("filterVersion")) {
                        //If the filterVersion is 3 i.e. filter is shared after adding support for MultiSheet share table Refer:EXC-2513
                        if (filterJSON.get("filterVersion").getAsInt() == multiSheetFilterVersion) {
                            
                            /* Ref : EXC-2613
                            *  set active sheet based on sheetIndex which is shared.
                            */
                            if (filterJSON.has("activeSheetIndex")){
                                int activeSheetIndex = filterJSON.get("activeSheetIndex").getAsInt();
                                metadataJSON.addProperty("activeSheetIndex",activeSheetIndex);
                            }
                                
                            /*Iterate over all the sheets & retrieve shared filters*/
                            Iterator<?> keys = sheetsJSON.keySet().iterator();
                            while (keys.hasNext()) {
                                String key = (String) keys.next();
                                if (sheetsJSON.has(key) && filterJSON.has(key)) {
                                    retrieveSharedFilters((JsonObject) filterJSON.get(key), (JsonObject) sheetsJSON.get(key));
                                }
                            }
                        }
                        //If the filterVersion is 2 i.e. filter is shared after adding support for share table design filter Refer:EXC-1301
                        else if(filterJSON.get("filterVersion").getAsInt() == singleSheetFilterVersion){
                            retrieveSharedFilters(filterJSON,activeSheetObj);
                        }
                        shareFilterModel.setFilterVersion(filterJSON.get("filterVersion").getAsInt());

                    } else {
                        //Add an entry of filterJSON in the whole tableMetaData that will apply filter on table.
                        activeSheetObj.add("rowFilter", filterJSON);
                    }
                }
                
                shareFilterModel.setContentEntityId(excellentableDb.getContentEntityId());
                shareFilterModel.setMetaData(metadataJSON.toString());
                shareFilterModel.setFilterApplied(filterApplied);
                return Response.ok(shareFilterModel).build();
            } else {
                int statusCode = checkPermission.get("status-code").getAsInt();
                String statusMsg = checkPermission.get("message").getAsString();
                return Response.status(Response.Status.fromStatusCode(statusCode)).entity(statusMsg).build();
            }
        } catch (Exception e) {
            LOGGER.error("Exception: "+ e);
            return Response.status(Response.Status.fromStatusCode(500)).entity("Uable to retrieve shared table").build();
        }
    }

    /* Used to create User object in case of anonymous User */
    private class AnonymousUser implements User {

        private final String email;

        AnonymousUser(String email) {
            this.email = email;
        }

        @Override
        public String getName() {
            return this.email;
        }

        @Override
        public String getFullName() {
            return this.email;
        }

        @Override
        public String getEmail() {
            return this.email;
        }
    }

    private String getMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            String hashtext = number.toString(16);
            // Now we need to zero pad it if you actually want the full 32 chars.
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Exception occured", e);
            return null;
        }
    }

    /* Make an entry of the FilterJSON in the DB with below fields:
        1. ExcellentableID
        2. FilterString (i.e Shared filter in JSON format)
        3. Reporter (who shared the filter)
        4. SecretKey (MD5 of DB generated ID which will be used in future while retrieving.)
    */
    private ETShare addToShare(int excId, String filterString, String reporter) {

        ETShare[] shareDetails = ao.find(ETShare.class, "EXCELLENTABLE_ID = ? AND FILTER_STRING = ? AND REPORTER = ? ", excId, filterString, reporter);
        if (shareDetails.length == 0) { //If the filterJSON entry does not present in DB.

            ExcellentableDB[] excellentableDetails = ao.find(ExcellentableDB.class, " ID = ? ", excId);
            ETShare share = ao.create(ETShare.class);

            String encodedId = getMD5(Integer.toString(share.getID())); //Generate MD5 of the ID that would used be used while sharing/retrieving filter.

            share.setSecretKey(encodedId);
            share.setExcellentable(excellentableDetails[0]);
            share.setFilterString(filterString);
            share.setReporter(reporter);
            share.save();

            return share;
        }
        return shareDetails[0];
    }

    /* Add watchers details entry in the DB with below fields:
        1. Which filter is shared.
        2. Note.
        3. Watcher(i.e UserKey(User)/groupName(Group)/email(Anonymous))
        4. WatcherType ( 1:User  2:Group  3:Email)
        5. SharedDate (when filter is shared.)
    */
    private ETShareDetails addToShareDetails(ETShare share, String note, String watcher, int watcher_type) {
        ETShareDetails[] watchersList = ao.find(ETShareDetails.class, "ETSHARE_ID = ? AND NOTE = ? AND WATCHER = ?", share, note, watcher);
        if (watchersList.length == 0) {
            ETShareDetails shareDetails = ao.create(ETShareDetails.class);
            shareDetails.setETShare(share);
            shareDetails.setNote(note);
            shareDetails.setWatcher(watcher);
            shareDetails.setWatcherType(watcher_type);
            shareDetails.setSharedDate(new Date());

            shareDetails.save();

            return shareDetails;
        }
        return watchersList[0];
    }

    /* Send an email to all the users with whom filter is shared. */
    private void sendEmail(ShareFilterModel shareFilterModel, User user, Set uniqueUsers, String secretKey) {
        try {
            Map context = MacroUtils.defaultVelocityContext();
            String baseUrl = settingsManager.getGlobalSettings().getBaseUrl();

            Page page = pageManager.getPage(shareFilterModel.getContentEntityId());
            String userFullName = "Anonymous";
            if (user != null) {   //If user is not anonymous
                userFullName = user.getFullName();

            }
            DataSource userAvatar = dataSourceFactory.getAvatar(user);
            DataSource pageAvatar = dataSourceFactory.getURLResource(new java.net.URL(baseUrl + "/download/resources/Addteq.Excellentable:share-filter-resources/pageIcon.png"), "pageIcon");
            List<DataSource> attachments = new ArrayList<DataSource>();

            String note = shareFilterModel.getNote();
            if (note != null && !note.trim().equals("")) {
                context.put("note", note);
            }
            context.put("userFullName", userFullName);
            context.put("pageTitle", page.getDisplayTitle());
            context.put("pageLink", baseUrl + "/pages/viewpage.action?pageId="+ page.getIdAsString() + "&eFilter=" + secretKey);
            context.put("avatarCid", (Object) userAvatar.getName());
            context.put("pageIconCid", (Object) pageAvatar.getName());

            String mailSubject = userFullName + " shared \"Excellentable\" with you";
            String templateLocation = "/template/excellentable/shareFilter/";
            String templateName = "shareFilter.vm";

            attachments.add(userAvatar);
            attachments.add(pageAvatar);
            Iterator<User> usersIterator = uniqueUsers.iterator();
            while (usersIterator.hasNext()) {
                User receiver = usersIterator.next();

                PreRenderedMailNotificationQueueItem.Builder builder = PreRenderedMailNotificationQueueItem.with((User) receiver, (String) templateName, mailSubject)
                        .andSender(user)
                        .andTemplateLocation(templateLocation)
                        .andContext(context)
                        .andRelatedBodyParts(attachments);

                taskManager.addTask(MAIL, builder.render());

                LOGGER.info("Mail Sent Successfully" + receiver.getEmail());

            }

        } catch (Exception e) {
            LOGGER.error("Error in sending e-mail", e);
        }
    }
    
    private void retrieveSharedFilters(JsonObject filterJSON, JsonObject activeSheetObj) {
        if (filterJSON.has("globalFilter")) { //If global filter is shared
            JsonObject globalFilter = (JsonObject) filterJSON.get("globalFilter");
            int rowCount =0 , colCount = 0;
            if(globalFilter.has("rowCount")) rowCount = globalFilter.get("rowCount").getAsInt();
            if(globalFilter.has("colCount")) colCount = globalFilter.get("colCount").getAsInt();
            if(rowCount != 0 && colCount !=0){
                activeSheetObj.add("rowFilter", filterJSON.get("globalFilter"));
                filterApplied= true;
            }
        }

        if (filterJSON.has("tableFilter")) { //If table design is shared
            JsonObject tableFilters = (JsonObject) filterJSON.get("tableFilter");

            JsonArray sheetTableArray = (JsonArray) activeSheetObj.get("tables");
            for (int i = 0; i < sheetTableArray.size(); i++) {
                JsonObject currTable = (JsonObject) sheetTableArray.get(i);
                String tableName = currTable.get("name").getAsString();
                
                if (tableFilters.has(tableName)) {
                    JsonObject tableObject = (JsonObject) tableFilters.get(tableName);
                    currTable.add("rowFilter", tableObject);
                    boolean hasFilterItemMap = tableObject.has("filterItemMap"); 
                    if(hasFilterItemMap){ //Filter is applied on table formatting
                        filterApplied = true;
                    }
                }
            }
        }
        
        if (filterJSON.has("globalSearch")) { //If Global Search is shared
            String globalSearch = filterJSON.get("globalSearch").getAsString();
            JsonObject searchString = new JsonObject();
            searchString.addProperty("searchString", globalSearch);
            activeSheetObj.add("tag", searchString);
            filterApplied = true;
        }
        
    }
}
