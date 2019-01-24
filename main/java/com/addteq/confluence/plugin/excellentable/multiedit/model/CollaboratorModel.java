package com.addteq.confluence.plugin.excellentable.multiedit.model;
/**
 *
 * @author Vikash Kumar
 * @author saurabh.gupta
 */

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@XmlRootElement(name = "Collaborators")
@XmlAccessorType(XmlAccessType.FIELD)
public class CollaboratorModel {

    private Integer ID;
    private String userKey;
    private String  avatar;
    private Date  lastSeen;
    private Integer versionNumber;
    private String userName;

    public CollaboratorModel(Integer ID, String userKey, String avatar, Date lastSeen, Integer versionNumber,String userName) {
        this.ID = ID;
        this.userKey = userKey;
        this.avatar = avatar;
        this.lastSeen = lastSeen;
        this.versionNumber = versionNumber;
        this.userName = userName;
    }

    public CollaboratorModel() {
    }

    public Integer getID() {
        return ID;
    }

    public void setID(Integer ID) {
        this.ID = ID;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Date getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Date lastSeen) {
        this.lastSeen = lastSeen;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

}