package com.addteq.confluence.plugin.excellentable.rest;

import com.addteq.confluence.plugin.excellentable.ao.EditHistoryDB;
import com.addteq.confluence.plugin.excellentable.ao.ExcellentableDB;
import com.addteq.confluence.plugin.excellentable.dao.ExcellentableDataService;
import com.addteq.confluence.plugin.excellentable.history.EditHistoryDAO;
import com.addteq.confluence.plugin.excellentable.model.ExcellentableModel;
import com.addteq.confluence.plugin.excellentable.model.Links;
import com.addteq.confluence.plugin.excellentable.multiedit.dao.LiveEditRegisterService;
import com.addteq.confluence.plugin.excellentable.multiedit.model.InitMultieditModel;
import com.addteq.confluence.plugin.excellentable.multiedit.model.MultieditConnectionInfo;
import com.addteq.confluence.plugin.excellentable.multiedit.service.LambdaRestClient;
import com.addteq.confluence.plugin.excellentable.permission.ExcPermissionManager;
import com.addteq.service.excellentable.exc_io.importfile.impl.ImportHtml;
import com.addteq.service.excellentable.exc_io.parser.JsonHtmlParser;
import com.addteq.service.excellentable.exc_io.utils.DomUtils;
import com.addteq.service.excellentable.exc_io.utils.ETDateUtils;
import com.addteq.service.excellentable.exc_io.utils.Gzip;
import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.confluence.diff.Differ;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.upm.api.license.PluginLicenseManager;
import com.atlassian.user.User;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.java.ao.Query;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author truptikanase **
 */
@Path("/content")
@Component
public class ExcellentableRest {

    private final ActiveObjects ao;
    private final UserAccessor userAccessor;
    private final ExcPermissionManager excPermissionManager;
    private final String ANONYMOUS = "Anonymous";
    private final EditHistoryDAO editHistoryDao;
    private final I18nResolver i18nResolver;
    private static final Logger LOGGER = LoggerFactory.getLogger(ExcellentableRest.class);
    private final String BASE_URL;
    private final Differ differ;
    private final PluginAccessor pluginAccessor;
    private final PluginLicenseManager licenseManager;
    private final LambdaRestClient lambdaRestClient;
    private final PageManager pageManager;
    private final ExcellentableDataService excellentableDataService;
    private final LiveEditRegisterService liveEditRegisterService;
    @Autowired
    public ExcellentableRest(@ComponentImport ActiveObjects ao, @ComponentImport UserAccessor userAccessor,
                             @ComponentImport I18nResolver i18nResolver, @ComponentImport Differ diff,
                             @ComponentImport SettingsManager settingsManager, @ComponentImport PluginAccessor pluginAccessor,
                             @ComponentImport PluginLicenseManager licenseManager, ExcPermissionManager excPermissionManager,
                             EditHistoryDAO editHistoryDao, LambdaRestClient lambdaRestClient,
                             @ComponentImport PageManager pageManager, ExcellentableDataService excellentableDataService,
                             LiveEditRegisterService liveEditRegisterService) {

        this.ao = ao;
        this.userAccessor = userAccessor;
        this.excPermissionManager = excPermissionManager;
        this.editHistoryDao = checkNotNull(editHistoryDao);
        this.i18nResolver = i18nResolver;
        this.BASE_URL = settingsManager.getGlobalSettings().getBaseUrl();
        this.differ = diff;
        this.pluginAccessor = pluginAccessor;
        this.licenseManager = licenseManager;
        this.lambdaRestClient = lambdaRestClient;
        this.pageManager = pageManager;
        this.excellentableDataService = excellentableDataService;
        this.liveEditRegisterService = checkNotNull(liveEditRegisterService);
    }

