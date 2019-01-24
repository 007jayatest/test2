/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.addteq.confluence.plugin.excellentable.multiedit.dao;

import com.addteq.confluence.plugin.excellentable.multiedit.ao.LiveEditConfigDB;
import com.addteq.confluence.plugin.excellentable.multiedit.model.MultieditConnectionInfo;
import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 *
 * @author Vikash Kumar <vikash.kumar@addteq.com>
 */

@Component
public class MultieditSettingsDaoImpl implements MultieditSettingsDao{
    
    private final ActiveObjects ao;

    @Autowired
    public MultieditSettingsDaoImpl(@ComponentImport ActiveObjects ao) {
        this.ao = ao;
    }
    
    @Override
    public Integer saveMultieditSettings(MultieditConnectionInfo multieditConnectionInfo) {
        
        LiveEditConfigDB[] multieditSettingsDB = ao.find(LiveEditConfigDB.class);
        Integer status = 0;
        if ( multieditSettingsDB.length > 0) {
            saveMultieditDB(multieditSettingsDB[0], multieditConnectionInfo);
            status = 1; // Enabled by changing the status in the existing ao
        } else {
            LiveEditConfigDB multieditSettings = ao.create(LiveEditConfigDB.class);
            saveMultieditDB(multieditSettings, multieditConnectionInfo);
            status = 2; // Enabled by creating new settings with status = 1
        }
        
        return status;
    }
    @Override
    public Integer disableMultiedit() {
        LiveEditConfigDB[] multieditSettingsDB = ao.find(LiveEditConfigDB.class);
        Integer status = 0; // Return 0 if any error occurs in DB operation else return 1.
        if ( multieditSettingsDB.length > 0 ) {
            multieditSettingsDB[0].setStatus(0);
            multieditSettingsDB[0].save();
            status = 1;
        }
        return status;
    }
    
    @Override
    public MultieditConnectionInfo getMultieditSettings() {
        
        LiveEditConfigDB[] multieditSettingsDb = ao.find(LiveEditConfigDB.class);
        if(multieditSettingsDb.length > 0)
            return makeMultieditConnectionInfo(multieditSettingsDb[0]);
        else
            return null;
    }


    private boolean saveMultieditDB(LiveEditConfigDB multieditSettingsDB,
                                    MultieditConnectionInfo multieditConnectionInfo) {

        multieditSettingsDB.setStatus(multieditConnectionInfo.getStatus());

        //Save the confluence id only if its not blank or not null , Ref:EXC-4683
        if (isNotBlank(multieditConnectionInfo.getConfluenceId())) {
            multieditSettingsDB.setConfluenceId(multieditConnectionInfo.getConfluenceId());
        }

        multieditSettingsDB.setPublicKey(multieditConnectionInfo.getPublicKey());
        multieditSettingsDB.setApiKey(multieditConnectionInfo.getApiKey());
        multieditSettingsDB.setFirebaseUrl(multieditConnectionInfo.getFirebaseUrl());
        multieditSettingsDB.setFirebaseContext(multieditConnectionInfo.getFirebaseContext());
        multieditSettingsDB.save();
        
        return true;
    }
    
    private MultieditConnectionInfo makeMultieditConnectionInfo(LiveEditConfigDB multieditSettingsDb) {
        MultieditConnectionInfo multieditConnectionInfo = null;
        if (multieditSettingsDb != null) {
            multieditConnectionInfo = new MultieditConnectionInfo();
            multieditConnectionInfo.setApiKey(multieditSettingsDb.getApiKey());
            multieditConnectionInfo.setConfluenceId(multieditSettingsDb.getConfluenceId());
            multieditConnectionInfo.setPublicKey(multieditSettingsDb.getPublicKey());
            multieditConnectionInfo.setFirebaseUrl(multieditSettingsDb.getFirebaseUrl());
            multieditConnectionInfo.setFirebaseContext(multieditSettingsDb.getFirebaseContext());
            multieditConnectionInfo.setStatus(multieditSettingsDb.getStatus());
        }
        return multieditConnectionInfo;
    }
}
