package com.addteq.confluence.plugin.excellentable.multiedit.dao;

import com.addteq.confluence.plugin.excellentable.ao.ExcellentableDB;
import com.addteq.confluence.plugin.excellentable.exceptions.ExcellentableRuntimeException;
import com.addteq.confluence.plugin.excellentable.multiedit.ao.CollaboratorDB;
import com.addteq.confluence.plugin.excellentable.permission.ExcPermissionManager;
import com.addteq.service.excellentable.exc_io.utils.ETDateUtils;
import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.gson.JsonObject;
import net.java.ao.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 * @author Vikash Kumar
 * @author saurabh.gupta
 */

@Component
public class CollaboratorServiceImpl implements CollaboratorService{

    private final ActiveObjects ao;
    private final ExcPermissionManager excPermissionManager;
    
    @Autowired
    public CollaboratorServiceImpl(
            @ComponentImport ActiveObjects ao,
            ExcPermissionManager excPermissionManager) {
        
        this.ao = checkNotNull(ao);
        this.excPermissionManager = excPermissionManager;
    }

    @Override
    public Integer addUniqueCollaborator(Integer eid, String userKey, Integer versionNumber) {
        CollaboratorDB collaboratorAlreadyExists = getCollaboratorByUserKeyNoError(eid, userKey);
        return checkRecordIfNull(eid, userKey, versionNumber, collaboratorAlreadyExists);
    }

    @Override
    public CollaboratorDB getCollaboratorByUserKey(Integer eid, String userKey) {
        CollaboratorDB[] collaboratorDB = ao.find(CollaboratorDB.class,Query.select().where("EXC_ID = ? and USER_KEY = ? ", eid, userKey).order("LAST_SEEN DESC"));
        switch (collaboratorDB.length) {
            case 0:
                return null;
            case 1:
                return collaboratorDB[0];
            default:
                throw new ExcellentableRuntimeException("Duplicate contributer found for Excellentable ID: "+eid+" and userKey: "+ userKey);
        }
    }

    @Override
    public CollaboratorDB[] getCollaborators(Integer eid, Integer getUserUpdatedWithin) {
        Date time = new Date(System.currentTimeMillis() - getUserUpdatedWithin * 1000);
        return ao.find(CollaboratorDB.class, Query.select().where("EXC_ID = ? AND LAST_SEEN > ?", eid, time));
    }

    @Override
    public Integer updateOrCreateCollaborator(Integer eid, String userKey, Integer versionNumber) {
        CollaboratorDB collaboratorDB = getCollaboratorByUserKeyNoError(eid, userKey);
        return checkRecordIfNull(eid, userKey, versionNumber, collaboratorDB);
    }

    @Override
    public Integer deleteCollaborator(Integer eid, String userKey) {
        CollaboratorDB[] collaboratorDBS = ao.find(CollaboratorDB.class, Query.select().where("EXC_ID = ? AND USER_KEY = ? ", eid, userKey));
        int recordsFound = -1;
        if (collaboratorDBS != null) {
            recordsFound = collaboratorDBS.length;
            switch (recordsFound){
                case 0:
                    //Do Nothing
                    break;
                default:
                    for(int i =0; i < recordsFound; i++){
                        ao.delete(collaboratorDBS[i]);
                    }
                    break;
            }
        }
        return recordsFound;
    }

    @Override
    public JsonObject authorizeLoggedInUser(int excId) {
        ExcellentableDB[] excellentableDB = ao.find(ExcellentableDB.class, "ID = ? ", excId);
        return excPermissionManager.hasPermissionOnExcellentable(excellentableDB, "EDIT");
    }
    
    private Integer checkRecordIfNull(Integer eid, String userKey, Integer versionNumber, CollaboratorDB collaboratorDB){
        if(collaboratorDB == null) {
            addCollaborator(eid, userKey, versionNumber);
            return 0;
        }
        updateCollaborator(collaboratorDB, versionNumber);
        return 1;
    }

    private void addCollaborator(Integer eid, String userKey, Integer versionNumber) {
        CollaboratorDB collaboratorDB = ao.create(CollaboratorDB.class);
        collaboratorDB.setExcID(eid);
        collaboratorDB.setUserKey(userKey);
        collaboratorDB.setVersion(versionNumber);
        collaboratorDB.setLastSeen(ETDateUtils.currentTime());
        collaboratorDB.save();
    }

    private void updateCollaborator(CollaboratorDB collaboratorDB, Integer versionNumber) {
        collaboratorDB.setLastSeen(ETDateUtils.currentTime());
        collaboratorDB.setVersion(versionNumber);
        collaboratorDB.save();
    }

    /**
     * The current method getCollaboratorByUserKey we have will throw error when receiving more than one user.
     * This behaviour is correct for REST API call. For other methods calling getCollaboratorByUserKey, getting error
     * was wrong therefore making another copy which will only return first user.
     * @param eid excellentableid
     * @param userKey confluence user-key string in string format
     * @return          : First user of all the users found.
     */
    public CollaboratorDB getCollaboratorByUserKeyNoError(Integer eid, String userKey) {
        CollaboratorDB[] collaboratorDB = ao.find(CollaboratorDB.class,Query.select().where("EXC_ID = ? and USER_KEY = ? ", eid, userKey).order("LAST_SEEN DESC"));
        switch (collaboratorDB.length) {
            case 0:
                return null;
            default:
                return collaboratorDB[0];
        }
    }
}