    /**
     * Store Excellentable into Active Objects
     *
     * @param excellentableModel
     * @return Response
     */
    @POST
    @AnonymousAllowed
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/table")
    public Response storeTableForUser(final ExcellentableModel excellentableModel) throws UnirestException {
        Long contentEntityId = excellentableModel.getContentEntityId();
        String spaceKey = excellentableModel.getSpaceKey();
        if (contentEntityId.equals(0L) && spaceKey == null) {
            String msg = "Content Entity Id & Space Key is mandatory.";
            return Response.status(Response.Status.fromStatusCode(400)).entity(msg).build();
        }
        JsonObject checkPermission = excPermissionManager.hasPermissionOnContentEntity(
                excellentableModel.getContentEntityId(), excellentableModel.getContentType(),
                excellentableModel.getSpaceKey(), "EDIT");
        if (checkPermission.get("status").getAsBoolean()) {
            ExcellentableModel excellentableModelNew = ao
                    .executeInTransaction(new TransactionCallback<ExcellentableModel>() // (1)
                    {
                        @Override
                        public ExcellentableModel doInTransaction() {
                            ExcellentableDB excellentableDbInsert = ao.create(ExcellentableDB.class); // (2)
                            excellentableDbInsert.setMetaData(excellentableModel.getMetaData());
                            excellentableDbInsert.setContentEntityId(excellentableModel.getContentEntityId());
                            excellentableDbInsert.setContentType(excellentableModel.getContentType());
                            excellentableDbInsert.setSpaceKey(excellentableModel.getSpaceKey());
                            excellentableDbInsert.setCreated(ETDateUtils.currentTime());
                            excellentableDbInsert.setCreator(getCurrentUsername());

                            excellentableDbInsert.save();
                            excellentableModel.setID(excellentableDbInsert.getID());
                            // Insert into EDIT_HISTORY table
                            excellentableModel.setCreated(ETDateUtils.currentTime());
                            excellentableModel.setCreator(getCurrentUsername());
                            editHistoryDao.createHistory(excellentableDbInsert, excellentableModel);
                            try {
                                // Multiedit related firebase connection details
                                populateFirebaseDetail(excellentableModel);
                            } catch (UnirestException ex) {
                                java.util.logging.Logger.getLogger(ExcellentableRest.class.getName()).log(Level.SEVERE,
                                        null, ex);
                                LOGGER.error("Problem occurred while initiliazing this user for Collaborative editing: "
                                        + ex);
                            }
                            return excellentableModel;
                        }
                    });

            return Response.ok(excellentableModel).build();
        } else {
            int statusCode = checkPermission.get("status-code").getAsInt();
            String statusMsg = checkPermission.get("message").toString();
            return Response.status(Response.Status.fromStatusCode(statusCode)).entity(statusMsg).build();
        }
    }

    /**
     * Update Metadata of specified Excellentable
     *
     * @param excId
     * @param excellentableModel
     * @return Response
     */
    @PUT
    @AnonymousAllowed
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/table/{excId}")
    public Response updateTableData(@PathParam("excId") Integer excId, final ExcellentableModel excellentableModel) {
        try {
            ExcellentableDB[] excellentableDbInsert = ao.find(ExcellentableDB.class, "ID = ? ", excId);
            JsonObject checkPermission = excPermissionManager.hasPermissionOnExcellentable(excellentableDbInsert,
                    "EDIT");
            if (checkPermission.get("status").getAsBoolean()) {
                // Save here
                return saveData(excellentableDbInsert, excellentableModel, excId);
            } else {
                int statusCode = checkPermission.get("status-code").getAsInt();
                String statusMsg = checkPermission.get("message").toString();
                return Response.status(Response.Status.fromStatusCode(statusCode)).entity(statusMsg).build();
            }
        } catch (Exception e) {
            LOGGER.error("Excellentable could not saved: " + ExceptionUtils.getFullStackTrace(e));
            String errorMessage = i18nResolver
                    .getText("com.addteq.confluence.plugin.excellentable.internal.server.error");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(getResonseErrorMessage(errorMessage).toString()).build();
        }
    }

    /**
     * Excellentable Save Rest Endpoint. Data is received in zip format,
     * decompressed into string-> ExcellentableModel and then passes it to
     * saveData method.
     *
     * @param excId       : excellentable id
     * @param inputStream : zipInputStream from client
     * @return : return response to client(browser)
     */
    @PUT
    @AnonymousAllowed
    @Consumes("application/gzip")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/table/zip/{excId}")
    public Response saveGZip(@PathParam("excId") Integer excId, final InputStream inputStream) {
        try {
            ExcellentableDB[] excellentableDbInsert = ao.find(ExcellentableDB.class, "ID = ? ", excId);
            JsonObject checkPermission = excPermissionManager.hasPermissionOnExcellentable(excellentableDbInsert,
                    "EDIT");
            if (checkPermission.get("status").getAsBoolean()) {

                byte[] bytes = IOUtils.toByteArray(inputStream);
                String encoded = Base64.encodeBase64String(bytes);
                ExcellentableModel excellentableModel = new ExcellentableModel();
                excellentableModel.setMetaData(encoded);
                return saveData(excellentableDbInsert, excellentableModel, excId);
            } else {
                int statusCode = checkPermission.get("status-code").getAsInt();
                String statusMsg = checkPermission.get("message").toString();
                return Response.status(Response.Status.fromStatusCode(statusCode)).entity(statusMsg).build();
            }
        } catch (Exception e) {
            LOGGER.error("Excellentable could not saved: " + ExceptionUtils.getFullStackTrace(e));
            String errorMessage = i18nResolver
                    .getText("com.addteq.confluence.plugin.excellentable.internal.server.error");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(getResonseErrorMessage(errorMessage).toString()).build();
        }
    }

