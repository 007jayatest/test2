package com.addteq.service.excellentable.exc_io.importfile.impl;

import com.addteq.service.excellentable.exc_io.importfile.Importable;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.poi.hssf.OldExcelFormatException;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.PaneInformation;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.addteq.service.excellentable.exc_io.importfile.impl.ImportLimits.MAX_CELL_LIMIT;

/**
 * @author neeraj bodhe
 */
public class ImportXls implements Importable {

    List<HSSFPictureData> hssfPictureData = new ArrayList<HSSFPictureData>();
    int pictCount = 0, rowCount = 0, colCount = 0;
    static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ImportXls.class);
    private static final short MIN_FONT_SIZE = 6;
    private static final short MAX_FONT_SIZE = 72;
    private static final String HYPERLINK_TYPE_NAME = "8";  //Cell type name for hyperlink in spreadjs
    private static final int MIN_ROW_COUNT = 99;  //Minimum number of rows in sheet
    private static final int MIN_COL_COUNT = 25; //Minimum number of column in sheet
    private static final int TWIPS_TO_PX = 15;  //Twips to pixel conversion (1 pixel = 15 twips)
    private static final int CHAR_TO_PX = 8;   //characters to pixel conversion (1character = 8 pixel)
    private static final String GRIDLINE_COLOR = "rgb(212, 212, 212)";
    private static final int DEFAULT_ROW_HEIGHT = 25;
    private static final int DEFAULT_COLUMN_WIDTH = 150;
    private JsonArray floatingObjects = new JsonArray();

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
    public JsonObject buildImportSheetJson(Object sheetData, String version)  {
        JsonObject spread = new JsonObject();
        JsonObject outputSheets = new JsonObject();
        JsonObject errorObj = new JsonObject();
        HSSFSheet inputSheet;
        int totalCells = 0;
        try {
            //Set Default Values For Each Sheet
            HSSFWorkbook wb = (HSSFWorkbook) sheetData;
            int numberOfSheets = wb.getNumberOfSheets();
            spread.addProperty("version", version);
            spread.add("floatingObjects", new JsonObject());
            //Get each sheet and save it to outputSheets object
            for (int i = 0; i < numberOfSheets; i++) {
                inputSheet = wb.getSheetAt(i);
                //Implemented Counter to limit the size of imports on the basis of cell
                totalCells += getNumberOfCellsInSheet(inputSheet);
                if (totalCells > MAX_CELL_LIMIT) throw new SheetLimitExceededException("Sheet Limit Exceeded");
                outputSheets.add("Sheet" + Integer.toString(i + 1), importSheet(inputSheet, errorObj, wb));
            }
            spread.add("sheets", outputSheets);

        } catch (OldExcelFormatException olfe) {
            LOGGER.error(olfe.getMessage());
            errorObj.addProperty("firstSheetExceeded", "com.addteq.confluence.plugin.excellentable.import.oldFormat.error");
        } catch (OfficeXmlFileException | ClassCastException cce) {
            LOGGER.error(cce.getMessage());
            errorObj.addProperty("firstSheetExceeded", "com.addteq.confluence.plugin.excellentable.import.fileExtension.error");
        } catch (SheetLimitExceededException ex) {
            LOGGER.warn("You are trying to import a sheet that is more than the specified limit of 250000 cells, hence aborting the import");
            ImportXlsx.putError(spread, outputSheets, errorObj, ex);
        }
        spread.add("errorData", errorObj);
        spread.addProperty("totalCells", totalCells);
        return spread;
    }

    /**
     * Need to Override this method so as to return the parsed json for spreadsheet import
     *
     * @param inputSheet
     * @return
     * @throws Exception
     */
    public JsonObject importSheet(HSSFSheet inputSheet, JsonObject errorObj, HSSFWorkbook wb) {
        JsonObject outputSheet = new JsonObject();
        JsonObject selections = new JsonObject();
        JsonObject zeroSelection = new JsonObject();
        JsonObject rowFilter = new JsonObject();
        JsonObject dataTable = new JsonObject();
        JsonArray commentsArray = new JsonArray();
        JsonArray spansArray = new JsonArray();
        JsonArray rowArray = new JsonArray();
        JsonObject defaults = new JsonObject();
        HashSet<String> fontNames = new HashSet<>();
        int defaultRowHeight = inputSheet.getDefaultRowHeight() / TWIPS_TO_PX; //Getting default row height and converting the height from twips to pixel
        int defaultColumnWidth = inputSheet.getDefaultColumnWidth() * CHAR_TO_PX; //Getting default column width and converting the height from characters to pixel
        
        defaultRowHeight = (defaultRowHeight == 0) ? DEFAULT_ROW_HEIGHT : defaultRowHeight;
        defaultColumnWidth = (defaultColumnWidth == 0) ? DEFAULT_COLUMN_WIDTH : defaultColumnWidth;
        
        outputSheet.addProperty("name", inputSheet.getSheetName());                           // Name of the sheet

        zeroSelection.addProperty("row", 0);
        zeroSelection.addProperty("rowCount", 1);
        zeroSelection.addProperty("col", 0);
        zeroSelection.addProperty("colCount", 1);

        selections.add("0", zeroSelection);
        defaults.addProperty("rowHeight", defaultRowHeight);   //Set default height of the sheet
        defaults.addProperty("colWidth", defaultColumnWidth);  //Set default width of the sheet
        defaults.addProperty("rowHeaderColWidth", 40);
        defaults.addProperty("colHeaderRowHeight", 20);

        outputSheet.add("selections", selections);                 // present selections if any
        outputSheet.add("defaults", defaults);

        zeroSelection = new JsonObject();
        zeroSelection.addProperty("row", 0);
        zeroSelection.addProperty("rowCount", 0);
        zeroSelection.addProperty("col", 0);
        zeroSelection.addProperty("colCount", 0);

        rowFilter.add("range", zeroSelection);
        rowFilter.add("filterButtonVisibleInfo", new JsonObject());
        rowFilter.addProperty("showFilterButton", false);

        outputSheet.add("rowFilter", rowFilter);                            // fow filter if any

        JsonArray colArray = new JsonArray();
        JsonObject cols = new JsonObject();
        cols.addProperty("resizable", true);
        cols.add("dirty", new JsonObject());
        outputSheet.addProperty("activeRow", 0);                            // Active row in the sheet
        outputSheet.addProperty("activeCol", 0);                             // Active col in the row
        for (int i = 0; i < inputSheet.getNumMergedRegions(); i++) {
            CellRangeAddress range = inputSheet.getMergedRegion(i);
            int startRow = range.getFirstRow();
            int startColomn = range.getFirstColumn();
            JsonObject spans = new JsonObject();
            spans.addProperty("row", startRow);
            spans.addProperty("rowCount", range.getLastRow() + 1 - startRow);
            spans.addProperty("col", startColomn);
            spans.addProperty("colCount", range.getLastColumn() + 1 - startColomn);
            spansArray.add(spans);
        }
        outputSheet.add("spans", spansArray);

        outputSheet.addProperty("index", 0);

        JsonObject dataObjectTemp = new JsonObject();
        int rowStart = Math.min(0, inputSheet.getFirstRowNum());
        int rowEnd = Math.max(1400, inputSheet.getLastRowNum());

        //Iterate through each rows from first sheet
        for (int rowIndex = rowStart; rowIndex < rowEnd; rowIndex++) {
            JsonObject rowObj = new JsonObject();
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

            JsonObject tempVal;

            JsonObject tempCol = new JsonObject();

            int lastColumn = Math.max(row.getLastCellNum(), MIN_COL_COUNT);
            for (int colIndex = 0; colIndex <= lastColumn; colIndex++) {

                //Get cell having data or blank cell
                Cell cell = row.getCell(colIndex, Row.RETURN_NULL_AND_BLANK);
                JsonObject colObj = new JsonObject();
                int colWidth = Math.round(inputSheet.getColumnWidthInPixels(colIndex));
                colWidth = (colWidth== 0) ? DEFAULT_COLUMN_WIDTH : colWidth;
                if (colArray.size() >= colIndex) {
                    colObj.addProperty("resizable", true);
                    colObj.addProperty("size", colWidth);
                    colArray.set(colIndex, colObj);
                }
                if (cell == null) {
                    continue;
                }
                if (cell.getCellComment() != null) {
                    JsonObject commentObj = new JsonObject();
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
                                JsonObject formatterJSON = new JsonObject(), autoFormatter = new JsonObject();
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
                        if (!formatter.equals("") && !formatter.toUpperCase().equals("GENERAL")) {
                            if (DateUtil.isCellDateFormatted(cell)) { //If the cell is in DateTime format.

                                JsonObject formatterJSON = new JsonObject(), autoFormatter = new JsonObject();
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
                cellStyles(cell, wb, tempVal, tempCol, cellColumnIndex, errorObj, fontNames);
                if (colCount < cell.getColumnIndex()) {
                    colCount = cell.getColumnIndex();
                }
            }
            rowCount = row.getRowNum();
            dataTable.add(Integer.toString(row.getRowNum()), tempCol);
        }
        dataObjectTemp.add("dataTable", dataTable);

        hssfPictureData = inputSheet.getWorkbook().getAllPictures();
        JsonArray floatingObjArray = loadSheetImages(inputSheet, errorObj);
        if (colCount < MIN_COL_COUNT) {      // Minimum size of the grid should be of 26 cols
            colCount = MIN_COL_COUNT;
        }

        if (rowCount < MIN_ROW_COUNT) {    //Minimum size of grid should be of 100 rows
            rowCount = MIN_ROW_COUNT;
        }
        //Frozen pane
        PaneInformation paneInformation = inputSheet.getPaneInformation();
        addPaneInformationToSheet(outputSheet, paneInformation);
        //sheet without gridlines
        addGridlinesToSheet(outputSheet, inputSheet.isDisplayGridlines(), GRIDLINE_COLOR);

        outputSheet.add("columns", colArray);                              // the columns in the sheet
        outputSheet.add("rows", rowArray);                                 // the rows in the sheet
        outputSheet.addProperty("rowCount", rowCount + 1);                            // the row count in the sheet
        outputSheet.addProperty("columnCount", colCount + 1);                          // the column count in the sheet
        outputSheet.add("comments", commentsArray);
        outputSheet.add("data", dataObjectTemp);                // All the cell data

        outputSheet.add("floatingObjects", floatingObjArray);   // floating objects i.e images of the sheet


        return outputSheet;


    }

    /**
     * Loads images for the currently active sheet and adds them to the target
     * Spreadsheet.
     * Link - http://www.programcreek.com/java-api-examples/index.php?api=org.apache.poi.hssf.usermodel.HSSFClientAnchor
     *
     * @param sheet - Sheet from which images are to be imported
     * @param errorObj - ErrorObject to addProperty error into
     */
    private JsonArray loadSheetImages(final Sheet sheet, JsonObject errorObj) {
        int i=0;
        JsonArray floatingObjectsArray = new JsonArray();
        Drawing drawing = ((HSSFSheet) sheet).getDrawingPatriarch();
        if (drawing instanceof HSSFPatriarch) {
            for (HSSFShape shape : ((HSSFPatriarch) drawing).getChildren()) {
                JsonObject floatingObj = new JsonObject();
                if (shape instanceof HSSFPicture) {
                    HSSFClientAnchor anchor = (HSSFClientAnchor) shape
                            .getAnchor();
                    HSSFPictureData pictureData = ((HSSFPicture) shape)
                            .getPictureData();
                    //Formatting Data
                    String ext = pictureData.suggestFileExtension();
                    byte[] picData = pictureData.getData();
                    String mimeType = pictureData.getMimeType();
                    StringBuilder sb = new StringBuilder();
                    sb.append("data:").append(mimeType).append(";base64,");
                    sb.append(StringUtils.newStringUtf8(Base64.encodeBase64(picData, false)));
                    String img = sb.toString();
                    String floatingObjName = "floatingObject" + i;
                    i++;
                    //Saving Data to JSON Object
                    floatingObj.addProperty("name", floatingObjName);
                    floatingObj.addProperty("src", img);
                    if (anchor != null) {
                        floatingObj.addProperty("startColumn", anchor.getCol1());
                        floatingObj.addProperty("startRow", anchor.getRow1());
                        floatingObj.addProperty("endColumn", anchor.getCol2() + 1);
                        floatingObj.addProperty("endRow", anchor.getRow2() + 1);
                    } else {
                        //IMAGE WITHOUT ANCHOR i.e without location
                        floatingObj.addProperty("startColumn", 1);
                        floatingObj.addProperty("startRow", 1);
                        floatingObj.addProperty("endColumn", 5);
                        floatingObj.addProperty("endRow", 5);
                        LOGGER.info("No location for the image was found so it was moved to 1,1 cell location");
                        errorObj.addProperty("imageLocation", "com.addteq.confluence.plugin.excellentable.import.style.imageLocation.error");
                    }
                    if (ext.equals("")){
                        LOGGER.info("The picture type extension in the workbook is not supported in the xls format. " +
                                "It will be not imported." +
                                "Supported formats are emf, wmf, pict, jpeg, png, dib. ");
                        errorObj.addProperty("imageFormat", "com.addteq.confluence.plugin.excellentable.import.style.imageExtension.error");
                        continue;
                    }
                    floatingObjectsArray.add(floatingObj);
                } else if(shape instanceof HSSFShapeGroup) {
                    errorObj.addProperty("ShapeNotImported", "com.addteq.confluence.plugin.excellentable.import.style.shapes.error");
                }
            }
        }
        return floatingObjectsArray;
    }

    //Import cell styles
    private void cellStyles(Cell cell, HSSFWorkbook wb, JsonObject tempVal, JsonObject tempCol, String cellColumnIndex, JsonObject errorObj, HashSet<String> fontNames) {
        JsonObject borderLeft = new JsonObject();
        JsonObject borderRight = new JsonObject();
        JsonObject borderTop = new JsonObject();
        JsonObject borderBottom = new JsonObject();
        JsonObject cellstyle = new JsonObject();
        if (cell.getCellStyle() != null) {
            HSSFCellStyle style = (HSSFCellStyle) cell.getCellStyle();
            String formatter = style.getDataFormatString();
            HSSFFont font = style.getFont(wb);
            String fontName = font.getFontName();
            short fontSize = font.getFontHeightInPoints();
            boolean isStrikeOut = font.getStrikeout();
            byte isUnderline = font.getUnderline();
            String fontString = "";
            short[] fontColorTriplet = null;
            short[] backColorTriplet = null;
            short textAlignment = style.getAlignment();
            short verticalAlignment = style.getVerticalAlignment();
            HSSFPalette customPallet = wb.getCustomPalette();
            short lColor = style.getLeftBorderColor();
            short rColor = style.getRightBorderColor();
            short tColor = style.getTopBorderColor();
            short bColor = style.getBottomBorderColor();
            short leftBorderStyle = style.getBorderLeft();
            short rightBorderStyle = style.getBorderRight();
            short topBorderStyle = style.getBorderTop();
            short bottomBorderStyle = style.getBorderBottom();
            short textIndent = style.getIndention();
            short[] lColorTriplet = null, rColorTriplet = null, tColorTriplet = null, bColorTriplet = null;
            String fontColor = null;
            if (customPallet.getColor(lColor) != null) {
                lColorTriplet = customPallet.getColor(lColor).getTriplet();
            }
            if (customPallet.getColor(rColor) != null) {
                rColorTriplet = customPallet.getColor(rColor).getTriplet();
            }
            if (customPallet.getColor(tColor) != null) {
                tColorTriplet = customPallet.getColor(tColor).getTriplet();
            }
            if (customPallet.getColor(bColor) != null) {
                bColorTriplet = customPallet.getColor(bColor).getTriplet();
            }
            //Background color
            try {
                backColorTriplet = style.getFillForegroundColorColor().getTriplet();
                if (!(backColorTriplet[0] == 0 && backColorTriplet[1] == 0 && backColorTriplet[2] == 0)) {
                    String backColor = String.format("#%02x%02x%02x", backColorTriplet[0], backColorTriplet[1], backColorTriplet[2]);
                    cellstyle.addProperty("backColor", backColor);
                    tempVal.add("style", cellstyle);
                    tempCol.add(cellColumnIndex, tempVal);
                }
            } catch (Exception ex) {
                LOGGER.warn("You may lose background color of cell", ex);
                errorObj.addProperty("backgroundColor", "com.addteq.confluence.plugin.excellentable.import.style.backgroundColor.error");
            }
            //Font color
            if (font.getHSSFColor(wb) != null) {
                try {
                    fontColorTriplet = font.getHSSFColor(wb).getTriplet();
                    fontColor = String.format("#%02x%02x%02x", fontColorTriplet[0], fontColorTriplet[1], fontColorTriplet[2]);
                    cellstyle.addProperty("foreColor", fontColor);
                    tempVal.add("style", cellstyle);
                    tempCol.add(cellColumnIndex, tempVal);
                } catch (Exception ex) {
                    LOGGER.warn("You may lose text color", ex);
                    errorObj.addProperty("fontColor", "com.addteq.confluence.plugin.excellentable.import.style.fontColor.error");
                }
            }
            //Bold,italic,font size and font style
            addBoldItalicFontSizeAndStyleToCell(tempVal, tempCol, cellColumnIndex, errorObj, fontNames, cellstyle, fontName, fontSize, fontString, font.getItalic(), font.getBold(), MIN_FONT_SIZE, MAX_FONT_SIZE, LOGGER);
            //strikethrough and underline
            addStrikeoutAndUnderlineToCell(tempVal, tempCol, cellColumnIndex, errorObj, cellstyle, isStrikeOut, isUnderline, LOGGER);
            //Horizontal alignment
            addHorizontolAlignmentToCell(tempVal, tempCol, cellColumnIndex, errorObj, cellstyle, textAlignment, LOGGER);
            //vertical alignment
            addVerticalAlignmentToCell(tempVal, tempCol, cellColumnIndex, errorObj, cellstyle, verticalAlignment, LOGGER);
            //Wordwrap
            addWordwrapToCell(tempVal, tempCol, cellColumnIndex, errorObj, cellstyle, style.getWrapText(), LOGGER);
            //Formatter
            addFormattingToCell(tempVal, tempCol, cellColumnIndex, errorObj, cellstyle, formatter, LOGGER);
            //Indentation
            addIndentationToCell(tempVal, tempCol, cellColumnIndex, errorObj, cellstyle, textIndent, LOGGER);
            //Border
            try {
                //Left border
                if (leftBorderStyle != 0 && lColorTriplet != null) {
                    String leftColor = String.format("#%02x%02x%02x", lColorTriplet[0], lColorTriplet[1], lColorTriplet[2]);
                    borderLeft.addProperty("color", leftColor);
                    for (ExcBorderStyle borderStyle : ExcBorderStyle.values()) {
                        if ((borderStyle.getExcBorderStyle()) == ((int) leftBorderStyle)) {
                            borderLeft.addProperty("style", borderStyle.getExcBorderStyle());
                            cellstyle.add("borderLeft", borderLeft);
                            break;
                        }
                    }
                } else if (leftBorderStyle != 0 && lColorTriplet == null) {  //handle scenario for excel and libre office file with left border style
                    borderLeft.addProperty("color", "#000000");
                    borderLeft.addProperty("style", leftBorderStyle);
                    cellstyle.add("borderLeft", borderLeft);
                }
                //Right border
                if (rightBorderStyle != 0 && rColorTriplet != null) {
                    String rightColor = String.format("#%02x%02x%02x", rColorTriplet[0], rColorTriplet[1], rColorTriplet[2]);
                    borderRight.addProperty("color", rightColor);
                    for (ExcBorderStyle borderStyle : ExcBorderStyle.values()) {
                        if ((borderStyle.getExcBorderStyle()) == ((int) rightBorderStyle)) {
                            borderRight.addProperty("style", borderStyle.getExcBorderStyle());
                            cellstyle.add("borderRight", borderRight);
                            break;
                        }
                    }
                } else if (rightBorderStyle != 0 && rColorTriplet == null) {  //handle scenario for excel and libre office file with right border style
                    borderRight.addProperty("color", "#000000");
                    borderRight.addProperty("style", rightBorderStyle);
                    cellstyle.add("borderRight", borderRight);
                }
                //Top border
                if (topBorderStyle != 0 && tColorTriplet != null) {
                    String topColor = String.format("#%02x%02x%02x", tColorTriplet[0], tColorTriplet[1], tColorTriplet[2]);
                    borderTop.addProperty("color", topColor);
                    for (ExcBorderStyle borderStyle : ExcBorderStyle.values()) {
                        if ((borderStyle.getExcBorderStyle()) == ((int) topBorderStyle)) {
                            borderTop.addProperty("style", borderStyle.getExcBorderStyle());
                            cellstyle.add("borderTop", borderTop);
                            break;
                        }
                    }
                } else if (topBorderStyle != 0 && tColorTriplet == null) {  //handle scenario for excel and libre office file with top border style
                    borderTop.addProperty("color", "#000000");
                    borderTop.addProperty("style", topBorderStyle);
                    cellstyle.add("borderTop", borderTop);
                }
                //Bottom border
                if (bottomBorderStyle != 0 && bColorTriplet != null) {
                    String bottomColor = String.format("#%02x%02x%02x", bColorTriplet[0], bColorTriplet[1], bColorTriplet[2]);
                    borderBottom.addProperty("color", bottomColor);
                    for (ExcBorderStyle borderStyle : ExcBorderStyle.values()) {
                        if ((borderStyle.getExcBorderStyle()) == ((int) bottomBorderStyle)) {
                            borderBottom.addProperty("style", borderStyle.getExcBorderStyle());
                            cellstyle.add("borderBottom", borderBottom);
                            break;
                        }
                    }
                } else if (bottomBorderStyle != 0 && bColorTriplet == null) {  //handle scenario for excel and libre office file with bottom border style
                    borderBottom.addProperty("color", "#000000");
                    borderBottom.addProperty("style", bottomBorderStyle);
                    cellstyle.add("borderBottom", borderBottom);
                }
                tempVal.add("style", cellstyle);
                tempCol.add(cellColumnIndex, tempVal);

            } catch (Exception ex) {
                LOGGER.warn("You may lose some border style", ex);
                errorObj.addProperty("border", "com.addteq.confluence.plugin.excellentable.import.style.borders.error");
            }
            //Hyperlink
            try {
                if (cell.getHyperlink() != null) {
                    JsonObject linkObj = new JsonObject();
                    HSSFHyperlink hyperLink = (HSSFHyperlink) cell.getHyperlink();
                    String url = hyperLink.getAddress();
                    String label = cell.getStringCellValue();
                    tempVal.addProperty("value", url);
                    linkObj.addProperty("typeName", HYPERLINK_TYPE_NAME);
                    linkObj.addProperty("linkColor", fontColor);
                    linkObj.addProperty("text", label);
                    linkObj.addProperty("linkToolTip", url);
                    cellstyle.add("cellType", linkObj);
                    tempVal.add("style", cellstyle);
                    tempCol.add(cellColumnIndex, tempVal);
                }
            } catch (Exception ex) {
                LOGGER.warn("You may lose some cell hyperlink", ex);
                errorObj.addProperty("hyperlink", "com.addteq.confluence.plugin.excellentable.import.style.hyperlink.error");
            }
        }
    }
}
