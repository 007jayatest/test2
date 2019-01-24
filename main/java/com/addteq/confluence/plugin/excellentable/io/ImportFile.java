package com.addteq.confluence.plugin.excellentable.io;

import com.addteq.service.excellentable.exc_io.importfile.Importable;
import com.addteq.service.excellentable.exc_io.importfile.impl.ImportCSV;
import com.addteq.service.excellentable.exc_io.importfile.impl.ImportHtml;
import com.addteq.service.excellentable.exc_io.importfile.impl.ImportXls;
import com.addteq.service.excellentable.exc_io.importfile.impl.ImportXlsx;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.message.I18nResolver;
import com.google.gson.JsonObject;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.POIXMLException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author akanksha
 */
@Component
public class ImportFile {

    private final I18nResolver i18n;

    //Per EXC-4915, we are changing the import logic to use file extensions as a way to decide the algorithm for import
    private static final String XLSX_EXTENSION = "xlsx";
    private static final String XLS_EXTENSION = "xls";
    private static final String CSV_EXTENSION = "csv";
    private static final String HTML_EXTENSION = "html";


    @Autowired
    public ImportFile(@ComponentImport I18nResolver i18n) {
        this.i18n = i18n;
    }

    static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ImportFile.class);

    protected JsonObject parse(HttpServletRequest request) throws IOException {
        JsonObject jsonObject = new JsonObject();
        JsonObject errorObject = new JsonObject();

        try {

            if (ServletFileUpload.isMultipartContent(request)) {
                String version = null;
                InputStream fileData = null;
                DiskFileItemFactory factory = new DiskFileItemFactory();
                ServletFileUpload fileUpload = new ServletFileUpload(factory);
                List<FileItem> formItems = fileUpload.parseRequest(request);

                String fileExtension = null;
                for (FileItem item : formItems) {
                    if (item.isFormField()) {
                        if ("version".equals(item.getFieldName())) {
                            version = item.getString();
                        }
                    } else {
                        if ("file".equals(item.getFieldName())) {
                            fileData = item.getInputStream();
                            String fileName = item.getName(); //Procure the filename along with its extension
                            fileExtension = FilenameUtils.getExtension(fileName); //Extract the file extension to decide which path to take for import
                            LOGGER.debug("File Extension found is " + fileExtension);
                        }
                    }
                }

                if (XLSX_EXTENSION.equals(fileExtension) || XLS_EXTENSION.equals(fileExtension)) {
                    LOGGER.debug("This is an Excel file of xls or xlsx extension ...");
                    Workbook wb = WorkbookFactory.create(fileData);
                    if(wb instanceof HSSFWorkbook){
                        LOGGER.debug("Building import sheet for XLS file");
                        Importable importfile = new ImportXls();
                        jsonObject = importfile.buildImportSheetJson(wb,version);
                    }else if(wb instanceof XSSFWorkbook){
                        LOGGER.debug("Building import sheet for XLSX file");
                        Importable importfile = new ImportXlsx();
                        jsonObject = importfile.buildImportSheetJson(wb,version);
                    }
                }
                else if (CSV_EXTENSION.equals(fileExtension)) {
                    LOGGER.debug("Building import sheet for CSV file");
                    Importable importfile = new ImportCSV();
                    jsonObject = importfile.buildImportSheetJson(fileData,version);
                }
                else if (HTML_EXTENSION.equals(fileExtension)) {
                    LOGGER.debug("Building import sheet for HTML file");
                    Importable importfile = new ImportHtml();
                    jsonObject = importfile.buildImportSheetJson(fileData,version);
                }
            }
        } catch (InvalidFormatException ex) {
            LOGGER.error("Invalid file type found while importing");
            errorObject.addProperty("firstSheetExceeded", i18n.getText("com.addteq.confluence.plugin.excellentable.import.fileExtension.error"));
        } catch (POIXMLException ex) {
            LOGGER.error("Corrupted file found while importing, aborting the import!");
            errorObject.addProperty("firstSheetExceeded", i18n.getText("com.addteq.confluence.plugin.excellentable.import.fileExtension.error"));
        } catch (Exception ex) {
            LOGGER.error("Error occurred while importing");
            errorObject.addProperty("firstSheetExceeded", i18n.getText("com.addteq.confluence.plugin.excellentable.import.fileExtension.error"));
        }
        // if firstSheetExceeded value is not null then it will take errorObj of this file
        if (errorObject.get("firstSheetExceeded") != null) {
            jsonObject.add("errorData", errorObject);
        }

        return jsonObject;
    }

}