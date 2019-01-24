/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.addteq.confluence.plugin.excellentable.attachment;

import com.addteq.confluence.plugin.excellentable.permission.ExcPermissionManager;
import com.addteq.service.excellentable.exc_io.importfile.Importable;
import com.addteq.service.excellentable.exc_io.importfile.impl.ImportXls;
import com.addteq.service.excellentable.exc_io.importfile.impl.ImportXlsx;
import com.atlassian.confluence.pages.Attachment;
import com.atlassian.confluence.pages.AttachmentManager;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.message.I18nResolver;
import com.google.gson.JsonObject;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Vikash Kumar <vikash.kumar@addteq.com>
 */

@Component
public class AttachmentServiceImpl implements AttachmentService {

    private final ExcPermissionManager excPermissionManager;
    private final I18nResolver i18nResolver;
    private final AttachmentManager attachmentManager;
    private static final String XLSX_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String XLS_CONTENT_TYPE = "application/vnd.ms-excel";
    static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AttachmentServiceImpl.class);

    @Autowired
    public AttachmentServiceImpl(
            @ComponentImport UserAccessor userAccessor,
            @ComponentImport I18nResolver i18nResolver,
            @ComponentImport AttachmentManager attachmentManager,
            ExcPermissionManager excPermissionManager) {

        this.i18nResolver = i18nResolver;
        this.attachmentManager = attachmentManager;
        this.excPermissionManager = excPermissionManager;
    }

    @Override
    public JsonObject parseAttachment(long attachmentId) {
        JsonObject attachmentAsJson = new JsonObject();
        JsonObject errorObj = new JsonObject();
        String spreadVersion = i18nResolver.getText("com.addteq.confluence.plugin.excellentable.spreadjs.version");

        Attachment attachment = attachmentManager.getAttachment(attachmentId);
        String spaceKey = attachment.getSpaceKey();

        JsonObject checkPermission = excPermissionManager.hasPermissionOnContentEntity(
                attachment.getContentId().asLong(),
                attachment.getContentTypeObject().getType(),
                spaceKey,
                "view");

        if (!checkPermission.get("status").getAsBoolean()) {
            return checkPermission;
        }
        InputStream attachmentInputStream = attachmentManager.getAttachmentData(attachment);
        String fileType = attachment.getMediaType();

        if (XLSX_CONTENT_TYPE.equals(fileType) || XLS_CONTENT_TYPE.equals(fileType)) {
            try {
                Workbook wb = WorkbookFactory.create(attachmentInputStream);
                if (wb instanceof HSSFWorkbook) {
                    Importable importfile = new ImportXls();
                    attachmentAsJson = importfile.buildImportSheetJson(wb, spreadVersion);
                } else if (wb instanceof XSSFWorkbook) {
                    Importable importfile = new ImportXlsx();
                    attachmentAsJson = importfile.buildImportSheetJson(wb, spreadVersion);
                }
            } catch (IOException ex) {
                LOGGER.error("An IO Exception occured while parsing an attached Excel file");
            } catch (InvalidFormatException | EncryptedDocumentException ex) {
                LOGGER.error("Error while parsing an attached Excel file, the file is in a different format than the expected Excel format");
                errorObj.addProperty("firstSheetExceeded", i18nResolver.getText("com.addteq.confluence.plugin.excellentable.import.fileExtension.error"));
            } catch (Exception ex) {
                LOGGER.error("Exception while parsing an attached Excel file");
                errorObj.addProperty("firstSheetExceeded", i18nResolver.getText("com.addteq.confluence.plugin.excellentable.import.fileExtension.error"));
            }
        }
        if (errorObj.has("firstSheetExceeded")) {
            attachmentAsJson.add("errorData", errorObj);
        }

        return attachmentAsJson;
    }

}
