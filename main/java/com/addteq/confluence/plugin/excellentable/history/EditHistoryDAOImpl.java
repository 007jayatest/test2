/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.addteq.confluence.plugin.excellentable.history;

import com.addteq.confluence.plugin.excellentable.ao.EditHistoryDB;
import com.addteq.confluence.plugin.excellentable.ao.ExcellentableDB;
import com.addteq.confluence.plugin.excellentable.model.ExcellentableModel;
import com.addteq.service.excellentable.exc_io.utils.ETDateUtils;
import com.addteq.service.excellentable.exc_io.utils.Gzip;
import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.user.User;
import net.java.ao.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 * @author vikashkumar
 */

@Component
public class EditHistoryDAOImpl implements EditHistoryDAO{

    private final ActiveObjects ao;
    private final UserAccessor userAccessor;
    private final String BASE_URL;
    private final String ANONYMOUS = "Anonymous";

    @Autowired
    public EditHistoryDAOImpl(
            @ComponentImport ActiveObjects ao,
            @ComponentImport UserAccessor userAccessor,
            @ComponentImport SettingsManager settingsManager) {

        this.ao = checkNotNull(ao);
        this.userAccessor = userAccessor;
        this.BASE_URL = settingsManager.getGlobalSettings().getBaseUrl();
    }
    @Override
    public EditHistoryDB createHistory(ExcellentableDB tableDB, ExcellentableModel editHistory) {
        final EditHistoryDB editHistoryDB = ao.create(EditHistoryDB.class);
        editHistoryDB.setTableDB(tableDB); // setting the relationship
        editHistoryDB.setBody(Gzip.compressString(tableDB.getMetaData()));
        editHistoryDB.setComment(editHistory.getComment());
        editHistoryDB.setCreated(editHistory.getCreated());
        editHistoryDB.setCreator(editHistory.getCreator());
        editHistory.setHistoryID(editHistoryDB.getID());
        editHistoryDB.save();
        return editHistoryDB;
    }

    @Override
    public ExcellentableModel getHistory(int excId, int historyId) {
        ExcellentableModel editHistory = new ExcellentableModel();
        Query query = Query.select().from(EditHistoryDB.class).where(" TABLE_DBID = ? and ID = ? ", excId, historyId);
        EditHistoryDB editHistoryDB[] = ao.find(EditHistoryDB.class, query);
        if(editHistoryDB.length > 0 && (editHistoryDB[0] != null)) {
            editHistory.setMetaData(Gzip.uncompressString(editHistoryDB[0].getBody()));
            editHistory.setCreated(editHistoryDB[0].getCreated());
            editHistory.setCreator(editHistoryDB[0].getCreator());
            editHistory.setComment(editHistoryDB[0].getComment());
            editHistory.setHistoryID(editHistoryDB[0].getID());
            return editHistory;
        }
        return null;
    }

    @Override
    public List<ExcellentableModel> getAllHistory(int excId, int limit, int offset) {
        List <EditHistoryDB> editHistories = new ArrayList<>();
        // For older version of excellentable where there were no versioning feature
        // Need to update the versioning table with one stored
        if (updateVersioningTable(excId, editHistories)) return makeModelObjects(editHistories);
        ao.stream(EditHistoryDB.class, Query.select(" ID, BODY, COMMENT, CREATED, CREATOR ").
                where(" TABLE_DBID = ? ", excId).order("ID DESC").limit(limit).offset(offset),
                editHistory -> editHistories.add(editHistory));
        return makeModelObjects(editHistories);
    }

    private boolean updateVersioningTable(int excId, List<EditHistoryDB> editHistories) {
        if(getHistoryCount(excId) == 0) {
            ExcellentableDB initialVersion = ao.get(ExcellentableDB.class, excId);
            if(initialVersion != null) {
                ExcellentableModel eModel = new ExcellentableModel();
                eModel.setCreated(ETDateUtils.currentTime());
                eModel.setCreator(getCurrentUsername());
                editHistories.add(createHistory(initialVersion, eModel));
            }
            return true;
        }
        return false;
    }

