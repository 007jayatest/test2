/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.addteq.confluence.plugin.excellentable.multiedit.service;

import com.addteq.confluence.plugin.excellentable.multiedit.model.InitMultieditModel;
import com.addteq.confluence.plugin.excellentable.multiedit.model.MultieditConnectionInfo;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;

/**
 *
 * @author Vikash Kumar <vikash.kumar@addteq.com>
 */
public interface LambdaRestClient {
    
    String LAMBDA_ENDPOINT          = "https://einstein.excellentable.net";
    String LAMBDA_ENDPOIONT_ENABLE  = LAMBDA_ENDPOINT+"/keys";
    String LAMBDA_ENDPOIONT_INIT    = LAMBDA_ENDPOINT+"/users";
    String LAMBDA_ENDPOINT_TEST     = LAMBDA_ENDPOINT+"/test";
    
    public MultieditConnectionInfo isEnabled();
    
    public MultieditConnectionInfo enableMultiedit();
    
    public MultieditConnectionInfo enableMultiedit(JSONObject confluenceDetails);
    
    public MultieditConnectionInfo disableMultiedit() throws UnirestException;
    
    public MultieditConnectionInfo disableMultiedit(JSONObject confluenceDetails);

    public MultieditConnectionInfo initCollaborativeEditing(InitMultieditModel initMultieditModel); 
    
    public MultieditConnectionInfo testLambdaServer() ;
}
