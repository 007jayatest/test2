/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *
 * There are two levels of debugging used
 * 1) warn: Short error message about the exception.
 * 2) debug: Detailed error message about the exception.
 */
package com.addteq.confluence.plugin.excellentable.multiedit.service;

import com.addteq.confluence.plugin.excellentable.multiedit.dao.MultieditSettingsDao;
import com.addteq.confluence.plugin.excellentable.multiedit.model.ConfluenceDetails;
import com.addteq.confluence.plugin.excellentable.multiedit.model.InitMultieditModel;
import com.addteq.confluence.plugin.excellentable.multiedit.model.MultieditConnectionInfo;
import com.addteq.service.excellentable.exc_io.utils.MultieditStatus;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.atlassian.upm.api.license.PluginLicenseManager;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.*;

import static org.apache.commons.lang.StringUtils.isBlank;

/**
 *
 * @author Vikash Kumar <vikash.kumar@addteq.com>
 */

@Component
public class LambdaRestClientImpl implements LambdaRestClient {
    
    private final MultieditSettingsDao multieditSettingsDao;
    private static final Logger LOGGER = LoggerFactory.getLogger(LambdaRestClientImpl.class);
    private final SettingsManager settingsManager;
    private final PluginLicenseManager pluginLicenseManager;
    private final SoyTemplateRenderer soyTemplateRenderer;

    @Autowired
    public LambdaRestClientImpl(
            MultieditSettingsDao multieditSettingsDao,
            @ComponentImport PluginLicenseManager pluginLicenseManager,
            @ComponentImport SettingsManager settingsManager,
            @ComponentImport SoyTemplateRenderer soyTemplateRenderer) {
        
        this.multieditSettingsDao = multieditSettingsDao;
        this.settingsManager = settingsManager;
        this.pluginLicenseManager = pluginLicenseManager;
        this.soyTemplateRenderer = soyTemplateRenderer;
    }
    
    /** 
     * @return MultieditSetingsModel if the Collaborative editing is enabled or had ever been configured.
     * If Collaborative editing was never enabled then it returns MultieditConnectionInfo with status value
     * MultieditStatus.NEVER_ENABLED_BEFORE but it never return null object.
     */
    @Override
    public MultieditConnectionInfo isEnabled() {
        MultieditConnectionInfo multieditiSettingsModel = multieditSettingsDao.getMultieditSettings();
        if(multieditiSettingsModel == null) {
            multieditiSettingsModel = new MultieditConnectionInfo();
            multieditiSettingsModel.setStatus(MultieditStatus.NEVER_ENABLED_BEFORE);
        }
        return multieditiSettingsModel;
    }

    @Override
    public MultieditConnectionInfo enableMultiedit() {
        MultieditConnectionInfo multieditConnectionInfo = isEnabled();
        switch (multieditConnectionInfo.getStatus()) {
            case MultieditStatus.NEVER_ENABLED_BEFORE:
                {
                    JSONObject confluenceDetails = makeLiveEditEnablePostData(generateUniqueServerID());
                    multieditConnectionInfo = enableMultiedit(confluenceDetails);
                    break;
                }
            case MultieditStatus.DISABLED:
                {
                    LOGGER.debug("Multi-edit is disabled, enabling it back again");
                    String confluenceId = multieditConnectionInfo.getConfluenceId();

                    // Ref : EXC-4683, never allow a null or blank confluence id to be passed to Einstein or
                    // to be stored in Active Objects, while trying to enable collaborative editing
                    if (isBlank(confluenceId)) {
                        LOGGER.info("Found Confluence ID blank, generating a new one ...");
                        confluenceId = generateUniqueServerID();
                    }

                    JSONObject confluenceDetails = makeLiveEditEnablePostData(confluenceId);
                    multieditConnectionInfo = enableMultiedit(confluenceDetails);
                    break;
                }
            case MultieditStatus.ENABLED:
                multieditConnectionInfo.setMessage("Collaborative Editing is already enabled");
                break;
            default: 
                multieditConnectionInfo.setHasError(Boolean.TRUE);
                multieditConnectionInfo.setMessage("The status of Collaborative Editing is unknown");
                multieditConnectionInfo.setDeveloperMessage("The status of Collaborative editing is: "+multieditConnectionInfo.getStatus());
        }
        return multieditConnectionInfo;
    }