    /**
     * Saves data into excellentable db
     *
     * @param excellentableDbInsert : Retrieved excellentable with same excID
     * @param excellentableModel    : Data to be saved in modal format
     * @return {Response response}
     */
    private Response saveData(ExcellentableDB[] excellentableDbInsert, ExcellentableModel excellentableModel,
                              Integer excId) {
        ao.executeInTransaction(new TransactionCallback<ExcellentableModel>() {
            @Override
            public ExcellentableModel doInTransaction() {
                excellentableDbInsert[0].setMetaData(excellentableModel.getMetaData());
                excellentableDbInsert[0].setUpdated(ETDateUtils.currentTime());
                excellentableDbInsert[0].setUpdater(getCurrentUsername());
                excellentableDbInsert[0].save();

                excellentableModel.setCreated(ETDateUtils.currentTime());
                excellentableModel.setCreator(getCurrentUsername());
                editHistoryDao.createHistory(excellentableDbInsert[0], excellentableModel);
                // To return updated history Id for excellentable.
                EditHistoryDB[] editHistoryDBS = ao.find(EditHistoryDB.class,
                        Query.select().where("TABLE_DBID = ?", excId).order("ID DESC"));
                excellentableModel.setVersionNumber(editHistoryDBS.length);
                return excellentableModel;
            }
        });
        return Response.ok(excellentableModel).build();
    }

    /**
     * Delete Excellentable from DB
     *
     * @param excId
     * @return Response
     */
    @DELETE
    @AnonymousAllowed
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/table/{excId}")
    public Response removeTable(@PathParam("excId") Integer excId) {
        ExcellentableDB[] excellentableDbDelete = ao.find(ExcellentableDB.class, "ID = ?", excId);
        JsonObject checkPermission = excPermissionManager.hasPermissionOnExcellentable(excellentableDbDelete, "EDIT");
        if (checkPermission.get("status").getAsBoolean()) {
            EditHistoryDB[] editHistoryDB = ao.find(EditHistoryDB.class, "TABLE_DBID = ? ", excId);
            // Cascade delete
            ao.delete(editHistoryDB);
            ao.delete(excellentableDbDelete[0]);
        } else {
            // Preparing REST Response Object
            int statusCode = checkPermission.get("status-code").getAsInt();
            String statusMsg = checkPermission.get("message").toString();
            return Response.status(Response.Status.fromStatusCode(statusCode)).entity(statusMsg).build();
        }
        JsonObject resp = new JsonObject();
        resp.addProperty("message", "Record deleted successfully");
        return Response.ok(resp.toString()).build();
    }

