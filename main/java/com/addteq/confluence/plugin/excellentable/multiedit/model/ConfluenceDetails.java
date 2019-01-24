/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.addteq.confluence.plugin.excellentable.multiedit.model;

/**
 *
 * @author Vikash Kumar <vikash.kumar@addteq.com>
 */
public class ConfluenceDetails {
    
    private String confluenceId;
    private String SEN;
    private String username;
    private String fullname;
    private String timestamp;
    private String agreementText;
    private String hostname;

    public String getConfluenceId() {
        return confluenceId;
    }

    public void setConfluenceId(String confluenceId) {
        this.confluenceId = confluenceId;
    }

    public String getSEN() {
        return SEN;
    }

    public void setSEN(String SEN) {
        this.SEN = SEN;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getAgreementText() {
        return agreementText;
    }

    public void setAgreementText(String agreementText) {
        this.agreementText = agreementText;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
    
    
}
