package com.addteq.confluence.plugin.excellentable.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.Set;

@XmlRootElement(name = "excellentableShare")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class ShareFilterModel {

    private Set<String> users;
    private Set<String> emails;
    private Set<String> groups;
    
    private String secretKey;
    private Long contentEntityId;
    private Integer ID;
    private String note;
    private String filterString;
    private String reporter;
    private String watchers;
    private Date sharedDate;
    private String metaData;
    private String globalSearchString;
    private String contentType;
    private String spaceKey;
    private boolean filterApplied;
    private int filterVersion;
    
    public ShareFilterModel(){
        
    }
    public ShareFilterModel(Set<String> users,Set<String> emails, Set<String> groups, String secretKey, Long contentEntityId,Integer ID,
            String note, String filterString,String reporter, String watchers, Date sharedDate, String metaData,String globalSearchString,
            int filterVersion) {      
        this.users = users;
        this.emails = emails;
        this.groups = groups;
        this.secretKey = secretKey;
        this.contentEntityId = contentEntityId;
        this.ID = ID;
        this.note = note;
        this.filterString = filterString;
        this.reporter = reporter;
        this.watchers = watchers;
        this.sharedDate = sharedDate;
        this.metaData = metaData;
        this.globalSearchString =globalSearchString;
        this.filterVersion = filterVersion;
    }

    public Set<String> getUsers() {
        return users;
    }

    public void setUsers(Set<String> users) {
        this.users = users;
    }

    public Set<String> getEmails() {
        return emails;
    }

    public void setEmails(Set<String> emails) {
        this.emails = emails;
    }

    public Set<String> getGroups() {
        return groups;
    }

    public void setGroups(Set<String> groups) {
        this.groups = groups;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public long getContentEntityId() {
        return contentEntityId;
    }

    public void setContentEntityId(long contentEntityId) {
        this.contentEntityId = contentEntityId;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getFilterString() {
        return filterString;
    }

    public void setFilterString(String filterString) {
        this.filterString = filterString;
    }

    public String getReporter() {
        return reporter;
    }

    public void setReporter(String reporter) {
        this.reporter = reporter;
    }

    public String getWatchers() {
        return watchers;
    }

    public void setWatchers(String watchers) {
        this.watchers = watchers;
    }

    public Date getSharedDate() {
        return sharedDate;
    }

    public void setSharedDate(Date sharedDate) {
        this.sharedDate = sharedDate;
    }

    public String getMetaData() {
        return metaData;
    }

    public void setMetaData(String metaData) {
        this.metaData = metaData;
    } 

    public String getGlobalSearchString() {
        return globalSearchString;
    }

    public void setGlobalSearchString(String globalSearchString) {
        this.globalSearchString = globalSearchString;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getSpaceKey() {
        return spaceKey;
    }

    public void setSpaceKey(String spaceKey) {
        this.spaceKey = spaceKey;
    }

    public boolean isFilterApplied() {
        return filterApplied;
    }

    public void setFilterApplied(boolean filterApplied) {
        this.filterApplied = filterApplied;
    }

    public void setFilterVersion(int filterVersion) {
        this.filterVersion = filterVersion;
    }
    
    public int getFilterVersion() {
        return filterVersion;
    }

}
