package com.addteq.service.excellentable.exc_io.importfile.impl;

import com.addteq.service.excellentable.exc_io.importfile.Importable;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.addteq.service.excellentable.exc_io.importfile.impl.ImportLimits.MAX_CELL_LIMIT;


/**
 * @author neeraj bodhe
 */
public class ImportCSV implements Importable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ImportCSV.class);

    @Override
    public JsonObject buildImportSheetJson(Object sheetData, String version) {
        InputStream targetStream = (InputStream) sheetData;
        JsonObject output = new JsonObject();
        int rowCount, colCount = 0, totalCells = 0, rowSize;
        JsonObject sheets = new JsonObject();
        JsonObject sheet1 = new JsonObject();
        JsonObject selections = new JsonObject();
        JsonObject zeroSelection = new JsonObject();
        JsonObject rowFilter = new JsonObject();
        JsonObject dataTable = new JsonObject();
        JsonObject dataObjectTemp = new JsonObject();
        JsonObject cols = new JsonObject();
        JsonObject errorObj = new JsonObject();
        JsonObject tempVal;
        JsonObject tempCol;

        try {

            BufferedReader br = new BufferedReader(new InputStreamReader(targetStream, StandardCharsets.UTF_8));
            CSVParser csvparser = CSVFormat.EXCEL.parse(br);
            List<CSVRecord> csvRecs = csvparser.getRecords();
            if (csvRecs.isEmpty()) {
                errorObj.addProperty("corruptData", "com.addteq.confluence.plugin.excellentable.import.emptyFile.error");
            }
            int count = 0;

            for (CSVRecord record : csvRecs) {
                tempCol = new JsonObject();
                rowSize = record.size();
                totalCells += rowSize;
                if (totalCells > MAX_CELL_LIMIT) {
                    errorObj.addProperty("sizeExceeded", "com.addteq.confluence.plugin.excellentable.import.size.limitReached");
                    break;
                }
                for (int i = 0; i < rowSize; i++) {
                    tempVal = new JsonObject();

                    tempVal.addProperty("value", record.get(i));
                    if (dataTable.has(Long.toString(count))) {
                        tempCol = (JsonObject) dataTable.get(Long.toString(count));
                    }
                    tempCol.add(Integer.toString(i), tempVal);
                    dataTable.add(Long.toString(count), tempCol);

                    if (colCount < record.size()) {
                        colCount = record.size();
                    }
                }
                count++;
            }

            //Get first sheet from the workbook

            output.addProperty("version", version);

            sheet1.addProperty("name", "Sheet1");                           // Sheet 1 is only considered here. Not any other sheets.

            zeroSelection.addProperty("row", 0);
            zeroSelection.addProperty("rowCount", 1);
            zeroSelection.addProperty("col", 0);
            zeroSelection.addProperty("colCount", 1);

            selections.add("0", zeroSelection);

            sheet1.add("selections", selections);                 // Selections in the work book sheet

            zeroSelection = new JsonObject();
            zeroSelection.addProperty("row", 0);
            zeroSelection.addProperty("rowCount", 0);
            zeroSelection.addProperty("col", 0);
            zeroSelection.addProperty("colCount", 0);

            rowFilter.add("range", zeroSelection);
            rowFilter.add("filterButtonVisibleInfo", new JsonObject());
            rowFilter.addProperty("showFilterButton", false);

            sheet1.add("rowFilter", rowFilter);                            // row filter if any

            JsonArray colArray = new JsonArray();
            cols.addProperty("resizable", true);
            cols.add("dirty", new JsonNull());
            sheet1.addProperty("activeRow", 0);                            // active row if any
            sheet1.addProperty("activeCol", 0);                             // active col if any
            sheet1.addProperty("index", 0);                     // index of the sheet


            dataObjectTemp.add("dataTable", dataTable);
            if (colCount < 26)       // Minimum size of the grid should be of 26 cols
                colCount = 26;
            for (int i = 0; i < colCount; i++) {    // addProperty columns in the array
                colArray.add(cols);
            }
            rowCount = csvRecs.size(); //lineNo;
            if (rowCount < 50)       // Minimum size of the grid should be of 50 rows
                rowCount = 50;
            JsonArray rowArray = new JsonArray();
            for (int i = 0; i < rowCount; i++) {    // addProperty rows in the array
                rowArray.add(cols);
            }

            sheet1.add("columns", colArray);                              // columns present in the sheet
            sheet1.add("rows", rowArray);                                 // rows present in the sheet
            sheet1.addProperty("rowCount", rowArray.size());                            // row count in the sheet
            sheet1.addProperty("columnCount", colArray.size());                          // column count in the sheet
            sheet1.add("data", dataObjectTemp);                // data of the cells

            sheets.add("Sheet1", sheet1);

            output.add("sheets", sheets);

        } catch (IOException ex) {
            LOGGER.error("Invalid character in file", ex);
            errorObj.addProperty("firstSheetExceeded", "com.addteq.confluence.plugin.excellentable.import.fileExtension.error");
        } catch (Exception ex) {
            LOGGER.error("Error occurred while importing CSV", ex);
            errorObj.addProperty("firstSheetExceeded", "com.addteq.confluence.plugin.excellentable.import.fileExtension.error");
        }
        output.add("errorData", errorObj);
        return output;
    }
}
