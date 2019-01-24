package com.addteq.confluence.plugin.excellentable.multiedit.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "multieditSettings")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class MultieditSettingsResponse {
    private int     status;
    private Boolean hasError;
    private String  message;
    private String  developerMessage;

    public MultieditSettingsResponse(int status, Boolean hasError, String message, String developerMessage) {
        this.status = status;
        this.hasError = hasError;
        this.message = message;
        this.developerMessage = developerMessage;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public MultieditSettingsResponse(){
        this.hasError = Boolean.FALSE;
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
}
