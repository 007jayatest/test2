/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.addteq.confluence.plugin.excellentable.multiedit.dao;

import com.addteq.confluence.plugin.excellentable.multiedit.model.MultieditConnectionInfo;
import com.atlassian.activeobjects.tx.Transactional;

/**
 * Provides DAO layer to do crud operation for Collaborative editing settings
 * on the database.
 * 
 * @author Vikash Kumar <vikash.kumar@addteq.com>
 */
@Transactional
public interface MultieditSettingsDao {
    
    public Integer saveMultieditSettings(MultieditConnectionInfo multieditConnectionInfo);
    public Integer disableMultiedit();
    public MultieditConnectionInfo getMultieditSettings();
}