    /**
     * @param confluenceDetails: JSON object representing unique id of Confluence instance 
     * @return True or false based on 
     * @throws UnirestException 
     */
    @Override
    public MultieditConnectionInfo enableMultiedit(JSONObject confluenceDetails) {

        final MultieditConnectionInfo multieditSettingModel = new MultieditConnectionInfo();
        Integer status = 0;
        Future<HttpResponse<String>> future = Unirest.post(LAMBDA_ENDPOIONT_ENABLE)
                    .header("content-type", "application/json")
                    .header("cache-control", "no-cache")
                    .body(confluenceDetails)
                .asStringAsync(new Callback<String>() {
                    @Override
                    public void completed(HttpResponse<String> response) {
                        if (response != null && response.getStatus() == 200) {

                            JSONObject multieditSettingResp = new JSONObject(response.getBody());
                            multieditSettingModel.setApiKey(multieditSettingResp.getString("apiKey"));
                            multieditSettingModel.setPublicKey(multieditSettingResp.getString("publicKey"));
                            multieditSettingModel.setFirebaseUrl(multieditSettingResp.getString("url"));
                            multieditSettingModel.setFirebaseContext(multieditSettingResp.getString("context"));
                            multieditSettingModel.setStatus(MultieditStatus.ENABLE);
                            multieditSettingModel.setConfluenceId(confluenceDetails.get("confluenceId").toString());
                        } else {
                            multieditSettingModel.setHasError(Boolean.TRUE);
                            multieditSettingModel.setMessage("An error occurred while enabling Collaborative Editing");
                            if(response != null){
                                multieditSettingModel.setDeveloperMessage(response.getBody());
                            } else {
                                multieditSettingModel.setDeveloperMessage("EnableMultiEdit: Error occurred while" +
                                        "enabling and response object is null");
                            }
                        }
                    }

                    @Override
                    public void failed(UnirestException ex) {
                        makeConnectionErrorResponse(multieditSettingModel, "An error occurred while enabling Collaborative Editing",
                                ex.getMessage());
                        LOGGER.warn("Excellentable failed to reach collaborative editing service");
                        LOGGER.debug("Exception: ", ex);
                    }

                    @Override
                    public void cancelled() {
                        makeConnectionErrorResponse(multieditSettingModel, "An error occurred while enabling Collaborative Editing",
                                "Excellentable Collaborative editing service timed out.");
                        LOGGER.warn("Excellentable failed to reach collaborative editing service");
                        LOGGER.debug("Excellentable Collaborative editing service timed out.");
                    }
                });
        HttpResponse<String> response = checkIfComplete(future);
        if (response != null && response.getStatus() == 200 && multieditSettingsDao.saveMultieditSettings(multieditSettingModel) != 0){
            multieditSettingModel.setMessage("Collaborative Editing is enabled");
        } else {
            multieditSettingModel.setHasError(Boolean.TRUE);
            multieditSettingModel.setMessage("Collaborative Editing could  not enabled");
            multieditSettingModel.setDeveloperMessage("While enabling Collaborative editing, the DB operation failed");
        }
        return multieditSettingModel;
    }
        
