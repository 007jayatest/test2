package com.addteq.confluence.plugin.excellentable.multiedit.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author akanksha
 * @author Vikash Kumar <vikash.kumar@addteq.com>
 * 
 * This is a rest bean used to hold the request data and response data.
 * It is used when excellentable collaborative edit permission is added from admin page.
 */
@XmlRootElement(name = "multieditSettings")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class MultieditConnectionInfo {
    
    private int     status;
    private String  confluenceId;
    private String  publicKey;
    private String  apiKey;
    private String  firebaseUrl;
    private String  firebaseContext;
    private Boolean hasError;
    private String  message;
    private String  developerMessage;
    private String  token;
    private boolean tried; //Ref EXC:5010, this is the flag we will use to ascertain whether collabortive editing button was ever tried

    public MultieditConnectionInfo() {
        this.hasError = Boolean.FALSE;
        this.status = 0;
    }
    
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getConfluenceId() {
        return confluenceId;
    }

    public void setConfluenceId(String confluenceId) {
        this.confluenceId = confluenceId;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getFirebaseUrl() {
        return firebaseUrl;
    }

    public void setFirebaseUrl(String firebaseUrl) {
        this.firebaseUrl = firebaseUrl;
    }

    public String getFirebaseContext() {
        return firebaseContext;
    }

    public void setFirebaseContext(String firebaseContext) {
        this.firebaseContext = firebaseContext;
    }

    public Boolean getHasError() {
        return hasError;
    }

    public void setHasError(Boolean hasError) {
        this.hasError = hasError;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDeveloperMessage() {
        return developerMessage;
    }

    public void setDeveloperMessage(String developerMessage) {
        this.developerMessage = developerMessage;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isTried() {
        return tried;
    }

    public void setTried(boolean tried) {
        this.tried = tried;
    }

    @Override
    public String toString() {
        return "MultieditConnectionInfo{" 
                + "status=" + status + ", confluenceId=" + confluenceId 
                + ", publicKey=" + publicKey + ", apiKey=" + apiKey 
                + ", firebaseUrl=" + firebaseUrl + ", firebaseContext=" + firebaseContext 
                + ", hasError=" + hasError + ", message=" + message 
                + ", developerMessage=" + developerMessage
                + ", isTried=" + tried
                + '}';
    }

}
