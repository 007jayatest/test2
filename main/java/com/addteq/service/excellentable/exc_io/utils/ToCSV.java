package com.addteq.service.excellentable.exc_io.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.poi.ss.usermodel.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by rober on 7/30/2016.
 */
public class ToCSV {

    private class DataRow {
        // In a row, data might be found in non-consecutive columns, like a key:value map
        public Map<Integer, String> valuesPerColumns;

        // Row Number in the Spreadsheet
        public int rowNumber;

        public DataRow(int rowNumber) {
            this.rowNumber = rowNumber;
            this.valuesPerColumns = new HashMap<Integer, String>();
        }

        public void addValueInColumn(int columnNumber, String value) {
            this.valuesPerColumns.put(columnNumber, value);
        }

        // Returns empty string if no value found at columnNumber
        public String getValueAtColumn(int columnNumber) {
            String valueFound = this.valuesPerColumns.get(columnNumber);

            return (valueFound != null) ? valueFound : "";
        }

        public String ToString() {
            String r = "Row# " + this.rowNumber + ":\n";
            for (Integer column: valuesPerColumns.keySet()) {
                r += column + " - " + valuesPerColumns.get(column) + ",";
            }
            return r;
        }
    }

    private final DataFormatter formatter = new DataFormatter(true);

    private final ArrayList<DataRow> ROWS_OF_DATA = new ArrayList<DataRow>();
    private int maxNumberOfColumns;
    private String csv;

    public ToCSV() {
        this.maxNumberOfColumns = 0;
        this.csv = "";
    }

    public static String fromSpreadJson(JsonObject spreadJson) {
        return new ToCSV().processSpreadJson(spreadJson);
    }

    public static String fromWorkbook(Workbook workbook) {
        return new ToCSV().processWorkbook(workbook);
    }


    private String processSpreadJson(JsonObject spreadJson) {
        JsonObject data = getDataFromSpreadJson(spreadJson);
        loadDataRowsFromSpreadJsonData(data);

        writeCSVStringFromDataRows();
        return this.csv;
    }

    private String processWorkbook(Workbook workbook) {
        // Used for formula parsing
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

        // Right now our spreadsheets are single sheets
        loadDataRowsFromSheet(workbook.getSheetAt(0), evaluator);

        writeCSVStringFromDataRows();
        return this.csv;
    }


    private JsonObject getDataFromSpreadJson(JsonObject spreadJson) {
        return spreadJson.getAsJsonObject("sheets")
                .getAsJsonObject("Sheet1")
                .getAsJsonObject("data")
                .getAsJsonObject("dataTable");
    }


    private void loadDataRowsFromSpreadJsonData(JsonObject data) {
        Iterator<Map.Entry<String, JsonElement>> rows = data.entrySet().iterator();

        // Go through all rows
        while (rows.hasNext()) {
            DataRow dataRow = createDataRowFromSpreadRow(rows.next());
            this.ROWS_OF_DATA.add(dataRow);
        }
    }

    private void loadDataRowsFromSheet(Sheet sheet, FormulaEvaluator evaluator) {
        Iterator<Row> rows = sheet.rowIterator();

        while (rows.hasNext()) {
            Row row = rows.next();
            createDataRowFromWorkbookRow(row, evaluator);
        }
    }

    private DataRow createDataRowFromSpreadRow(Map.Entry<String, JsonElement> row) {
        // Save the values we find in this row
        DataRow dataRow = new DataRow(Integer.parseInt(row.getKey()));

        // Go through all columns
        int lastColumnNumber = 1;
        Iterator<Map.Entry<String, JsonElement>> columns = ((JsonObject) row.getValue()).entrySet().iterator();
        while(columns.hasNext()) {
            Map.Entry<String, JsonElement> column = columns.next();
            JsonPrimitive valuePrimitive = ((JsonObject)column.getValue()).getAsJsonPrimitive("value");

            // A table in SpreadJSON might have a cell definition with no value (only style or other stuff)
            // So we need to check that value exists, else set value to empty
            String value = (valuePrimitive != null) ? valuePrimitive.getAsString() : "";

            int columnNumber = Integer.parseInt(column.getKey());
            dataRow.addValueInColumn(columnNumber, value);

            lastColumnNumber = columnNumber;
        }

        // Keep tracking the greatest number of columns
        if (this.maxNumberOfColumns < lastColumnNumber)
            this.maxNumberOfColumns = lastColumnNumber;

        return dataRow;
    }

    private void createDataRowFromWorkbookRow(Row row, FormulaEvaluator evaluator) {
        DataRow dataRow = new DataRow(row.getRowNum());

        int lastColumnInRow = 0;
        // Get the index for the right most cell on the row and then
        // step along the row from left to right recovering the contents
        // of each cell, storing them into DataRows objects
        Iterator<Cell> cells = row.cellIterator();
        while (cells.hasNext()) {
            Cell cell = cells.next();

            if(cell.getCellType() != Cell.CELL_TYPE_FORMULA)
                dataRow.addValueInColumn(cell.getColumnIndex(), this.formatter.formatCellValue(cell));
            else
                dataRow.addValueInColumn(cell.getColumnIndex(), this.formatter.formatCellValue(cell, evaluator));

            lastColumnInRow = cell.getColumnIndex();
        }
        // Make a note of the index number of the right most cell. This value
        // will later be used to ensure that the matrix of data in the CSV file
        // is square.
        if(lastColumnInRow > this.maxNumberOfColumns)
            this.maxNumberOfColumns = lastColumnInRow;

        this.ROWS_OF_DATA.add(dataRow);
    }


    private void writeCSVStringFromDataRows() {

        int i = 0;
        for (DataRow row: this.ROWS_OF_DATA) {
            while (row.rowNumber != i) {
                writeLineOfEmptyValues();
                i++;
            }

            writeLineWithValues(row);
            i++;
        }

        // Remove the last new line, not needed
        this.csv = this.csv.substring(0, csv.length() - 1);
    }

    private void writeLineOfEmptyValues() {
        for (int i = 0; i <= this.maxNumberOfColumns; i++)
            this.csv += ",";

        removeTrailingComa_And_AddNewLineEnding();
    }

    private void writeLineWithValues(DataRow row) {
        for (int i = 0; i <= this.maxNumberOfColumns; i++) {
            String value = row.getValueAtColumn(i);

            if (!value.isEmpty())
                this.csv += value;

            this.csv += ",";
        }

        removeTrailingComa_And_AddNewLineEnding();
    }

    private void removeTrailingComa_And_AddNewLineEnding() {
        this.csv = this.csv.substring(0, this.csv.length() - 1) + '\n';
    }
}
