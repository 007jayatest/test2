package com.addteq.confluence.plugin.excellentable.model;

import com.addteq.confluence.plugin.excellentable.multiedit.model.MultieditConnectionInfo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.List;

/**
 * Model class to represent Excellentable content.
 *
 * @author Trupti Kanase
 * @author Vikash Kumar <vikash.kumar@addteq.com>
 */
@XmlRootElement(name = "excellentableRest")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class ExcellentableModel {

    private String metaData;
    private String tableId;
    private int ID;
    private int historyId;
    private int versionNumber;
    private String themeName;
    private String filePath;
    private long contentEntityId;
    private String spaceKey;
    private String contentType;
    private Date created;
    private String creator;
    private Date updated;
    private String updater;
    private String comment;
    private List<ExcellentableModel> history;
    private String createdDate;
    private String updatedDate;
    private String creatorFullName;
    private String creatorProfilePicPath;
    private Boolean isGZipped;
    private int contentSize;

    private MultieditConnectionInfo multieditConnectionInfo;

    public ExcellentableModel() {
    }

    public ExcellentableModel(int ID, long contentEntityId, String spaceKey, String contentType) {
        this.ID = ID;
        this.contentEntityId = contentEntityId;
        this.spaceKey = spaceKey;
        this.contentType = contentType;
    }

    public ExcellentableModel(int ID, String metaData, String tableId, String themeName) {
        this.ID = ID;
        this.metaData = metaData;
        this.tableId = tableId;
        this.themeName = themeName;
    }

    public ExcellentableModel(String metaData, String tableId,
                              int ID, int historyId, String themeName, String filePath,
                              long contentEntityId, String spaceKey, String contentType,
                              Date created, String creator, String creatorFullName, String creatorProfilePicPath, Date updated, String updater,
                              String comment, List<ExcellentableModel> history,
                              String createdDate, String updatedDate) {
        this.metaData = metaData;
        this.tableId = tableId;
        this.ID = ID;
        this.historyId = historyId;
        this.themeName = themeName;
        this.filePath = filePath;
        this.contentEntityId = contentEntityId;
        this.spaceKey = spaceKey;
        this.contentType = contentType;
        this.created = created;
        this.creator = creator;
        this.updated = updated;
        this.updater = updater;
        this.comment = comment;
        this.history = history;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
        this.creatorFullName = creatorFullName;
        this.creatorProfilePicPath = creatorProfilePicPath;
    }

    public String getMetaData() {
        return metaData;
    }

    public void setMetaData(String metaData) {
        this.metaData = metaData;
    }

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

    public String getThemeName() {
        return themeName;
    }

    public void setThemeName(String themeName) {
        this.themeName = themeName;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getID() {
        return ID;
    }

    public void setHistoryID(int historyId) {
        this.historyId = historyId;
    }

    public int getHistoryID() {
        return historyId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getContentEntityId() {
        return contentEntityId;
    }

    public void setContentEntityId(long contentEntityID) {
        this.contentEntityId = contentEntityID;
    }

    public String getSpaceKey() {
        return spaceKey;
    }

    public void setSpaceKey(String spaceKey) {
        this.spaceKey = spaceKey;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public String getUpdater() {
        return updater;
    }

    public void setUpdater(String updater) {
        this.updater = updater;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<ExcellentableModel> getHistory() {
        return history;
    }

    public void setHistory(List<ExcellentableModel> history) {
        this.history = history;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getCreatorFullName() {
        return creatorFullName;
    }

    public void setCreatorFullName(String creatorFullName) {
        this.creatorFullName = creatorFullName;
    }

    public String getProfilePicPath() {
        return creatorProfilePicPath;
    }

    public void setProfilePicPath(String creatorProfilePicPath) {
        this.creatorProfilePicPath = creatorProfilePicPath;
    }

    public MultieditConnectionInfo getMultieditConnectionInfo() {
        return multieditConnectionInfo;
    }

    public void setMultieditConnectionInfo(MultieditConnectionInfo multieditConnectionInfo) {
        this.multieditConnectionInfo = multieditConnectionInfo;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    public Boolean getIsGZipped() {
        return isGZipped;
    }

    public void setIsGZipped(Boolean isGZipped) {
        this.isGZipped = isGZipped;
    }

    public void setContentSize(int contentSize) {
        this.contentSize = contentSize;
    }

    public int getContentSize() {
        return contentSize;
    }

    @Override
    public String toString() {
        return "ExcellentableModel"
                + "{" + "metaData=" + metaData + ", tableId=" + tableId + ", ID=" + ID + ", themeName=" + themeName +
                ", filePath=" + filePath + ", contentEntityId=" + contentEntityId + ", spaceKey=" + spaceKey +
                ", contentType=" + contentType + ", created=" + created + ", creator=" + creator + ", updated=" +
                updated + ", updater=" + updater + ", comment=" + comment + ", history=" + history + ", contentSize=" +
                contentSize + "}";
    }


}
