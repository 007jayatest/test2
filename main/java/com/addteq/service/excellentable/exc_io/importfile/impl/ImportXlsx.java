package com.addteq.service.excellentable.exc_io.importfile.impl;

import com.addteq.service.excellentable.exc_io.importfile.Importable;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFPictureData;
import org.apache.poi.hssf.util.PaneInformation;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.*;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTDrawing;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTMarker;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTTwoCellAnchor;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.*;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.addteq.service.excellentable.exc_io.importfile.impl.ImportLimits.MAX_CELL_LIMIT;

/**
 * @author neeraj bodhe
 *         edited - To support multi sheet import
 */
public class ImportXlsx implements Importable {

    List<HSSFPictureData> hssfPictureData = new ArrayList<>();
    List<XSSFPictureData> xssfPictureData = new ArrayList<>();
    int pictCount = 0, rowCount = 0, colCount = 0;
    private JsonObject selections;
    JsonArray floatingOjects = new JsonArray();
    final private static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ImportXlsx.class);
    private static final short MIN_FONT_SIZE = 6;
    private static final short MAX_FONT_SIZE = 72;
    private static final String HYPERLINK_TYPE_NAME = "8";  //Cell type name for hyperlink in spreadjs
    private static final int MIN_ROW_COUNT = 99; //Minimum number of rows in sheet
    private static final int MIN_COL_COUNT = 25; //Minimum number of column in sheet
    private static final int TWIPS_TO_PX = 15;  //Twips to pixel conversion (1 pixel = 15 twips)
    private static final int CHAR_TO_PX = 8;   //characters to pixel conversion (1character = 8 pixel)
    private static final String GRIDLINE_COLOR = "rgb(212, 212, 212)";
    private static final int defaultBGColor = 64;
    private static final int defaultFGColor = 0;
    private static final int DEFAULT_ROW_HEIGHT = 25;
    private static final int DEFAULT_COLUMN_WIDTH = 150;



    private int protectedCellCountPerSheet;
    private int numberOfDataCellsInSheet;

    public int getProtectedCellCountPerSheet() {
        return protectedCellCountPerSheet;
    }

    public void setProtectedCellCountPerSheet(int protectedCellCountPerSheet) {
        this.protectedCellCountPerSheet = protectedCellCountPerSheet;
    }

    public int getNumberOfDataCellsInSheet() {
        return numberOfDataCellsInSheet;
    }

    public void setNumberOfDataCellsInSheet(int numberOfDataCellsInSheet) {
        this.numberOfDataCellsInSheet = numberOfDataCellsInSheet;
    }

    public JsonObject getSelections() {
        return selections;
    }

    public void setSelections() {
        selections = new JsonObject();
        JsonObject zeroSelections = new JsonObject();
        zeroSelections.addProperty("row", 0);
        zeroSelections.addProperty("rowCount", 1);
        zeroSelections.addProperty("col", 0);
        zeroSelections.addProperty("colCount", 1);
        selections.add("0", zeroSelections);
    }

    public enum ExcBorderStyle {
        NONE(0), THIN(1), MEDIUM(2), DASHED(3), DOTTED(4), THICK(5), DOUBLE(6), HAIR(7), MEDIUM_DASHED(8), DASH_DOT(9), MEDIUM_DASH_DOT(10), DASH_DOT_DOT(11), MEDIUM_DASH_DOT_DOT(12), SLANTED_DASH_DOT(13);
        private final int border;

        private ExcBorderStyle(int border) {
            this.border = border;
        }

        public int getExcBorderStyle() {
            return border;
        }
    }

    @Override
    public JsonObject buildImportSheetJson(Object sheetData, String version) {
        JsonObject spread = new JsonObject();
        JsonObject outputSheets = new JsonObject();
        JsonObject warnings = new JsonObject();
        int totalCells = 0;
        try {
            //Set Default Values For Each Sheet
            setSelections();

            Workbook wb = (Workbook) sheetData;

            spread.addProperty("version", version);
            spread.add("floatingObjects", new JsonObject());
            //Get each sheet and save it to outputSheets object

            totalCells = getTotalCellsCount_AND_LoadOutputSheets(wb, outputSheets, warnings);

            spread.add("sheets", outputSheets);
        } catch (ClassCastException cce) {
            warnings.addProperty("firstSheetExceeded", "com.addteq.confluence.plugin.excellentable.import.fileExtension.error");
            LOGGER.error("The file format or file extension is not valid. " +
                    "Verify that the file has not been corrupted and that the file extension matches " +
                    "the format of the file." + cce);
        } catch (SheetLimitExceededException sle) {
            LOGGER.warn("You are trying to import a sheet that is more than the specified limit of 250000 cells, hence aborting the import");
            putError(spread, outputSheets, warnings, sle);
        }

        spread.add("errorData", warnings);
        spread.addProperty("totalCells", totalCells);
        return spread;
    }

    static void putError(JsonObject spread, JsonObject outputSheets, JsonObject warnings, Exception ex) {
        if (ex.toString().equals("com.addteq.service.excellentable.exc_io.importfile.impl.SheetLimitExceededException: Sheet Limit Exceeded")) {
	          if (outputSheets.size() > 0) {
                spread.add("sheets", outputSheets);
                warnings.addProperty("sizeExceeded", "com.addteq.confluence.plugin.excellentable.import.size.limitReached");
            } else {
                warnings.addProperty("firstSheetExceeded", "com.addteq.confluence.plugin.excellentable.import.size.limitReached");
            }
        } else if (outputSheets.size() == 0) {
            warnings.addProperty("firstSheetExceeded", "com.addteq.confluence.plugin.excellentable.import.error");
        }
    }

    public int getTotalCellsCount_AND_LoadOutputSheets(Workbook wb, JsonObject outputSheets, JsonObject warnings)
            throws SheetLimitExceededException{

        int numberOfSheets = wb.getNumberOfSheets();
        int totalCells = 0;


        for (int i = 0; i < numberOfSheets; i++) {
            XSSFSheet inputSheet = (XSSFSheet) wb.getSheetAt(i);
            //Implemented Counter to limit the size of imports on the basis of cell
            totalCells += getNumberOfCellsInSheet(inputSheet);
            if (totalCells > MAX_CELL_LIMIT) throw new SheetLimitExceededException("Sheet Limit Exceeded");
            outputSheets.add("Sheet" + Integer.toString(i + 1), importSheet(inputSheet, warnings));
        }

        return totalCells;
    }

    //Import the sheet passed to it
    public JsonObject importSheet(XSSFSheet inputSheet, JsonObject warnings) {
        //Set the below two counters to zero before any sheet level iteration
        setProtectedCellCountPerSheet(0);
        setNumberOfDataCellsInSheet(0);

        JsonObject outputSheet = new JsonObject();
        JsonObject dataTable = new JsonObject();
        JsonArray commentsArray = new JsonArray();
        JsonArray spansArray = new JsonArray();
        JsonArray rowArray = new JsonArray();
        JsonObject defaults = new JsonObject();
        JsonObject rowObj, tempVal, tempCol, colObj, commentObj, formatterJSON, autoFormatter;
        HashSet<String> fontNames = new HashSet<>();

        int defaultRowHeight = inputSheet.getDefaultRowHeight() / TWIPS_TO_PX;     //Getting default row height and converting the height from twips to pixel
        int defaultColumnWidth = inputSheet.getDefaultColumnWidth() * CHAR_TO_PX;

        defaultRowHeight = (defaultRowHeight == 0) ? DEFAULT_ROW_HEIGHT : defaultRowHeight;
        defaultColumnWidth = (defaultColumnWidth == 0) ? DEFAULT_COLUMN_WIDTH : defaultColumnWidth;

        outputSheet.addProperty("name", inputSheet.getSheetName());                      // Name of the sheet
        defaults.addProperty("rowHeight", defaultRowHeight);                        //Set default height of the sheet
        defaults.addProperty("colWidth", defaultColumnWidth);                       //Set default width of the sheet
        defaults.addProperty("rowHeaderColWidth", 40);
        defaults.addProperty("colHeaderRowHeight", 20);
        outputSheet.add("selections", getSelections());                          // Selections in the work book sheet
        outputSheet.add("defaults", defaults);

        autoFilter(inputSheet, outputSheet);

        JsonArray colArray = new JsonArray();
        JsonObject cols = new JsonObject();
        cols.addProperty("resizable", true);
        cols.add("dirty", new JsonObject());
        outputSheet.addProperty("activeRow", 0);                                    // Active row in the sheet
        outputSheet.addProperty("activeCol", 0);                                    // Active col in the row
        for (int i = 0; i < inputSheet.getNumMergedRegions(); i++) {
            CellRangeAddress range = inputSheet.getMergedRegion(i);
            spansArray.add(getSpans(range));
        }
        outputSheet.add("spans", spansArray);
        JsonArray tableArray = tableFormatting(inputSheet, warnings);

        outputSheet.addProperty("index", 0);

        JsonObject dataObjectTemp = new JsonObject();
        int rowStart = Math.min(0, inputSheet.getFirstRowNum());
        int rowEnd = Math.max(1400, inputSheet.getLastRowNum());
        for (int rowIndex = rowStart; rowIndex < rowEnd; rowIndex++) {
            rowObj = new JsonObject();
            Row row = inputSheet.getRow(rowIndex);
            //Row having no data or no height set
            if (row == null) {
                rowObj.addProperty("resizable", true);
                rowObj.addProperty("size", defaultRowHeight);
                rowArray.add(rowObj);
                continue;
            }
            //Row having data and height set or no data but height set
            int rowHeight = row.getHeight() / TWIPS_TO_PX;
            rowHeight = (rowHeight== 0) ? DEFAULT_ROW_HEIGHT : rowHeight;
            rowObj.addProperty("resizable", true);
            rowObj.addProperty("size", rowHeight);
            rowArray.add(rowObj);


            //For each row, iterate through each columns
            tempCol = new JsonObject();
            int lastColumn = Math.max(row.getLastCellNum(), MIN_COL_COUNT);
            for (int colIndex = 0; colIndex <= lastColumn; colIndex++) {
                //Get cell having data or blank cell
                Cell cell = row.getCell(colIndex, Row.RETURN_NULL_AND_BLANK);
                int colWidth = Math.round(inputSheet.getColumnWidthInPixels(colIndex));
                colWidth = (colWidth== 0) ? DEFAULT_COLUMN_WIDTH : colWidth;
                if(colIndex >= colArray.size()){
                    colObj = new JsonObject();
                    colObj.addProperty("resizable", true);
                    colObj.addProperty("size", colWidth);
                    colArray.add(colObj);
                }
                if (cell == null) {
                    continue;
                }

                if (cell.getCellComment() != null) {
                    commentObj = new JsonObject();
                    Comment comment = cell.getCellComment();
                    commentObj.addProperty("text", comment.getString().getString());
                    commentObj.addProperty("rowIndex", comment.getRow());
                    commentObj.addProperty("colIndex", comment.getColumn());
                    commentsArray.add(commentObj);
                }

                tempVal = new JsonObject();

                String cellColumnIndex = Integer.toString(cell.getColumnIndex());

                switch (cell.getCellType()) {
                    case Cell.CELL_TYPE_FORMULA:
                        String cellFormula = cell.getCellFormula();
                        if (!cellFormula.contains("HYPERLINK")) {
                            tempVal.addProperty("formula", cellFormula);
                            short formatterIndex = cell.getCellStyle().getDataFormat();
                            String formatter = cell.getCellStyle().getDataFormatString();
                            if (DateUtil.isADateFormat(formatterIndex, formatter)) { //If the cell is in DateTime format.
                                formatterJSON = new JsonObject();
                                autoFormatter = new JsonObject();
                                formatterJSON.addProperty("customerCultureName", "en-US");
                                formatterJSON.addProperty("formatCached", formatter);
                                autoFormatter.add("autoFormatter", formatterJSON);
                                tempVal.add("style", autoFormatter);
                            }
                        }
                        tempCol.add(cellColumnIndex, tempVal);
                        break;
                    case Cell.CELL_TYPE_BOOLEAN:
                        tempVal.addProperty("value", cell.getBooleanCellValue());
                        tempCol.add(cellColumnIndex, tempVal);
                        break;
                    case Cell.CELL_TYPE_NUMERIC:
                        String formatter = cell.getCellStyle().getDataFormatString();
                        String timeFormatter="h\":\"mm\" \"am/pm\" \"";
                        String timeFormatter2="h:mm am/pm";
                        // change the formatter of cell to proper date-time formatter
                        if (timeFormatter.equals(formatter) || timeFormatter2.equals(formatter)) {
                            short dateFormat = inputSheet.getWorkbook().getCreationHelper().createDataFormat().getFormat("HH:MM AM/PM");
                            CellStyle dateCellStyle = cell.getCellStyle();
                            dateCellStyle.setDataFormat(dateFormat);
                            cell.setCellStyle(dateCellStyle);
                        }
                        if (!"".equals(formatter) && !"GENERAL".equalsIgnoreCase(formatter)) {
                            if (DateUtil.isCellDateFormatted(cell)) { //If the cell is in DateTime format.
                                formatterJSON = new JsonObject();
                                autoFormatter = new JsonObject();
                                formatterJSON.addProperty("customerCultureName", "en-US");
                                formatterJSON.addProperty("formatCached", formatter);
                                autoFormatter.add("autoFormatter", formatterJSON);

                                tempVal.addProperty("value", "/OADate(" + cell.getNumericCellValue() + ")/");
                                tempVal.add("style", autoFormatter);
                            } else {
                                tempVal.addProperty("value", cell.getNumericCellValue());
                            }
                        } else {
                            tempVal.addProperty("value", cell.getNumericCellValue());
                        }
                        tempCol.add(cellColumnIndex, tempVal);
                        break;
                    case Cell.CELL_TYPE_STRING:
                        if (cell.getHyperlink() == null) {
                            tempVal.addProperty("value", cell.getStringCellValue());
                            tempCol.add(cellColumnIndex, tempVal);
                        }
                        break;
                }
                cellStyles(cell, tempVal, tempCol, cellColumnIndex, warnings, fontNames, inputSheet);

                if (colCount < cell.getColumnIndex()) {
                    colCount = cell.getColumnIndex();
                }
            }
            rowCount = row.getRowNum();
            dataTable.add(Integer.toString(row.getRowNum()), tempCol);
        }

        dataObjectTemp.add("dataTable", dataTable);

        if (colCount < MIN_COL_COUNT) {      // Minimum size of the grid should be of 26 cols
            colCount = MIN_COL_COUNT;
        }
        if (rowCount < MIN_ROW_COUNT) {     //Minimum size of grid should be of 100 rows
            rowCount = MIN_ROW_COUNT;
        }
        //Frozen pane
        PaneInformation paneInformation = inputSheet.getPaneInformation();
        addPaneInformationToSheet(outputSheet, paneInformation);
        //sheet without gridlines
        addGridlinesToSheet(outputSheet, inputSheet.isDisplayGridlines(), GRIDLINE_COLOR);


        outputSheet.add("columns", colArray);                           // the columns in the sheet
        outputSheet.add("rows", rowArray);                              // the rows in the sheet
        outputSheet.addProperty("rowCount", rowCount + 1);                      // the row count in the sheet
        outputSheet.addProperty("columnCount", colCount + 1);                   // the column count in the sheet
        outputSheet.add("comments", commentsArray);
        outputSheet.add("tables", tableArray);                           //Tables in the sheet i.e table formatting
        outputSheet.add("data", dataObjectTemp);                        // All the cell data
        JsonArray floatingObjects = floatingObjects(inputSheet, warnings);
        outputSheet.add("floatingObjects", floatingObjects);            // floating objects i.e images of the sheet


        LOGGER.debug("Number of Cells in Sheet is " + getNumberOfDataCellsInSheet());
        LOGGER.debug("Protected Cell Count per Sheet " + getProtectedCellCountPerSheet());

        //EXC-4754 : If the excel sheet is locked and its data cells and locked cells are same in count,
        //thats when you set the sheet level protection
        if ((getNumberOfDataCellsInSheet() == getProtectedCellCountPerSheet()) && (inputSheet.isSheetLocked())) {
            outputSheet.addProperty("isProtected", true);
        }


        return outputSheet;
    }

    private JsonObject getSpans(CellRangeAddress range) {
        int startRow = range.getFirstRow();
        int startColomn = range.getFirstColumn();
        JsonObject spans = new JsonObject();
        spans.addProperty("row", startRow);
        spans.addProperty("rowCount", range.getLastRow() + 1 - startRow);
        spans.addProperty("col", startColomn);
        spans.addProperty("colCount", range.getLastColumn() + 1 - startColomn);
        return spans;
    }

    /**
     * If any floating objects in the sheet then put to the json object response
     */
    private JsonArray floatingObjects(XSSFSheet sheet, JsonObject warnings) {
        JsonArray floatingObjArray = new JsonArray();
        int i = 0;
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        for (XSSFShape shape : drawing.getShapes()) {
            if (shape instanceof Picture) {

                CTDrawing ctDrawing = drawing.getCTDrawing();
                CTTwoCellAnchor twoCellAnchorArray = ctDrawing.getTwoCellAnchorArray(i);

                CTMarker startCellRef = twoCellAnchorArray.getFrom();
                int startCol = startCellRef.getCol();
                int startRow = startCellRef.getRow();
                CTMarker endCellRef = twoCellAnchorArray.getTo();
                int endCol = endCellRef.getCol();
                int endRow = endCellRef.getRow();

                //Picture data or src info of floating object
                XSSFPictureData image = ((XSSFPicture) shape).getPictureData();
                byte[] imgData = image.getData();
                String mimeType = image.getMimeType();
                StringBuilder sb = new StringBuilder();
                sb.append("data:").append(mimeType).append(";base64,");
                sb.append(StringUtils.newStringUtf8(Base64.encodeBase64(imgData, false)));
                String Img = sb.toString();

                //Floating object name
                String floatingObjName = "floatingObject" + i;
                i++;

                //JSON creation for floating objects
                JsonObject floatingObj = new JsonObject();
                floatingObj.addProperty("name", floatingObjName);
                floatingObj.addProperty("startColumn", startCol);
                floatingObj.addProperty("startRow", startRow);
                floatingObj.addProperty("endColumn", endCol);
                floatingObj.addProperty("endRow", endRow);
                floatingObj.addProperty("src", Img);
                floatingObjArray.add(floatingObj);
            } else if(shape instanceof XSSFShapeGroup){
                warnings.addProperty("ShapeNotImported", "com.addteq.confluence.plugin.excellentable.import.style.shapes.error");
            }
        }
        return floatingObjArray;
    }

    /**
     * If any filter condition applied to row filter then put to the json object
     * response
     */
    private JsonObject filterCondition(CTAutoFilter autoFilter, JsonObject rowFilterObj, int startCol) {
        JsonArray filterItemMapArray = new JsonArray();
        JsonArray filteredColumnsArray = new JsonArray();
        if (autoFilter != null) { //if autofilter set
            CTFilterColumn[] filterColumnArray = autoFilter.getFilterColumnArray();
            int filterColArrayLength = filterColumnArray.length;
            for (int k = 0; k < filterColArrayLength; k++) {
                JsonObject filterItemMapObj = new JsonObject();
                JsonArray filterConditionArray = new JsonArray();
                CTFilterColumn filterColumnArray1 = autoFilter.getFilterColumnArray(k);
                long colId = filterColumnArray1.getColId();
                CTFilters filters = filterColumnArray1.getFilters();
                if (filters == null) {
                    continue;
                }
                if (filters.getBlank()) {
                    JsonObject filterConditionObj = new JsonObject();
                    filterConditionObj.addProperty("compareType", 0);
                    filterConditionObj.addProperty("expected", "");
                    filterConditionObj.addProperty("formula", "null");
                    filterConditionObj.addProperty("useWildCards", false);
                    filterConditionObj.addProperty("conType", 2);
                    filterConditionArray.add(filterConditionObj);
                }
                CTFilter[] filterArray = filters.getFilterArray();
                for (int l = filterArray.length - 1; l >= 0; l--) {
                    JsonObject filterConditionObj = new JsonObject();
                    String val = filters.getFilterArray(l).getVal();
                        /* Put filter conditions to json object response */
                    filterConditionObj.addProperty("compareType", 0);
                    filterConditionObj.addProperty("expected", val);
                    filterConditionObj.addProperty("formula", "null");
                    filterConditionObj.addProperty("useWildCards", false);
                    filterConditionObj.addProperty("conType", 2);
                    filterConditionArray.add(filterConditionObj);
                }
                    /*Put filter column index to json object response*/
                filterItemMapObj.addProperty("index", startCol + colId);

                filterItemMapObj.add("conditions", filterConditionArray);

                    /*Put filter condition and index json object to json array */
                filterItemMapArray.add(filterItemMapObj);
                filteredColumnsArray.add(startCol + colId);
            }
        }
        rowFilterObj.add("filterItemMap", filterItemMapArray);
        rowFilterObj.add("filteredColumns", filteredColumnsArray);
        return rowFilterObj;
    }

    /**
     * If sort condition is applied to row filter then addProperty to the json object     * response
     */
    private JsonObject sortCondition(CTSortState sortState, JsonObject sortFilterObj) {
        JsonObject sortInfoObj = new JsonObject();
        /* Put Sort condition and index to json object response*/
        if (sortState != null) {
            CTSortCondition sortConditionArray = sortState.getSortConditionArray(0);
            AreaReference areaReference = new AreaReference(sortConditionArray.getRef(), SpreadsheetVersion.EXCEL2007);
            short sortColIdx = areaReference.getFirstCell().getCol();
            sortInfoObj.addProperty("index", sortColIdx);
            sortInfoObj.addProperty("ascending", true);
            if (sortConditionArray.getDescending()) {
                sortInfoObj.addProperty("ascending", false);
            }
            sortFilterObj.add("sortInfo", sortInfoObj);

        }
        return sortFilterObj;
    }

    /**
     * If any row filter in the sheet then put to the json object response
     */
    private void autoFilter(XSSFSheet sheet, JsonObject sheetMetaData) {
        CTWorksheet ctWorksheet = sheet.getCTWorksheet();
        /* Sort and filter info */
        CTAutoFilter autoFilter = ctWorksheet.getAutoFilter();
        CTSortState sortState = ctWorksheet.getSortState();

        if (autoFilter != null) {
            JsonObject filterRangeInfoObj = new JsonObject();
            JsonObject rowFilterInfoObj = new JsonObject();
            JsonObject rowFilterObj = new JsonObject();
            JsonObject filterBtnVisibleInfo = new JsonObject();
            /*Row Filter range*/
            String ref = autoFilter.getRef().replace("$", "");
            AreaReference areaReference = new AreaReference(ref, SpreadsheetVersion.EXCEL2007);
            short startFilterCol = areaReference.getFirstCell().getCol();
            int startFilterRow = areaReference.getFirstCell().getRow();
            short endFilterCol = areaReference.getLastCell().getCol();
            int endFilterRow = areaReference.getLastCell().getRow();
            int columnCount = endFilterCol - startFilterCol;
            /*Put Row Filter range info to json object response */
            filterRangeInfoObj.addProperty("row", startFilterRow + 1);
            filterRangeInfoObj.addProperty("rowCount", endFilterRow - startFilterRow);
            filterRangeInfoObj.addProperty("col", startFilterCol);
            filterRangeInfoObj.addProperty("colCount", columnCount + 1);
            rowFilterInfoObj.add("range", filterRangeInfoObj);
            /* Put row filter button visibility info to json object response */
            for (int i = startFilterCol; i <= endFilterCol; i++) {
                filterBtnVisibleInfo.addProperty(Integer.toString(i), true);
            }
            rowFilterObj.add("range", filterRangeInfoObj);
            filterCondition(autoFilter, rowFilterObj, startFilterCol);
            sortCondition(sortState, rowFilterObj);
            rowFilterObj.add("filterButtonVisibleInfo", filterBtnVisibleInfo);
            rowFilterObj.addProperty("showFilterButton", true);
            sheetMetaData.add("rowFilter", rowFilterObj);
        }
    }

    /**
     * If any table in the sheet then put to the json object response
     */
    private JsonArray tableFormatting(XSSFSheet sheet, JsonObject warnings) {
        JsonArray tableArray = new JsonArray();
        try {
            List<XSSFTable> tables = sheet.getTables();
            int tableSize = tables.size();
            for (int j = 0; j < tableSize; j++) {
                XSSFTable table = tables.get(j);

                JsonObject tableObj = new JsonObject();
                JsonObject filterRngObj = new JsonObject();
                JsonObject rangeObj = new JsonObject();
                JsonObject styleNameObj = new JsonObject();
                JsonObject filterBtnInfoObj = new JsonObject();
                JsonArray tableColArray = new JsonArray();

                CTTable ctTable = table.getCTTable();
                CTTableStyleInfo inputStyleInfo = ctTable.getTableStyleInfo();

                /* Sort and filter info */
                CTSortState sortState = ctTable.getSortState();
                CTAutoFilter autoFilter = ctTable.getAutoFilter();

                /* Basic table info */
                String tableName = table.getName();
                CellReference startCell = table.getStartCellReference();
                CellReference endCell = table.getEndCellReference();
                int startRow = startCell.getRow();
                int startCol = startCell.getCol();
                int tableRowCount = table.getRowCount();
                long tableColCount = (endCell.getCol() + 1) - startCol;

                /* Put table basic info content to json object response */
                if(tableName == null || tableName.equals("")){
                    tableName = "Table"+j;
                }
                tableObj.addProperty("name", tableName);
                tableObj.addProperty("row", startRow);
                tableObj.addProperty("col", startCol);
                tableObj.addProperty("rowCount", tableRowCount + 1);
                tableObj.addProperty("colCount", tableColCount);

                /* Header visibility info*/
                long headerRowCount = ctTable.getHeaderRowCount();
                boolean showFilterBtnInfo;

                if (headerRowCount == 0) {
                    tableObj.addProperty("showHeader", false);
                    showFilterBtnInfo = false;
                } else {
                    showFilterBtnInfo = true;
                }

                /* Table style options info*/
                boolean showColumnStripes, showRowStripes, showFirstColumn, showLastColumn;
                /*
                Covering case:
                    when table style was added to xlsx(through microsoft excel or excellentable) - >
                    import in google spreadsheet - >
                    made changes - >
                    export to xlsx - >
                    import in excellentable
                Convert all the table styles to medium1 which doesn't have proper metadata.
                */
                String styleName;
                if (inputStyleInfo == null) {
                    warnings.addProperty("tableStyle", "com.addteq.confluence.plugin.excellentable.import.style.table.error");
                    showColumnStripes = false;
                    showRowStripes = true;
                    showFirstColumn = true;
                    showLastColumn = true;
                    styleName = "light2";
                } else {
                    showColumnStripes = inputStyleInfo.getShowColumnStripes();
                    showRowStripes = inputStyleInfo.getShowRowStripes();
                    showFirstColumn = inputStyleInfo.getShowFirstColumn();
                    showLastColumn = inputStyleInfo.getShowLastColumn();
                    /* Table style info */
                    styleName = inputStyleInfo.getName();
                    if (styleName != null && styleName.length() >= 10) {
                        styleName = styleName.substring(10);
                    } else {
                        styleName = "";
                    }
                }

                long totalsRowCount = ctTable.getTotalsRowCount();

                /* Put table style options info to json object response */
                if (!showRowStripes) {
                    tableObj.addProperty("bandRows", false);
                }
                if (showColumnStripes) {
                    tableObj.addProperty("bandColumns", true);
                }
                if (showFirstColumn) {
                    tableObj.addProperty("highlightFirstColumn", true);
                }
                if (showLastColumn) {
                    tableObj.addProperty("highlightLastColumn", true);
                }
                if (totalsRowCount == 1) {
                    tableObj.addProperty("showFooter", true);
                }


                /* Put table style info to json object response*/
                styleNameObj.addProperty("buildInName", styleName);
                tableObj.add("style", styleNameObj);

                /* Get Header and footer Information for the Table */
                CTTableColumns columns = table.getCTTable().getTableColumns();
                int tableColArraySize = columns.sizeOfTableColumnArray();
                for (int i = 0; i < tableColArraySize; i++) {
                    JsonObject curentColObj = new JsonObject();
                    CTTableColumn column = columns.getTableColumnArray(i);
                    String totalsRowLabel = column.getTotalsRowLabel();
                    CTTableFormula totalsRowFormula = column.getTotalsRowFormula();
                    long colHeadId = column.getId();
                    String colHeadName = column.getName();

                    /* addProperty table header columns info to json object response*/
                    if (colHeadId > 0) {
                        curentColObj.addProperty("id", colHeadId);
                    }
                    if (colHeadName != null) {
                        curentColObj.addProperty("name", colHeadName);
                    }

                    if (totalsRowLabel != null) {
                        curentColObj.addProperty("footerValue", totalsRowLabel);
                    }
                    if (totalsRowFormula != null) {
//                        curentColObj.add("footerFormula", totalsRowFormula);
                    }

                    /* Put filter button visiblity info to json object response*/
                    if (headerRowCount != 0) {
                        filterBtnInfoObj.addProperty(i + "", true);
                    } else {
                        filterBtnInfoObj.addProperty(i + "", false);
                    }
                    tableColArray.add(curentColObj);
                }

                tableObj.add("columns", tableColArray);
                /* Put table header and filter contents to json object response */
                filterRngObj.addProperty("row", startRow + 1);
                filterRngObj.addProperty("rowCount", tableRowCount);
                filterRngObj.addProperty("col", startCol);
                filterRngObj.addProperty("colCount", tableColCount);
                rangeObj.add("range", filterRngObj);
                filterCondition(autoFilter, rangeObj, startCol);
                sortCondition(sortState, rangeObj);
                rangeObj.add("filterButtonVisibleInfo", filterBtnInfoObj);
                rangeObj.addProperty("showFilterButton", showFilterBtnInfo);
                tableObj.add("rowFilter", rangeObj);

                tableArray.add(tableObj);
            }
        } catch (Exception ex) {
            LOGGER.warn("You may lose some table formatting", ex);
            warnings.addProperty("tableFormatting", "com.addteq.confluence.plugin.excellentable.import.style.tableFormatting.error");
        }
        return tableArray;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    //Import cell styles
    private void cellStyles(Cell cell, JsonObject tempVal, JsonObject tempCol, String cellColumnIndex, JsonObject warnings, HashSet<String> fontNames,
                            XSSFSheet inputSheet) {
        JsonObject borderLeft = new JsonObject();
        JsonObject borderRight = new JsonObject();
        JsonObject borderTop = new JsonObject();
        JsonObject borderBottom = new JsonObject();
        JsonObject cellstyle = new JsonObject();
        if (cell.getCellStyle() != null) {
            try {
                XSSFCellStyle style = (XSSFCellStyle) cell.getCellStyle();
                String formatter = style.getDataFormatString();
                XSSFFont font = style.getFont();
                String fontName = font.getFontName();
                short fontSize = font.getFontHeightInPoints();
                boolean isStrikeOut = font.getStrikeout();
                byte isUnderline = font.getUnderline();
                String fontString = "";
                XSSFColor fontColour = font.getXSSFColor();
                XSSFColor bgColor = (XSSFColor) style.getFillForegroundColorColor();
                short textAlignment = style.getAlignment();
                short verticalAlignment = style.getVerticalAlignment();
                short leftBorderStyle = style.getBorderLeft();
                short rightBorderStyle = style.getBorderRight();
                short topBorderStyle = style.getBorderTop();
                short bottomBorderStyle = style.getBorderBottom();
                short textIndent = style.getIndention();
                String leftBorderColor = null, rightBorderColor = null, topBorderColor = null, bottomBorderColor = null, foreColor = null;
                if (style.getLeftBorderXSSFColor() != null) {
                    leftBorderColor = style.getLeftBorderXSSFColor().getARGBHex();
                }
                if (style.getRightBorderXSSFColor() != null) {
                    rightBorderColor = style.getRightBorderXSSFColor().getARGBHex();
                }
                if (style.getTopBorderXSSFColor() != null) {
                    topBorderColor = style.getTopBorderXSSFColor().getARGBHex();
                }
                if (style.getBottomBorderXSSFColor() != null) {
                    bottomBorderColor = style.getBottomBorderXSSFColor().getARGBHex();
                }
                //Background color
                if ((bgColor != null) && (bgColor.getIndexed() != defaultBGColor)) {//Second condition added because it was going in the loop and not able to get color using method getRGB for some cases.
                    //Link - http://massapi.com/source/jboss/20/60/2060101335/org/concordion/concordion-excel-extension/0.1.2-SNAPSHOT/concordion-excel-extension-0.1.2-20141003.070418-1-sources.jar/concordion-excel-extension-0.1.2-20141003.070418-1-sources.jar.unzip/org/concordion/ext/excel/conversion/cellcontent/DefaultStyleConverter.java.html#57
                    try {
                        byte[] rgb = bgColor.getARGB();
                        String rgbString;
                        rgbString = (rgb != null)?bytesToHex(rgb):"ffffffff";//default bg color -> white
                        cellstyle.addProperty("backColor", "#" + rgbString.substring(2));
                        tempVal.add("style", cellstyle);
                        tempCol.add(cellColumnIndex, tempVal);
                    } catch (Exception ex) {
                        logAndUpdateWarnings(
                                warnings,
                                "You may lose cell background color",
                                ex,
                                "backgroundColor",
                                "com.addteq.confluence.plugin.excellentable.import.style.backgroundColor.error",
                                LOGGER
                        );
                    }
                }
                //Font color
                if ((fontColour != null) && (fontColour.getARGBHex() != null)) {
                    try {
                        foreColor = "#" + fontColour.getARGBHex().substring(2);
                        cellstyle.addProperty("foreColor", foreColor);
                        tempVal.add("style", cellstyle);
                        tempCol.add(cellColumnIndex, tempVal);
                    } catch (Exception ex) {
                        logAndUpdateWarnings(
                                warnings,
                                "You may lose some font color",
                                ex,
                                "fontColor",
                                "com.addteq.confluence.plugin.excellentable.import.style.fontColor.error",
                                LOGGER
                        );
                    }
                }
                //Bold,italic,font size and font style
                addBoldItalicFontSizeAndStyleToCell(tempVal, tempCol, cellColumnIndex, warnings, fontNames, cellstyle, fontName, fontSize, fontString, font.getItalic(), font.getBold(), MIN_FONT_SIZE, MAX_FONT_SIZE, LOGGER);
                //strikeout and underline
                addStrikeoutAndUnderlineToCell(tempVal, tempCol, cellColumnIndex, warnings, cellstyle, isStrikeOut, isUnderline, LOGGER);
                //Horizontal alignment
                addHorizontolAlignmentToCell(tempVal, tempCol, cellColumnIndex, warnings, cellstyle, textAlignment, LOGGER);
                //vertical alignment
                addVerticalAlignmentToCell(tempVal, tempCol, cellColumnIndex, warnings, cellstyle, verticalAlignment, LOGGER);
                //Wordwrap
                addWordwrapToCell(tempVal, tempCol, cellColumnIndex, warnings, cellstyle, style.getWrapText(), LOGGER);
                //Formatter
                addFormattingToCell(tempVal, tempCol, cellColumnIndex, warnings, cellstyle, formatter, LOGGER);
                //Indentation
                addIndentationToCell(tempVal, tempCol, cellColumnIndex, warnings, cellstyle, textIndent, LOGGER);
                //Border
                try {
                    //left border
                    if (leftBorderStyle != 0 && leftBorderColor != null) {
                        borderLeft.addProperty("color", "#" + leftBorderColor.substring(2));
                        for (ExcBorderStyle borderStyle : ExcBorderStyle.values()) {
                            if ((borderStyle.toString()).equals(style.getBorderLeftEnum().toString())) {
                                borderLeft.addProperty("style", borderStyle.getExcBorderStyle());
                                cellstyle.add("borderLeft", borderLeft);
                                break;
                            }
                        }
                    } else if (leftBorderStyle != 0 && leftBorderColor == null) {  //handle scenario for excel and libre office file with left border style
                        borderLeft.addProperty("color", "#000000");
                        borderLeft.addProperty("style", leftBorderStyle);
                        cellstyle.add("borderLeft", borderLeft);
                    }
                    //Right border
                    if (rightBorderStyle != 0 && rightBorderColor != null) {
                        borderRight.addProperty("color", "#" + rightBorderColor.substring(2));
                        for (ExcBorderStyle borderStyle : ExcBorderStyle.values()) {
                            if ((borderStyle.toString()).equals(style.getBorderRightEnum().toString())) {
                                borderRight.addProperty("style", borderStyle.getExcBorderStyle());
                                cellstyle.add("borderRight", borderRight);
                                break;
                            }
                        }
                    } else if (rightBorderStyle != 0 && rightBorderColor == null) {  //handle scenario for excel and libre office file with right border style
                        borderRight.addProperty("color", "#000000");
                        borderRight.addProperty("style", rightBorderStyle);
                        cellstyle.add("borderRight", borderRight);
                    }
                    //Top border
                    if (topBorderStyle != 0 && topBorderColor != null) {
                        borderTop.addProperty("color", "#" + topBorderColor.substring(2));
                        for (ExcBorderStyle borderStyle : ExcBorderStyle.values()) {
                            if ((borderStyle.toString()).equals(style.getBorderTopEnum().toString())) {
                                borderTop.addProperty("style", borderStyle.getExcBorderStyle());
                                cellstyle.add("borderTop", borderTop);
                                break;
                            }
                        }
                    } else if (topBorderStyle != 0 && topBorderColor == null) {  //handle scenario for excel and libre office file with top border style
                        borderTop.addProperty("color", "#000000");
                        borderTop.addProperty("style", topBorderStyle);
                        cellstyle.add("borderTop", borderTop);
                    }
                    //Bottom border
                    if (bottomBorderStyle != 0 && bottomBorderColor != null) {
                        borderBottom.addProperty("color", "#" + bottomBorderColor.substring(2));
                        for (ExcBorderStyle borderStyle : ExcBorderStyle.values()) {
                            if ((borderStyle.toString()).equals(style.getBorderBottomEnum().toString())) {
                                borderBottom.addProperty("style", borderStyle.getExcBorderStyle());
                                cellstyle.add("borderBottom", borderBottom);
                                break;
                            }
                        }
                    } else if (bottomBorderStyle != 0 && bottomBorderColor == null) {  //handle scenario for excel and libre office file with bottom border style
                        borderBottom.addProperty("color", "#000000");
                        borderBottom.addProperty("style", bottomBorderStyle);
                        cellstyle.add("borderBottom", borderBottom);
                    }
                    tempVal.add("style", cellstyle);
                    tempCol.add(cellColumnIndex, tempVal);

                } catch (Exception ex) {
                    logAndUpdateWarnings(
                            warnings,
                            "You may lose some border style",
                            ex,
                            "border",
                            "com.addteq.confluence.plugin.excellentable.import.style.borders.error",
                            LOGGER
                    );
                }
                //Hyperlink
                try {
                    if (cell.getHyperlink() != null) {
                        JsonObject linkObj = new JsonObject();
                        XSSFHyperlink hyperLink = (XSSFHyperlink) cell.getHyperlink();
                        String url = hyperLink.getAddress();
                        String label = cell.getStringCellValue();
                        tempVal.addProperty("value", url);
                        linkObj.addProperty("typeName", HYPERLINK_TYPE_NAME);
                        linkObj.addProperty("linkColor", foreColor);
                        linkObj.addProperty("text", label);
                        linkObj.addProperty("linkToolTip", url);
                        cellstyle.add("cellType", linkObj);
                        tempVal.add("style", cellstyle);
                        tempCol.add(cellColumnIndex, tempVal);
                    }
                } catch (Exception ex) {
                    logAndUpdateWarnings(
                            warnings,"You may lose some cell hyperlink",
                            ex,
                            "hyperlink",
                            "com.addteq.confluence.plugin.excellentable.import.style.hyperlink.error",
                            LOGGER
                    );
                }

                /* EXC-4754 If the cellstyle has "locked" and the sheet is protected, increment both protected cell as well as number of data
                   cells counters. Else increment only the number of data cells counters.
                 */
                if ((style.getLocked()) && (inputSheet.isSheetLocked())) {
                    cellstyle.addProperty("locked", "protected");
                    protectedCellCountPerSheet++;
                }
                numberOfDataCellsInSheet++;

            } catch (Exception e) {
                LOGGER.warn("You may loose some cell styling.", e);
            }


        }
    }
}
