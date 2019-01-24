/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.addteq.confluence.plugin.excellentable.multiedit.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Vikash Kumar <vikash.kumar@addteq.com>
 */
@XmlRootElement(name = "Collaborators")
@XmlAccessorType(XmlAccessType.FIELD)
public class InitMultieditModel {
    
    String publicKey;
    Integer excellentableId;
    String userKey;
    String content;
    String confluenceId;

    public InitMultieditModel(Integer excellentableId, String userKey, String content) {
        this.excellentableId = excellentableId;
        this.userKey = userKey;
        this.content = content;
    }

    public Integer getExcellentableId() {
        return excellentableId;
    }

    public void setExcellentableId(Integer excellentableId) {
        this.excellentableId = excellentableId;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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
    
}