    @Override
    public MultieditConnectionInfo disableMultiedit() throws UnirestException {
        MultieditConnectionInfo multieditConnectionInfo = isEnabled();
        switch (multieditConnectionInfo.getStatus()) {
            case MultieditStatus.NEVER_ENABLED_BEFORE:
                {
                    multieditConnectionInfo.setMessage("Collaborative Editing was never enabled. Please enable it from Confluence Administration page.");
                    break;
                }
            case MultieditStatus.DISABLED:
                {   
                    multieditConnectionInfo.setMessage("Collaborative Editing is already disabled");
                    break;
                }
            case MultieditStatus.ENABLED: // Disable Collaborative Editing in this case
                JSONObject confluenceDetails = makeLiveEditDisablePostData(multieditConnectionInfo.getConfluenceId());
                multieditConnectionInfo = disableMultiedit(confluenceDetails);
                break;
            default: 
                multieditConnectionInfo.setHasError(Boolean.TRUE);
                multieditConnectionInfo.setMessage("The status of Collaborative Editing is unknown");
                multieditConnectionInfo.setDeveloperMessage("The status of Collaborative editing is: "+multieditConnectionInfo.getStatus());
        }
        sanitizeConnectionInfo(multieditConnectionInfo);
        return multieditConnectionInfo;
    }
    
    /**
     * @param confluenceDetails: JSON object representing unique id of Confluence instance 
     * @return True or false based on 
     * @throws UnirestException 
     */
    @Override
    public MultieditConnectionInfo disableMultiedit(JSONObject confluenceDetails) {
        Integer status = 0; // 0 -> disabled, 1 -> enabled
        MultieditConnectionInfo multieditConnectionInfo = new MultieditConnectionInfo();
        // Disable in the DB so that based on the value the plugin won't try to reach to Collaborative Editing Service
        status = multieditSettingsDao.disableMultiedit();
        if(status == 1) {
            multieditConnectionInfo.setMessage("Collaborative Editing disabled");
        } else {
            multieditConnectionInfo.setHasError(Boolean.TRUE);
            multieditConnectionInfo.setMessage("Some error occurred while disabling Collaborative Editing. Please try again.");
            multieditConnectionInfo.setDeveloperMessage("DB operation failed while disabling Collaborative editing");
            return multieditConnectionInfo;
        }
        // Disable from the Excellentable Collaborative Service (via Lambda service)
        Unirest.delete(LAMBDA_ENDPOIONT_ENABLE)
                .header("content-type", "application/json")
                .header("cache-control", "no-cache")
                .body(confluenceDetails)
                .asStringAsync(new Callback<String>() {
                    @Override
                    public void completed(HttpResponse<String> response) {
                        //Doesn't affect us if delete call is achieved or not
                    }

                    @Override
                    public void failed(UnirestException e) {
                        LOGGER.warn("Excellentable failed to reach collaborative editing service but collaborative " +
                                "editing is disabled.");
                        LOGGER.debug("Exception: ", e);
                    }

                    @Override
                    public void cancelled() {
                        //There is no scenario for cancellation
                    }
                });
        return multieditConnectionInfo;
    }
        
    private JSONObject makeLiveEditDisablePostData(String SERVER_ID) {
        ConfluenceDetails confDetailBean = new ConfluenceDetails();
        confDetailBean.setConfluenceId(SERVER_ID);
        JSONObject confluenceDetailJson = new JSONObject(confDetailBean);
        return confluenceDetailJson;
    }
    
    private JSONObject makeLiveEditEnablePostData(String SERVER_ID) {
        ConfluenceDetails confDetailBean = new ConfluenceDetails();
        ConfluenceUser loggedInUser = getLoggedinConfUser();
        String baseUrl = settingsManager.getGlobalSettings().getBaseUrl();
        String agreementContentText = getAgreementContent();
        
        // Populate confDetailBean object with all the data collected above.
        confDetailBean.setConfluenceId(SERVER_ID);
        confDetailBean.setFullname(loggedInUser.getFullName());
        confDetailBean.setUsername(loggedInUser.getName());
        confDetailBean.setTimestamp(new DateTime().toString());
        confDetailBean.setHostname(baseUrl);
        confDetailBean.setSEN(getSEN());
        confDetailBean.setAgreementText(agreementContentText);
        
        JSONObject confluenceDetailJson = new JSONObject(confDetailBean);
        return confluenceDetailJson;
    }
    
    private String getSEN() {
        return pluginLicenseManager.getLicense().get().getSupportEntitlementNumber().get();
    }
    
    private  ConfluenceUser getLoggedinConfUser() {

            return AuthenticatedUserThreadLocal.get();
    }
    