    /**
     * Copies metaData of one Excellentable into another
     *
     * @param excellentableModel
     * @return Response
     */
    @POST
    @AnonymousAllowed
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/table/copy")
    public Response copyTableData(final ExcellentableModel excellentableModel) {
        Long contentEntityId = excellentableModel.getContentEntityId();
        String spaceKey = excellentableModel.getSpaceKey();
        if (contentEntityId.equals(0L) && spaceKey == null) {
            String msg = "Content Entity Id & Space Key is mandatory.";
            return Response.status(Response.Status.fromStatusCode(400)).entity(msg).build();
        }
        JsonObject checkPermission = excPermissionManager.hasPermissionOnContentEntity(
                excellentableModel.getContentEntityId(), excellentableModel.getContentType(),
                excellentableModel.getSpaceKey(), "EDIT");
        if (checkPermission.get("status").getAsBoolean()) {
            ExcellentableModel excellentableModelNew = ao
                    .executeInTransaction(new TransactionCallback<ExcellentableModel>() {
                        @Override
                        public ExcellentableModel doInTransaction() {
                            for (ExcellentableDB excellentableDbInsert : ao.find(ExcellentableDB.class, "ID = ?",
                                    excellentableModel.getID())) {

                                ExcellentableDB excellentableDbInsert1 = ao.create(ExcellentableDB.class);
                                excellentableDbInsert1.setMetaData(excellentableDbInsert.getMetaData());
                                excellentableDbInsert1.setContentEntityId(excellentableModel.getContentEntityId());
                                excellentableDbInsert1.setContentType(excellentableModel.getContentType());
                                excellentableDbInsert1.setSpaceKey(excellentableModel.getSpaceKey());
                                excellentableDbInsert1.setCreated(ETDateUtils.currentTime());
                                excellentableDbInsert1.setCreator(getCurrentUsername());
                                excellentableDbInsert1.save();
                                excellentableModel.setID(excellentableDbInsert1.getID());
                            }
                            return excellentableModel;
                        }
                    });
            return Response.ok(excellentableModel).build();
        } else {
            int statusCode = checkPermission.get("status-code").getAsInt();
            String statusMsg = checkPermission.get("message").toString();
            return Response.status(Response.Status.fromStatusCode(statusCode)).entity(statusMsg).build();
        }
    }

    /**
     * Retrieves Excellentable record from DB.
     *
     * @param excId: id of the Excellentable.
     * @param mode1  Rendering mode of the Excellentable. Possible values: view,
     *               edit
     * @return Response: Content of the Excellentable along with Collaborative
     * editing service connection info only if the mode is "edit".
     * @throws com.mashape.unirest.http.exceptions.UnirestException
     */
    @GET
    @AnonymousAllowed
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/table/{excId}")
    public Response retrieveTableMetaData(@PathParam("excId") Integer excId, @QueryParam("mode") String mode1)
            throws UnirestException {
        // Set default value as "view" in case client does not send rendering mode/type
        final String mode = StringUtils.isEmpty(mode1) ? "view" : mode1;
        final ExcellentableModel excellentableModel = new ExcellentableModel();
        excellentableModel.setID(excId);
        ExcellentableDB excTuple = excellentableDataService.getTable(excId);
        //Verify row
        JsonObject checkPermission = excPermissionManager.getPermissionOnExcellentable(excTuple);
        final int permission = checkPermission.get("permissionType").getAsInt();
        if (permission > 0) {
            excellentableDataService.getTableData(excId, excTuple, excellentableModel);
            // Check if user has edit permission so that it can return firebase connection information
            if (permission > 1 && "edit".equalsIgnoreCase(mode)) {
                try {
                    //TODO Implement transactions on dao access methods used in populateFirebaseDetail method
                    //Multiedit related firebase connection details
                    populateFirebaseDetail(excellentableModel);

                    //EXC-5010 Populate the multiedit attempted info
                    excellentableModel.getMultieditConnectionInfo().setTried(liveEditRegisterService.getRegisterMultiEditAttempt());
                } catch (UnirestException e) {
                    LOGGER.error("Failed to retrieve collaborative settings.", e);
                }
            }
            // Set a flag if this Excellentable's content is compressed
            Boolean isGZipped = Gzip.isCompressed(excellentableModel.getMetaData());
            excellentableModel.setIsGZipped(isGZipped);
            //Setting content size in bytes
            excellentableModel.setContentSize(Gzip.getStringSizeInBytes(excellentableModel.getMetaData()));
            return Response.ok(excellentableModel).build();
        } else {
            int statusCode = checkPermission.get("status-code").getAsInt();
            String statusMsg = checkPermission.get("message").toString();
            return Response.status(Response.Status.fromStatusCode(statusCode)).entity(statusMsg).build();
        }
    }