    @Override
    public List<ExcellentableModel> getAllHistoryOverview(int excId, int limit, int offset) {
        List<EditHistoryDB> editHistories = new ArrayList<>();
        // For older version of excellentable where there were no versioning feature
        // Need to update the versioning table with one stored
        if (updateVersioningTable(excId, editHistories)) return makeModelObjects(editHistories);
        ao.stream(EditHistoryDB.class, Query.select(" ID, COMMENT, CREATED, CREATOR ").
                where(" TABLE_DBID = ? ", excId).order("ID DESC").limit(limit).offset(offset),
                tableHistory -> editHistories.add(tableHistory));
        return makeModelObjects(editHistories);
    }

    @Override
    public int getHistoryCount(int excId) {
        return ao.count(EditHistoryDB.class, Query.select().where(" TABLE_DBID = ? ", excId));
    }

    /**
     * Restore to an old version of Excellentable from the list of edit histories.
     * Restoring involves two activities, first- it insert a new record into the EditHistory table
     * by copying the restore version. Second, it updates the ExcellentableDB with latest version.
     * @param excellentableModel Excellentable model object to get Excellentable ID, updated and updater values.
     * @param editHistoryId This is the id of the version to which restoration will happen.
     * @return ExcellentableModel
     */
    @Override
    public ExcellentableModel restoreTo(ExcellentableModel excellentableModel, int editHistoryId) {
        EditHistoryDB restoreToEditHistoryDB[] = ao.find(
                EditHistoryDB.class, " TABLE_DBID = ? and ID = ? ",
                excellentableModel.getID(), editHistoryId
        );
        // Update the ExcellentableDB
        ExcellentableDB excellentableDB = ao.get(ExcellentableDB.class, restoreToEditHistoryDB[0].getTableDB().getID());
        excellentableDB.setMetaData(restoreToEditHistoryDB[0].getBody());
        excellentableDB.setUpdated(excellentableModel.getUpdated());
        excellentableDB.setUpdater(excellentableModel.getUpdater());
        excellentableDB.save();
        // Create new edit history version
        excellentableModel.setCreated(excellentableModel.getUpdated());
        excellentableModel.setCreator(excellentableModel.getUpdater());
        createHistory(excellentableDB, excellentableModel);
       // Return the model to the caller after setting the latest Excellentable version (i.e metaData)
       excellentableModel.setMetaData(excellentableDB.getMetaData());
        return excellentableModel;
    }

    private List<ExcellentableModel> makeModelObjects(List<EditHistoryDB> editHistories) {
        List<ExcellentableModel> excellentableModels = new ArrayList<>();
        ExcellentableModel editHistory;
        for (EditHistoryDB editHistoryDB : editHistories) {
            editHistory = new ExcellentableModel();
            editHistory.setMetaData(Gzip.uncompressString(editHistoryDB.getBody()));
            editHistory.setComment(editHistoryDB.getComment());
            editHistory.setCreated(editHistoryDB.getCreated());
            editHistory.setCreatorFullName(getUserFullName(editHistoryDB.getCreator()));
            editHistory.setProfilePicPath(getUserProfilePicPath(editHistoryDB.getCreator()));
            editHistory.setCreatedDate(ETDateUtils.getFormattedDate(editHistoryDB.getCreated()));
            editHistory.setCreator(editHistoryDB.getCreator());
            editHistory.setHistoryID(editHistoryDB.getID());
            excellentableModels.add(editHistory);
        }
        return excellentableModels;
    }

    private String getUserFullName(String userName){
        User user = userAccessor.getUserByName(userName);
        if(user == null){
            return ANONYMOUS;
        }else{
            return user.getFullName();
        }
    }

    private String getCurrentUsername() {
        if (AuthenticatedUserThreadLocal.isAnonymousUser()) {
            return ANONYMOUS;
        } else {
            return AuthenticatedUserThreadLocal.get().getName();
        }
    }

    private String getUserProfilePicPath(String currentUserName){
        ConfluenceUser confluenceUser = userAccessor.getUserByName(currentUserName);
        return BASE_URL+userAccessor.getUserProfilePicture(confluenceUser).getDownloadPath();

    }
}