    private String generateUniqueServerID() {
        // CHANGE THIS TO CREATE UNIQUE ID OF CONLFUENCE SERVER-ID
        String SERVER_ID = UUID.randomUUID().toString();
        return SERVER_ID;
    }
    
    @Override
    public MultieditConnectionInfo initCollaborativeEditing(InitMultieditModel initMultieditModel) {

        final MultieditConnectionInfo multieditSettingModel = multieditSettingsDao.getMultieditSettings();
        if(multieditSettingModel == null) {
            MultieditConnectionInfo multieditSettingModel1 = new MultieditConnectionInfo();
            multieditSettingModel1.setHasError(Boolean.TRUE);
            multieditSettingModel1.setMessage("Collaborative Editing is not configured yet. "
                    + "Please enable it from Confluence Administration page.");

            return multieditSettingModel1;
        } else if(multieditSettingModel.getStatus() == MultieditStatus.DISABLED) {
            MultieditConnectionInfo multieditSettingModel1 = new MultieditConnectionInfo();
            multieditSettingModel1.setHasError(Boolean.TRUE);
            multieditSettingModel1.setMessage("Collaborative Editing is not enabled. Please contact your Confluence Administration to enable it.");

            return multieditSettingModel1;
        }
        
        String publicKey = multieditSettingModel.getPublicKey();
        initMultieditModel.setConfluenceId(multieditSettingModel.getConfluenceId());
        initMultieditModel.setPublicKey(publicKey);
        JSONObject postData = new JSONObject(initMultieditModel);

        Future<HttpResponse<String>> future = Unirest.post(LAMBDA_ENDPOIONT_INIT)
                    .header("content-type", "application/json")
                    .header("cache-control", "no-cache")
                    .body(postData)
                .asStringAsync(new Callback<String>() {
                    @Override
                    public void completed(HttpResponse<String> response) {
                        if (response != null && response.getStatus() == 200) {
                            JSONObject initMultieditResp = new JSONObject(response.getBody());
                            if (initMultieditResp.has("url") && initMultieditResp.has("context") && initMultieditResp.has("token")) {
                                multieditSettingModel.setFirebaseUrl(initMultieditResp.getString("url"));
                                multieditSettingModel.setFirebaseContext(initMultieditResp.getString("context"));
                                multieditSettingModel.setToken(initMultieditResp.getString("token"));
                            } else {
                                multieditSettingModel.setHasError(Boolean.TRUE);
                                multieditSettingModel.setMessage("Error occurred while initializing collaborative editing");
                                multieditSettingModel.setDeveloperMessage("Either of these properties is/are null: "
                                        + ", url: " + initMultieditResp.optString("url")
                                        + ", context" + initMultieditResp.optString("context")
                                        + ", token" + initMultieditResp.optString("token"));
                            }
                        } else {
                            multieditSettingModel.setHasError(Boolean.TRUE);
                            multieditSettingModel.setMessage("Error occurred while initializing collaborative editing");
                            if(response != null){
                                multieditSettingModel.setDeveloperMessage(response.getBody());
                            } else {
                                multieditSettingModel.setDeveloperMessage("initCollaborativeEditing: Error occurred" +
                                        " while enabling and response object is null");
                            }
                        }
                    }

                    @Override
                    public void failed(UnirestException ex) {
                        makeConnectionErrorResponse(multieditSettingModel,
                                "Error occurred while initializing collaborative editing.",
                                ex.getMessage());
                        LOGGER.warn("Error occurred while initializing collaborative editing");
                        LOGGER.debug("Exception: ", ex);
                        sanitizeConnectionInfo(multieditSettingModel);
                    }

                    @Override
                    public void cancelled() {
                        makeConnectionErrorResponse(multieditSettingModel,
                                "Error occurred while initializing collaborative editing.",
                                "Excellentable Collaborative editing service timed out.");
                        LOGGER.error("Error occurred while initializing collaborative Editing - Service timed out.");
                        sanitizeConnectionInfo(multieditSettingModel);
                    }
                });
        checkIfComplete(future);
        return multieditSettingModel;
    }
    