    /**
     * Returns primarily with contentType contentId and ExcId
     * @param excId excellentable id
     * @return read above
     */
    @GET
    @AnonymousAllowed
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/page/{excId}")
    public Response getContentTypeAndId(@PathParam("excId") Integer excId) {
        // Set default value as "view" in case client does not send rendering mode/type
        final ExcellentableModel excellentableModel = new ExcellentableModel();
        excellentableModel.setID(excId);
        ExcellentableDB excTuple = excellentableDataService.getTable(excId);
        //Verify row
        JsonObject checkPermission = excPermissionManager.getPermissionOnExcellentable(excTuple);
        final int permission = checkPermission.get("permissionType").getAsInt();
        if (permission > 0) {
            excellentableDataService.getContentData(excTuple, excellentableModel);
            return Response.ok(excellentableModel).build();
        } else {
            int statusCode = checkPermission.get("status-code").getAsInt();
            String statusMsg = checkPermission.get("message").toString();
            return Response.status(Response.Status.fromStatusCode(statusCode)).entity(statusMsg).build();
        }
    }

    private Page getContentForDiff(int id, int historyId) {

        String html = "";

        if (id >= 0) {
            ExcellentableModel oldExcTable = editHistoryDao.getHistory(id, historyId);
            String decompressedMetaData = Gzip.uncompressString(oldExcTable.getMetaData());
            html = JsonHtmlParser.getHTML(decompressedMetaData, false, "", true);

        }

        Page content = new Page();
        content.setBodyAsString(html);

        return content;
    }

    @GET
    @AnonymousAllowed
    @Produces(MediaType.TEXT_HTML)
    @Path("/table/diff/{excId}/old/{oldExc}/new/{newExc}")
    public Response getDiff(@PathParam("excId") @DefaultValue("0") int excId,
                            @PathParam("oldExc") @DefaultValue("0") int oldExc, @PathParam("newExc") @DefaultValue("0") int newExc) {

        JsonObject checkPermission = excPermissionManager.hasPermissionOnExcellentableById(excId, "VIEW");
        if (!checkPermission.get("status").getAsBoolean()) {
            int statusCode = checkPermission.get("status-code").getAsInt();
            String statusMsg = checkPermission.get("message").toString();
            return Response.status(Response.Status.fromStatusCode(statusCode)).entity(statusMsg).build();
        }

        String diffHtml = "Unable to retrive ";

        try {

            diffHtml = differ.diff(getContentForDiff(excId, oldExc), getContentForDiff(excId, newExc))
                    + VelocityUtils.getRenderedTemplate("template/excellentable-html-css.vm",
                    MacroUtils.createDefaultVelocityContext());

        } catch (Exception e) {

            LOGGER.error("error ", e);
        }

        return Response.ok(diffHtml).build();
    }

    @GET
    @AnonymousAllowed
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    /**
     * Returns history or list of histories based on the excId and optional
     * parameter historyId. If historyId id not passed then all the versions for
     * the excId will be returned. Refer for path syntax used here:
     * http://stackoverflow.com/questions/32765804/optional-params-in-rest-api-request-using-jersey-2-21
     */
    @Path("/table/{excId}/versions{noop: (/)?}{historyId: .*}")
    public Response getHistory(@PathParam("excId") @DefaultValue("0") int excId,
                               @PathParam("historyId") @DefaultValue("0") int historyId,
                               @QueryParam("meta") @DefaultValue("true") boolean isMeta,
                               @QueryParam("limit") @DefaultValue("10") int limit, @QueryParam("page") @DefaultValue("1") int page) {

        JsonObject checkPermission = excPermissionManager.hasPermissionOnExcellentableById(excId, "VIEW");
        if (!checkPermission.get("status").getAsBoolean()) {
            int statusCode = checkPermission.get("status-code").getAsInt();
            String statusMsg = checkPermission.get("message").toString();
            return Response.status(Response.Status.fromStatusCode(statusCode)).entity(statusMsg).build();
        }
        String errorMessage;
        Response.Status status;
        List<ExcellentableModel> editHistories = new ArrayList();
        if (excId <= 0) {
            errorMessage = i18nResolver.getText("com.addteq.confluence.plugin.excellentable.does.not.exist");
            status = Response.Status.NOT_FOUND;
            return Response.status(status).entity(getResonseErrorMessage(errorMessage).toString()).build();
        }
        if (!checkPermission.get("status").getAsBoolean()) {
            int statusCode = checkPermission.get("status-code").getAsInt();
            String statusMsg = checkPermission.get("message").toString();
            return Response.status(Response.Status.fromStatusCode(statusCode)).entity(statusMsg).build();
        }
        // Negative parameters are not allowed
        if (limit < 1 || page < 1) {
            errorMessage = i18nResolver
                    .getText("com.addteq.confluence.plugin.excellentable.parameter.cannotbe.negative");
            status = Response.Status.INTERNAL_SERVER_ERROR;
            return Response.status(status).entity(getResonseErrorMessage(errorMessage).toString()).build();
        }
        try {
            if (historyId == 0) {
                int offset = (page - 1) * limit;
                if (isMeta) {
                    // Get only overview of histories
                    editHistories = editHistoryDao.getAllHistoryOverview(excId, limit, offset);
                } else {
                    // Get all histories with complete data
                    editHistories = editHistoryDao.getAllHistory(excId, limit, offset);
                }
                // Finally create response if the editHistory is not empty
                if (!editHistories.isEmpty()) {
                    float total = editHistoryDao.getHistoryCount(excId);
                    return Response.ok(createPaginatedResponse(editHistories, excId, limit, page, total)).build();
                } else {
                    errorMessage = i18nResolver.getText("com.addteq.confluence.plugin.excellentable.record.not.found");
                    status = Response.Status.NOT_FOUND;
                }

            } else {
                ExcellentableModel editHistory = editHistoryDao.getHistory(excId, historyId);
                if (editHistory != null) {
                    return Response.ok(editHistory).build();
                } else {
                    errorMessage = i18nResolver
                            .getText("com.addteq.confluence.plugin.excellentable.version.does.not.exist");
                    status = Response.Status.NOT_FOUND;
                }

            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            errorMessage = i18nResolver.getText("com.addteq.confluence.plugin.excellentable.internal.server.error");
            status = Response.Status.INTERNAL_SERVER_ERROR;
        }
        return Response.status(status).entity(getResonseErrorMessage(errorMessage).toString()).build();
    }

    @PUT
    @AnonymousAllowed
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/table/{excId}/versions{noop: (/)?}{historyId: .*}")
    public Response history(@PathParam("excId") int excId, @PathParam("historyId") int historyId) {
        JsonObject checkPermission = excPermissionManager.hasPermissionOnExcellentableById(excId, "EDIT");
        if (!checkPermission.get("status").getAsBoolean()) {
            int statusCode = checkPermission.get("status-code").getAsInt();
            String statusMsg = checkPermission.get("message").toString();
            return Response.status(Response.Status.fromStatusCode(statusCode)).entity(statusMsg).build();
        }
        ExcellentableModel excellentableModel = new ExcellentableModel();
        excellentableModel.setID(excId);
        excellentableModel.setUpdated(ETDateUtils.currentTime());
        excellentableModel.setUpdater(getCurrentUsername());
        editHistoryDao.restoreTo(excellentableModel, historyId);
        excellentableModel.setCreatorFullName(getUserFullName());
        excellentableModel.setCreatedDate(ETDateUtils.getFormattedDate(excellentableModel.getUpdated()));
        excellentableModel.setProfilePicPath(getUserProfilePicPath());
        return Response.ok(excellentableModel).build();
    }

    /**
     * Retrieves Excellentable as HTML
     *
     * @param excId
     * @return Response
     */
    @GET
    @AnonymousAllowed
    @Produces(MediaType.TEXT_HTML)
    @Path("/table/html/{excId}")
    public Response getExcAsHtml(@PathParam("excId") Integer excId) {

        String htmlExc = VelocityUtils.getRenderedTemplate("template/excellentable-html-css.vm",
                MacroUtils.createDefaultVelocityContext());

        ExcellentableDB[] excellentableDb = ao.find(ExcellentableDB.class, "ID = ? ", excId);
        JsonObject checkPermission = excPermissionManager.hasPermissionOnExcellentable(excellentableDb, "VIEW");
        if (checkPermission.get("status").getAsBoolean()) {

            String metaData = excellentableDb[0].getMetaData();
            String decompressedString = Gzip.uncompressString(metaData);
            htmlExc += JsonHtmlParser.getHTML(decompressedString, false, "", true);
        }

        return Response.ok(htmlExc).build();
    }

    @GET
    @AnonymousAllowed
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/license/isEval")
    public Response isEvaluationLicense() {
        return Response.ok(licenseManager.getLicense().get().isEvaluation()).build();
    }

    @GET
    @AnonymousAllowed
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/version/installed")
    public Response version() {
        return Response.ok(pluginAccessor.getPlugin("Addteq.Excellentable").getPluginInformation().getVersion())
                .build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/table/json")
    public Response getJsonfromHtml(@FormParam("html") String html) {

        ImportHtml importfile = new ImportHtml();
        JsonObject jsonObject = new JsonObject();
        try {
            jsonObject = importfile.buildImportSheetJson(html, "1");
        } catch (Exception e) {
            return Response.ok("").build();
        }
        return Response.ok(jsonObject.toString()).build();
    }

    @GET
    @Path("/table/highlight-action-update")
    public Response highlightActionUpdate(@QueryParam("excId") Integer excId, @QueryParam("pageId") long pageId) {

        Response res = null;
        try {
            Page page = pageManager.getPage(pageId);
            Document dom = DomUtils.getContentDom(page.getBodyAsString());
            Element macro = DomUtils.findMacroByExcellentableId(excId, dom);
            Element table = DomUtils.closestByTag("table", macro);

            table.after(macro);

            table.remove();

            DomUtils.updateContent(page, dom, pageManager);
            res = Response.ok().build();
        } catch (Exception e) {
            LOGGER.error("Excellentable - Replace HTML table: failed to complete", e);
            res = Response.serverError().build();
        }


        return res;
    }

    private Map createPaginatedResponse(List<ExcellentableModel> editHistories, int excId, int limit, int page,
                                        float total) {
        int totalPage = (int) Math.ceil(total / limit);
        String self = i18nResolver.getText("com.addteq.confluence.plugin.excellentable.version.url", excId, limit,
                page);
        String first = i18nResolver.getText("com.addteq.confluence.plugin.excellentable.version.url", excId, limit, 1);
        String next = "";
        String previous = "";
        String last;

        int nextPage = page + 1;
        if ((limit * page) < (int) total) {
            next = i18nResolver.getText("com.addteq.confluence.plugin.excellentable.version.url", excId, limit,
                    nextPage);
        }

        int prevPage = page - 1;
        if (prevPage > 0) {
            previous = i18nResolver.getText("com.addteq.confluence.plugin.excellentable.version.url", excId, limit,
                    prevPage);
        }
        last = i18nResolver.getText("com.addteq.confluence.plugin.excellentable.version.url", excId, limit, totalPage);
        // Create Links object
        Links links = new Links(BASE_URL, first, next, previous, last, self);

        Map paginatedResponse = new HashMap();
        paginatedResponse.put("_links", links);
        paginatedResponse.put("results", editHistories);
        paginatedResponse.put("total", (int) total);
        paginatedResponse.put("size", limit);

        return paginatedResponse;
    }

    private JsonObject getResonseErrorMessage(String message) {
        JsonObject response = new JsonObject();
        response.addProperty("errorMessage", message);
        return response;
    }

    private String getCurrentUsername() {
        if (AuthenticatedUserThreadLocal.isAnonymousUser()) {
            return ANONYMOUS;
        } else {
            return AuthenticatedUserThreadLocal.get().getName();
        }
    }

    private String getUserFullName() {
        User user = userAccessor.getUserByName(getCurrentUsername());
        if (user == null) {
            return ANONYMOUS;
        } else {
            return user.getFullName();
        }
    }

    private String getLoggedInUserKey() {
        if (AuthenticatedUserThreadLocal.isAnonymousUser()) {
            return ANONYMOUS;
        } else {
            return AuthenticatedUserThreadLocal.get().getKey().getStringValue();
        }
    }

    private String getUserProfilePicPath() {
        return BASE_URL + userAccessor.getUserProfilePicture(getCurrentUsername()).getDownloadPath();
    }

    private void populateFirebaseDetail(ExcellentableModel excellentableModel) throws UnirestException {
        String userKey = getLoggedInUserKey();
        // Lambda call to add this Excellentable for editing        
        String decompressedMetaData = excellentableModel.getMetaData();
        InitMultieditModel initMultieditModel = new InitMultieditModel(excellentableModel.getID(), userKey,
                decompressedMetaData);
        MultieditConnectionInfo multieditConnectionInfo = lambdaRestClient.initCollaborativeEditing(initMultieditModel);
        if (multieditConnectionInfo != null) {
            // Reset secure information to null before creating response
            multieditConnectionInfo.setConfluenceId(null);
            multieditConnectionInfo.setPublicKey(null);

            // Populate excellentableModel with multieditConnectionInfo
            excellentableModel.setMultieditConnectionInfo(multieditConnectionInfo);

        }
    }
}