    @Override
    public MultieditConnectionInfo testLambdaServer() {

        final MultieditConnectionInfo multieditConnectionInfo = new MultieditConnectionInfo();
        Future<HttpResponse<String>> future = Unirest.get(LAMBDA_ENDPOINT_TEST)
                    .header("content-type", "application/json")
                    .header("cache-control", "no-cache")
                .asStringAsync(new Callback<String>() {
                    @Override
                    public void completed(HttpResponse<String> response) {
                        LOGGER.warn(response.getBody());
                        if (response.getStatus() == 200) {
                            JSONObject initMultieditResp = new JSONObject(response.getBody());
                            multieditConnectionInfo.setMessage(initMultieditResp.optString("success"));
                        } else {
                            makeConnectionErrorResponse(
                                    multieditConnectionInfo,
                                    "Something went wrong while connecting to the server",
                                    response.getStatusText());
                        }
                    }

                    @Override
                    public void failed(UnirestException ex) {
                        LOGGER.warn("Excellentable Collaborative editing service is unreachable.");
                        LOGGER.debug("Exception: ", ex);
                        makeConnectionErrorResponse(multieditConnectionInfo,
                                "Excellentable Collaborative editing service is unreachable.",
                                ex.getMessage());
                    }

                    @Override
                    public void cancelled() {
                        LOGGER.warn("Excellentable Collaborative editing service is unreachable.");
                        LOGGER.debug("Excellentable Collaborative editing service timed out.");
                        makeConnectionErrorResponse(multieditConnectionInfo,
                                "Excellentable Collaborative editing service is unreachable.",
                                "Excellentable Collaborative editing service timed out.");
                    }
                });

        checkIfComplete(future);
        return multieditConnectionInfo;
    }

    private void makeConnectionErrorResponse(
            MultieditConnectionInfo multieditConnectionInfo,
            String message,
            String devMessage) {

        multieditConnectionInfo.setHasError(Boolean.TRUE);
        multieditConnectionInfo.setMessage(message);
        multieditConnectionInfo.setDeveloperMessage(devMessage);
    }

    private void sanitizeConnectionInfo(MultieditConnectionInfo multieditConnectionInfo) {
        multieditConnectionInfo.setApiKey(null);
        multieditConnectionInfo.setConfluenceId(null);
        multieditConnectionInfo.setFirebaseContext(null);
        multieditConnectionInfo.setFirebaseUrl(null);
        multieditConnectionInfo.setPublicKey(null);
        multieditConnectionInfo.setToken(null);
    }

    private String getAgreementContent() {
        String agreementContentHtml = soyTemplateRenderer.render(
        "Addteq.Excellentable:excellentable-admin", 
        "Confluence.Templates.Excellentable.liveEditAgreement",
        null);
        Document doc = Jsoup.parse(agreementContentHtml);
        Element element = doc.getElementById("main-content");
        return element.text();
    }

    /**
     * It will iterate for a total of 5 secs. On each iteration it will check if the async call made by the unirest is
     * complete or not. (complete also consists of failed, interrupted etc) When its complete it will retrieve the
     * response returned by call and return it. It will also cancel the call if the total time taken exceeds 5 sec.
     *
     * @param future        :its a variable consisting of the call made by unirest.
     * @return              :http response returned by unirest call
     */
    private HttpResponse<String> checkIfComplete(Future<HttpResponse<String>> future) {
        try {
            for (int i = 0; i < 20; i++) {
                Thread.sleep(250);
                if (future.isDone()) {
                    return future.get(0, TimeUnit.MILLISECONDS);
                }
            }
            future.cancel(true);
        } catch (TimeoutException e){
            //It is required by future.get, as we call it explicitly no processing is required.
        } catch (ExecutionException e) {
            LOGGER.warn("Excellentable DB call execution failed");
        } catch (InterruptedException e){
            LOGGER.warn("Excellentable DB call interrupted");
        } catch (CancellationException e){
            LOGGER.warn("Excellentable DB call cancelled");
        }
        return null;
    }
}