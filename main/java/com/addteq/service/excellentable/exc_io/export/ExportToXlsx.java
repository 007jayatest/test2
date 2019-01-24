package com.addteq.service.excellentable.exc_io.export;


import com.addteq.service.excellentable.exc_io.spreadjs.Fonts;
import com.addteq.service.excellentable.exc_io.utils.ColorUtil;
import com.addteq.service.excellentable.exc_io.utils.ETDateUtils;
import com.atlassian.confluence.pages.Attachment;
import com.atlassian.confluence.pages.AttachmentManager;
import com.atlassian.fugue.Maybe;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.*;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class ExportToXlsx {

    private final XSSFWorkbook workbook;
    static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ExportToXlsx.class);
    private static final int PX_TO_TWIPS = 15;  //Pixel to twips conversion (1 pixel = 15 twips)
    private static final int PX_TO_CHAR = 8;   //Pixel to character conversion (1character = 8 pixel)
    private static final double PX_TO_PT = 0.75; //1px equals 3/4pt (0.75pt)
    private static final String HEX_PATTERN = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$";
    private static final String RGB_PATTERN = "rgb\\(([0-9]+),([0-9]+),([0-9]+)\\)/gi";
    private static final ColorUtil colorUtil = new ColorUtil();
    private Pattern hexPattern;
    private Pattern rgbPattern;
    private Matcher matcher;

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    private String baseUrl;

    public AttachmentManager getAttachmentManager() {
        return attachmentManager;
    }

    public void setAttachmentManager(AttachmentManager attachmentManager) {
        this.attachmentManager = attachmentManager;
    }

    private AttachmentManager attachmentManager;


    public ExportToXlsx() {
        workbook = new XSSFWorkbook();
        hexPattern = Pattern.compile(HEX_PATTERN);
        rgbPattern = Pattern.compile(RGB_PATTERN);
    }

    public XSSFWorkbook createWorkbook(JsonObject jsonObj) throws IOException {
        this.sheet(jsonObj.getAsJsonObject("sheets"));
        return workbook;
    }

    public void sheet(JsonObject sheetObj) throws IOException {
        try {
            for (Map.Entry<String, JsonElement> entry : sheetObj.entrySet()) {
                String sheetName = entry.getKey(); //get sheet Name
                XSSFSheet xssfSheet = workbook.createSheet(sheetName); //Create new sheet in workbook
                JsonObject sheet = sheetObj.getAsJsonObject(sheetName); //get sheet data
                short defaultRowHeight = 10;
                short defaultColWidth = 10;
                if (sheet.getAsJsonObject("defaults") != null) {
                    defaultRowHeight = (short) (sheet.getAsJsonObject("defaults").get("rowHeight").getAsShort() * PX_TO_TWIPS);
                    defaultColWidth = (short) (sheet.getAsJsonObject("defaults").get("colWidth").getAsShort()/PX_TO_CHAR);
                }
                xssfSheet.setDefaultRowHeight(defaultRowHeight);
                xssfSheet.setDefaultColumnWidth(defaultColWidth);

                if (sheet.has("gridline")) {
                    boolean hasHorizontalGridline = sheet.getAsJsonObject("gridline").get("showHorizontalGridline").getAsBoolean();
                    boolean hasVerticalGridline = sheet.getAsJsonObject("gridline").get("showVerticalGridline").getAsBoolean();
                    if (!hasHorizontalGridline && !hasVerticalGridline) {
                        xssfSheet.setDisplayGridlines(false);
                    }
                }

                //For frozen Pane
                if (sheet.has("frozenColCount") || sheet.has("frozenRowCount")) {
                    try {
                        int frozenColCount = 0;
                        int frozenRowCount = 0;
                        if (sheet.get("frozenColCount") != null) {
                            frozenColCount = sheet.get("frozenColCount").getAsInt();
                        }
                        if (sheet.get("frozenRowCount") != null) {
                            frozenRowCount = sheet.get("frozenRowCount").getAsInt();
                        }
                        xssfSheet.createFreezePane(frozenColCount, frozenRowCount);
                    } catch (Exception ex) {
                        LOGGER.warn("You may lose frozen pane" + ex);
                    }
                }
                int columnCount = sheet.get("columnCount").getAsInt();
                if (sheet.has("data")) {
                    try {
                        JsonObject dataTable = sheet.getAsJsonObject("data").getAsJsonObject("dataTable");
                        data(xssfSheet, dataTable);
                    } catch (Exception ex) {
                        LOGGER.warn("You may lose some data" + ex);
                    }
                }
                if (sheet.has("spans")) {
                    try {
                        JsonArray spansArray = sheet.getAsJsonArray("spans");
                        spans(xssfSheet, spansArray);
                    } catch (Exception ex) {
                        LOGGER.warn("You may lose cell merges" + ex);
                    }
                }
                if (sheet.has("rows")) {
                    try {
                        JsonArray rowsArray = sheet.getAsJsonArray("rows");
                        rows(xssfSheet, rowsArray);
                    } catch (Exception ex) {
                        LOGGER.warn("You may lose rows formatting" + ex);
                    }
                }
                if (sheet.has("columns")) {
                    try {
                        JsonArray columnsArray = sheet.getAsJsonArray("columns");
                        columns(xssfSheet, columnsArray);
                    } catch (Exception ex) {
                        LOGGER.warn("You may lose column formatting" + ex);
                    }
                }
                if (sheet.has("comments")) {
                    try {
                        JsonArray commentsArray = sheet.getAsJsonArray("comments");
                        comment(xssfSheet, commentsArray);
                    } catch (Exception ex) {
                        LOGGER.warn("You may lose comments" + ex);
                    }
                }
                if (sheet.has("floatingObjects")) {
                    try {
                        JsonArray floatingObjects = sheet.getAsJsonArray("floatingObjects");
                        floatingObjects(xssfSheet, floatingObjects, columnCount);
                    } catch (Exception ex) {
                        LOGGER.warn("You may lose floating objects" + ex);
                    }
                }
                if (sheet.has("tables")) {
                    try {
                        JsonArray tablesArray = sheet.getAsJsonArray("tables");
                        tables(xssfSheet, tablesArray);
                    } catch (Exception ex) {
                        LOGGER.warn("You may lose table formatting : " + ex);
                    }
                }
            }

            //Set the order of the sheet
            for (Map.Entry<String, JsonElement> entry : sheetObj.entrySet()) {
                String sheetName = entry.getKey();
                JsonObject sheet = sheetObj.getAsJsonObject(sheetName);
                if (sheet.has("index")) {
                    int sheetIndex = sheet.get("index").getAsInt();
                    workbook.setSheetOrder(sheetName, sheetIndex);
                }
            }
        } catch (Exception ex) {
            LOGGER.warn("Error occurred while exporting to xlsx", ex);
        }
    }

    public void data(XSSFSheet xssfSheet, JsonObject dataTable) {
        Set<Map.Entry<String, JsonElement>> entrySet = dataTable.entrySet();
        Iterator<Map.Entry<String, JsonElement>> rowIterator = entrySet.iterator();
        while (rowIterator.hasNext()) {
            Map.Entry<String, JsonElement> rowObj = rowIterator.next();
            JsonObject currentRow = (JsonObject) rowObj.getValue();
            XSSFRow row = xssfSheet.createRow(Integer.parseInt(rowObj.getKey()));
            Iterator<Map.Entry<String, JsonElement>> columnIterator = currentRow.entrySet().iterator();
            while (columnIterator.hasNext()) {
                Map.Entry<String, JsonElement> columnObj = columnIterator.next();
                XSSFCell cell = row.createCell(Integer.parseInt(columnObj.getKey()));
                JsonObject currentColumn = (JsonObject) columnObj.getValue();
                try {
                    if (currentColumn.has("formula")) {
                        JsonElement Formula = currentColumn.get("formula");
                        JsonElement value = currentColumn.get("value");
                        if (value != null && value.isJsonObject()) {
                            cell.setCellType(XSSFCell.CELL_TYPE_ERROR);
                            byte errorCode = value.getAsJsonObject().get("_code").getAsByte();
                            cell.setCellErrorValue(errorCode);
                        } else {
                            cell.setCellType(XSSFCell.CELL_TYPE_FORMULA);
                            cell.setCellFormula(Formula.getAsString());
                            if (currentColumn.has("style")) {
                                JsonObject style = currentColumn.getAsJsonObject("style");
                                if (style.has("formatter")) {
                                    String cellFormatter = style.get("formatter").getAsString();
                                    if (cellFormatter.contentEquals("M/d/yyyy")) {
                                        JsonPrimitive primitiveValue = Objects.requireNonNull(value).getAsJsonPrimitive();
                                        if (primitiveValue.isNumber()) {
                                            Date date = DateUtil.getJavaDate(value.getAsDouble());
                                            cell.setCellValue(date);
                                        } else {
                                            Date date = Date.from(Instant.parse(value.getAsString()));
                                            cell.setCellValue(date);
                                        }
                                    }
                                } else {
                                    if (value == null)
                                        cell.setCellValue("");
                                    else
                                        cell.setCellValue(value.getAsString());
                                }
                            } else {
                                if (value == null)
                                    cell.setCellValue("");
                                else
                                    cell.setCellValue(value.getAsString());
                            }
                        }

                    } else if (currentColumn.has("value")) {
                        JsonElement cellValueEle = currentColumn.get("value");
                        if (cellValueEle.isJsonObject()) {
                            cell.setCellType(XSSFCell.CELL_TYPE_ERROR);
                            byte errorCode = cellValueEle.getAsJsonObject().get("_code").getAsByte();
                            cell.setCellErrorValue(errorCode);
                            break;
                        }

                        JsonPrimitive cellValueObj = (JsonPrimitive) currentColumn.get("value");
                        if (cellValueObj.isNumber()) {
                            cell.setCellValue(currentColumn.get("value").getAsDouble());
                        } else if (cellValueObj.isString()) {
                            String cellValue = currentColumn.get("value").getAsString();
                            if (cellValue.contains("OADate")) {
                                String dateString = cellValue.split(Pattern.quote("("))[1].split(Pattern.quote(")"))[0];
                                double doubleDate = Double.parseDouble(dateString);
                                
                                /*
                                If date set prior to 1900 dates then converting to String and aligned to right to make it appear as date. 
                                This is a workaround which is the only possible solution for dates prior 1900.
                                */
                                if (doubleDate < 0) {
                                    Date date = ETDateUtils.fromDoubleToDateTime(doubleDate);
                                    String formatterString = currentColumn.getAsJsonObject("style").getAsJsonObject("autoFormatter").get("formatCached").getAsString();
                                    DateFormat df = new SimpleDateFormat(formatterString);
                                    cell.setCellValue(df.format(date));
                                } else {
                                    Date date = DateUtil.getJavaDate(doubleDate);
                                    cell.setCellValue(date);
                                }
                            } else {
                                cell.setCellValue(cellValue);
                            }
                        }
                    }
                    if (currentColumn.has("style")) {
                        JsonObject style = currentColumn.getAsJsonObject("style");
                        XSSFCellStyle cellStyle = cellStyle(style);
                        if (style.has("cellType") && style.getAsJsonObject("cellType").has("linkToolTip")) {
                            CreationHelper createHelper = workbook.getCreationHelper();
                            Hyperlink link = createHelper.createHyperlink(Hyperlink.LINK_URL);
                            JsonObject cellTypeObj = style.getAsJsonObject("cellType");
                            String urlValue = cellTypeObj.get("linkToolTip").getAsString(), urlText;
                            if (cellTypeObj.has("text")) {
                                urlText = cellTypeObj.get("text").getAsString();
                                cell.setCellValue(urlText);
                            } else {
                                cell.setCellValue(urlValue);
                            }
                            link.setAddress(urlValue);
                            cell.setHyperlink(link);
                        }
                        cell.setCellStyle(cellStyle);
                    }
                } catch (Exception ex) {
                    LOGGER.warn("You may lose some cells data", ex);
                }
            }
        }
    }

    private void spans(XSSFSheet xssfSheet, JsonArray spanObj) {
        for (int i = 0; i < spanObj.size(); i++) {
            try {
                JsonObject mergedCellObj = (JsonObject) spanObj.get(i);
                int row = mergedCellObj.get("row").getAsInt();
                int rowCnt = mergedCellObj.get("rowCount").getAsInt() - 1;
                int col = mergedCellObj.get("col").getAsInt();
                int colCnt = mergedCellObj.get("colCount").getAsInt() - 1;

                xssfSheet.addMergedRegion(new CellRangeAddress(row, row + rowCnt, col, col + colCnt));
            } catch (Exception ex) {
                LOGGER.warn("You may lose some cell merges", ex);
            }
        }
    }

    private void rows(XSSFSheet xssfSheet, JsonArray rowsObj) {
        for (int i = 0; i < rowsObj.size(); i++) {
            try {
                JsonElement rowEle = rowsObj.get(i);
                if (!rowEle.isJsonNull()) {
                    JsonObject rowObj = (JsonObject) rowEle;
                    if (rowObj.has("size")) {
                        XSSFRow currentRow = xssfSheet.getRow(i);
                        if (currentRow == null) {
                            currentRow = xssfSheet.createRow(i);
                        }
                        short rowHeight = (short) Math.round((rowObj.get("size").getAsFloat()) * PX_TO_TWIPS);
                        currentRow.setHeight(rowHeight);
                    }
                }
            } catch (Exception ex) {
                LOGGER.warn("You may lose some row formatting", ex);
            }
        }
    }

    private void columns(XSSFSheet xssfSheet, JsonArray colsObj) {
        for (int i = 0; i < colsObj.size(); i++) {
            try {
                JsonElement colEle = colsObj.get(i);
                if (!colEle.isJsonNull()) {
                    JsonObject columnObj = (JsonObject) colEle;
                    if (columnObj.has("size")) {
                        float a = columnObj.get("size").getAsFloat();
                        a = calculateColWidth(Math.round(a));
                        xssfSheet.setColumnWidth(i, Math.round(a));
                    }
                }
            } catch (Exception ex) {
                LOGGER.warn("You may lose some column formatting", ex);
            }
        }
    }

    private void comment(XSSFSheet xssfSheet, JsonArray commentArr) {

        XSSFDrawing patr = xssfSheet.createDrawingPatriarch();
        XSSFClientAnchor clientAnchor = new XSSFClientAnchor(0, 0, 0, 0, (short) 7, 2, (short) 9, 5);

        for (int i = 0; i < commentArr.size(); i++) {
            try {
                JsonObject commentObj = (JsonObject) commentArr.get(i);

                XSSFCell cell = xssfSheet.getRow(commentObj.get("rowIndex").getAsInt()).getCell(commentObj.get("colIndex").getAsInt());

                XSSFComment comment = patr.createCellComment(clientAnchor);
                XSSFRichTextString testStr = new XSSFRichTextString(commentObj.get("text").getAsString());
                XSSFFont commentFont = workbook.createFont();

                if (commentObj.has("fontWeight")) {
                    commentFont.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
                }
                if (commentObj.has("fontStyle")) {
                    commentFont.setItalic(true);
                }
                if (commentObj.has("foreColor")) {
                    XSSFColor color = getXSSFColorObjFromColorString(commentObj.get("foreColor").toString());
                    if (color != null) {
                        commentFont.setColor(color);
                    }
                }
                testStr.applyFont(commentFont);
//            if (commentObj.has("backColor")) {
//                String[] backColor = commentObj.get("backColor").toString().replace("rgb(", "").replace(")", "").trim().split(",");
//                int r = Integer.parseInt(backColor[0].trim());
//                int g = Integer.parseInt(backColor[1].trim());
//                int b = Integer.parseInt(backColor[2].trim());
//                comment.setFillColor(new XSSFColor(new java.awt.Color(r,g,b)));
//            }

                comment.setString(testStr);
                cell.setCellComment(comment);
            } catch (Exception ex) {
                LOGGER.warn("You may lose some cells comments", ex);
            }
        }
    }

    /* Getting width and height of floating objects and converting byte array of image to buffered image for setting this exact width and height of image whike exporting.
       Getting start row and start column of image to set the exact position of image while exporting.*/
    private void floatingObjects(XSSFSheet xssfSheet, JsonArray floatingObjects, int columnCount) throws IOException {
        Base64 decoder = new Base64();

        for (int i = 0; i < floatingObjects.size(); i++) {
            try {
                CreationHelper helper = workbook.getCreationHelper();
                //Creates the top-level drawing patriarch.
                Drawing drawing = xssfSheet.createDrawingPatriarch();
                //Create an anchor that is attached to the worksheet
                ClientAnchor anchor = helper.createClientAnchor();

                JsonObject imgObj = (JsonObject) floatingObjects.get(i);
                double width, height;
                int startRow, startColumn;
                width = imgObj.get("width") != null ? imgObj.get("width").getAsDouble() : 30;
                height = imgObj.get("height") != null ? imgObj.get("height").getAsDouble() : 30;
                startRow = imgObj.get("startRow") != null ? imgObj.get("startRow").getAsInt() : 1;
                startColumn = imgObj.get("startColumn") != null ? imgObj.get("startColumn").getAsInt() : 1;

                String imageSource = imgObj.get("src").toString();
                String imageString = "";

                //Images can come in two ways - one is the older base64 encoded string ,
                // second is the just the confluence download url (newer way)
                //Need to take care of both scenarios to ensure backward compatibility.
                if (isNotBlank(imageSource)) {
                    if (imageSource.startsWith("\"data:image")) {
                        LOGGER.debug("Older base64 image");
                        String[] imageStrings = imgObj.get("src").toString().split("base64,");
                        if (imageStrings.length != 0) {
                            imageString = imageStrings[1];
                        }
                    } else {
                        LOGGER.debug("Newer url image ....");
                        String imageSrc = imgObj.get("src").getAsString();
                        String baseUrl = getBaseUrl();
                        if (isNotBlank(baseUrl)) {
                            //Create the Image URL by combining the baseURL and the image source attribute of the image
                            String imageURL = baseUrl + imageSrc;
                            imageString = getImageStringFromImageURL(imageURL, decoder);
                        }
                    }
                }


                byte[] imageByte = decoder.decode(imageString);
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageByte));//Changing our byte array to buffered image to set the proper width and height of the image.
                Image tmp = img.getScaledInstance((int) width, (int) height, Image.SCALE_SMOOTH);
                BufferedImage convertedImage = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_RGB);//setting the width and height through buffered image

                //draw image
                Graphics2D g2d = convertedImage.createGraphics();
                g2d.drawImage(tmp, 0, 0, null);
                g2d.dispose();

                //again converting buffered image to byte array to add it to the workbook
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(convertedImage, "jpg", baos);
                byte[] bytes = baos.toByteArray();
                int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);

                //setting the start row and start column of image
                anchor.setCol1(startColumn);
                anchor.setRow1(startRow);

                //Creates a picture
                Picture pict = drawing.createPicture(anchor, pictureIdx);
                pict.resize();

            } catch (Exception ex) {
                LOGGER.warn("You may lose some floating objects", ex);
            }
        }
    }

    /**
     * Helper method to get the base64 encode image string from the confluence image URL provided.
     *
     * @param imageURL
     * @param encoder
     * @return
     * @throws IOException
     */
    private String getImageStringFromImageURL(String imageURL, Base64 encoder) throws IOException {
        Maybe<Attachment> attachment = attachmentManager.getAttachmentForDownloadPath(imageURL);
        Attachment image = attachment.get();
        InputStream is = attachmentManager.getAttachmentData(image);
        byte[] imageBytes = IOUtils.toByteArray(is);
        String encodedImageString = encoder.encodeAsString(imageBytes);
        return encodedImageString;
    }

    private void tables(XSSFSheet xssfSheet, JsonArray tablesArray) {
        for (int table = tablesArray.size() - 1; table >= 0; table--) {
            try {
                JsonObject tableObj = (JsonObject) tablesArray.get(table);

                String tableName = "";
                if (tableObj.has("name")) {
                    tableName = tableObj.get("name").getAsString();
                }

                int row = tableObj.get("row").getAsInt();
                int rowCount = tableObj.get("rowCount").getAsInt();
                int col = tableObj.get("col").getAsInt();
                int colCount = tableObj.get("colCount").getAsInt();
                CellRangeAddress cellRange = new CellRangeAddress(row, row + rowCount - 1, col, col + colCount - 1);

                CTAutoFilter autoFilter;
   
                /*
                If TableStyleName is "exc-table-filter" then treat that as a table filter.
                because in Excellentable we are internally using table style for filter.
                 */
                if (tableName.endsWith("exc-table-filter")) {
                    xssfSheet.setAutoFilter(cellRange);
                    autoFilter = xssfSheet.getCTWorksheet().getAutoFilter();
                } else {
                    long tableId = (long) table + 1;
                    autoFilter = addTableDesign(xssfSheet, tableObj, tableId, cellRange);
                }

                if (!tableObj.has("rowFilter")) continue;
                JsonObject rowFilter = (JsonObject) tableObj.get("rowFilter");

                /* Define Filter Information for the Table */
                if (!rowFilter.has("range")) continue;
                JsonObject rangeObj = rowFilter.get("range").getAsJsonObject();
                JsonObject filterButtonVisibleInfo = rowFilter.get("filterButtonVisibleInfo").getAsJsonObject();
                int rangeColCount = rangeObj.get("colCount").getAsInt();

                if (autoFilter == null) {
                    continue;
                }
                for (int i = 0; i < rangeColCount; i++) {
                    CTFilterColumn filter = autoFilter.addNewFilterColumn();
                    filter.setColId((long) i);

                    boolean filterButtonStatus = filterButtonVisibleInfo.has(Integer.toString(i))
                            ? filterButtonVisibleInfo.get(Integer.toString(i)).getAsBoolean() : false;

                    filter.setShowButton(filterButtonStatus);
                }

                applyFilter(xssfSheet, rowFilter, autoFilter, cellRange);

            } catch (Exception ex) {
                LOGGER.warn("You may lose some table formatting", ex);
            }
        }
    }

    private XSSFCellStyle cellStyle(JsonObject style) {

        XSSFCellStyle cellStyle = workbook.createCellStyle();
        XSSFFont cellFont = workbook.createFont();
        if (style.has("cellType") && style.getAsJsonObject("cellType").has("linkToolTip")) {
            //by default hyperlinks are blue and underlined
            JsonObject cellTypeJson = style.getAsJsonObject("cellType");
            String linkColor = cellTypeJson.get("linkColor").getAsString();
            Color colorObj = hex2Rgb(linkColor);
            cellFont.setColor(new XSSFColor(colorObj));
            cellFont.setUnderline(XSSFFont.U_SINGLE);
        }
        if (style.has("formatter")) {
            CreationHelper createHelper = workbook.getCreationHelper();
            String formatterString = style.get("formatter").getAsString();
            cellStyle.setDataFormat(createHelper.createDataFormat().getFormat(formatterString));
        }
        if (style.has("autoFormatter")) {
            CreationHelper createHelper = workbook.getCreationHelper();
            JsonObject formatterString = style.getAsJsonObject("autoFormatter");
            if (formatterString.has("formatCached")) {
                String formatterString1 = style.getAsJsonObject("autoFormatter").get("formatCached").getAsString();
                cellStyle.setDataFormat(createHelper.createDataFormat().getFormat(formatterString1));
                cellStyle.setAlignment(HorizontalAlignment.RIGHT);
            }
        }

        if (style.has("font")) {
            try {
                String font = style.get("font").toString();
                String fontArray;
                if (font.contains("px")) {
                    fontArray = font.split("px")[1];
                } else if (font.contains("pt")) {
                    fontArray = font.split("pt")[1];
                } else {
                    fontArray = "";
                }
                String fontName = fontArray.replaceAll("\"", "").replaceAll("\\\\", "").trim();

                if (Fonts.hasFont(fontName)) {
                    cellFont.setFontName(fontName);
                    font = font.replace(fontName, "");
                } else {
                    cellFont.setFontName(Fonts.getDefault());
                }

                String[] fontStyle = font.split("\'")[0].split(" ");
                //Apply font style i.e bold, italic & fontSize
                for (String fontStyle1 : fontStyle) {
                    fontStyle1 = fontStyle1.replaceAll("\"", "");
                    if (fontStyle1.equals("bold")) {
                        cellFont.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
                    } else if (fontStyle1.equals("italic")) {
                        cellFont.setItalic(true);
                    } else if (fontStyle1.contains("px")) {
                        cellFont.setFontHeightInPoints((short) Math.round(Short.valueOf(fontStyle1.split("[.A-Za-z]+")[0]) * PX_TO_PT));
                    }
                }
            } catch (Exception ex) {
                LOGGER.warn("You may lose some text formatting", ex);
            }
        }
        if (style.has("backColor")) {
            try {
                String backColor = style.get("backColor").getAsString();
                Color bColor = hex2Rgb(backColor);
                cellStyle.setFillForegroundColor(new XSSFColor(bColor));
                cellStyle.setFillPattern(org.apache.poi.ss.usermodel.CellStyle.SOLID_FOREGROUND);
            } catch (Exception ex) {
                LOGGER.warn("You may lose some text formatting", ex);
            }
        }
        if (style.has("foreColor")) {
            try {
                String foreColor = style.get("foreColor").getAsString();
                Color fColor = hex2Rgb(foreColor);
                cellFont.setColor(new XSSFColor(fColor));
            } catch (Exception ex) {
                LOGGER.warn("You may lose some text formatting", ex);
            }

        }
        try {
            Short vAlign = 1;
            if (style.has("vAlign")) {
            	vAlign = style.get("vAlign").getAsShort();
            }
            cellStyle.setVerticalAlignment(vAlign);

            
        } catch (Exception ex) {
            LOGGER.warn("You may lose vertical alignment", ex);
        }
        
        
        if (style.has("hAlign")) {
            try {
                short hAlign = style.get("hAlign").getAsShort();
                switch (hAlign) {
                    case 0:
                        cellStyle.setAlignment(HorizontalAlignment.LEFT);
                        break;
                    case 1:
                        cellStyle.setAlignment(HorizontalAlignment.CENTER);
                        break;
                    case 2:
                        cellStyle.setAlignment(HorizontalAlignment.RIGHT);
                        break;
                    case 3:
                        cellStyle.setAlignment(HorizontalAlignment.GENERAL);
                        break;
                    default:
                        break;
                }
            } catch (Exception ex) {
                LOGGER.warn("You may lose horizontal alignment", ex);
            }
        }
        if (style.has("wordWrap")) {
            try {
                Boolean wordWrap = style.get("wordWrap").getAsBoolean();
                cellStyle.setWrapText(wordWrap);
            } catch (Exception ex) {
                LOGGER.warn("You may lose word wrap", ex);
            }
        }
       
        try {
        	 short textIndent = 1;
        	 if (style.has("textIndent")) {
        		 textIndent = style.get("textIndent").getAsShort();
             }
            
        	 cellStyle.setIndention(textIndent);
        	 
        } catch (Exception ex) {
            LOGGER.warn("You may lose text indentation", ex);
        }
        cellStyle = setBorder(cellStyle, style.get("borderTop"), "top");
        cellStyle = setBorder(cellStyle, style.get("borderBottom"), "bottom");
        cellStyle = setBorder(cellStyle, style.get("borderLeft"), "left");
        cellStyle = setBorder(cellStyle, style.get("borderRight"), "right");

        if (style.has("textDecoration")) {
            try {
                int textDecoration = style.get("textDecoration").getAsInt();
                boolean hasUnderline = false;
                boolean hasStrikethrough = false;

                if ((textDecoration | 1) == textDecoration) {
                    hasUnderline = true;
                }
                if ((textDecoration | 2) == textDecoration) {
                    hasStrikethrough = true;
                }

                if (hasUnderline) {
                    cellFont.setUnderline(XSSFFont.U_SINGLE);
                }
                if (hasStrikethrough) {
                    cellFont.setStrikeout(true);
                }
            } catch (Exception ex) {
                LOGGER.warn("You may lose text decoration", ex);
            }
        }
        cellStyle.setFont(cellFont);

        return cellStyle;
    }

    private int calculateColWidth(int width) {
        int newWidth = width / 8 * 256;
        return newWidth > 65280 ? 65280 : newWidth;
    }

    private Color hex2Rgb(String colorStr) {
        return new Color(
                Integer.valueOf(colorStr.substring(1, 3), 16),
                Integer.valueOf(colorStr.substring(3, 5), 16),
                Integer.valueOf(colorStr.substring(5, 7), 16));
    }

    private XSSFCellStyle setBorder(XSSFCellStyle cellStyle, Object border, String borderSide) {
        try {
            if (border == null) {
                return cellStyle;
            }

            JsonObject borderObj = (JsonObject) border;
            JsonElement borderColor = borderObj.get("color");
            JsonElement borderStyle = borderObj.get("style");

            if (borderColor == null || borderStyle == null) {
                return cellStyle;
            }

            XSSFColor c = getXSSFColorObjFromColorString(borderColor.getAsString());
            if (c == null) {
                return cellStyle;
            }

            switch (borderSide) {
                case "top":
                    cellStyle.setTopBorderColor(c);
                    cellStyle.setBorderTop(borderStyle.getAsShort());
                    break;
                case "right":
                    cellStyle.setRightBorderColor(c);
                    cellStyle.setBorderRight(borderStyle.getAsShort());
                    break;
                case "bottom":
                    cellStyle.setBottomBorderColor(c);
                    cellStyle.setBorderBottom(borderStyle.getAsShort());
                    break;
                case "left":
                    cellStyle.setLeftBorderColor(c);
                    cellStyle.setBorderLeft(borderStyle.getAsShort());
                    break;
                default:
                    return cellStyle;
            }

        } catch (Exception ex) {
            LOGGER.warn("You may lose " + borderSide + " border", ex);
        }
        return cellStyle;
    }

    /**
     * Validate hex with regular expression
     *
     * @param hex hex for validation
     * @return true valid hex, false invalid hex
     */
    public boolean isHEXColor(final String hex) {
        matcher = hexPattern.matcher(hex);
        return matcher.matches();
    }

    /**
     * Validate RGB with regular expression
     *
     * @param rgb for validation
     * @return true valid RGB, false invalid RGB
     */
    public boolean isRGBColor(final String rgb) {
        matcher = rgbPattern.matcher(rgb);
        return matcher.matches();
    }

    private CTAutoFilter addTableDesign(XSSFSheet xssfSheet, JsonObject tableObj, long tableId, CellRangeAddress cellRange) {

        XSSFTable xssfTable = xssfSheet.createTable();
        CTTable ctTable = xssfTable.getCTTable();
        String tableName = "";
        if (tableObj.has("name")) {
            tableName = tableObj.get("name").getAsString();
        }

        ctTable.setRef(cellRange.formatAsString());  //Set Range to the Table.
        ctTable.setDisplayName(tableName);
        ctTable.setName(tableName);
        ctTable.setId(tableId); //Set unique id attribute against table as long value

        CTTableStyleInfo tableStyle = ctTable.addNewTableStyleInfo(); //Define Table style of the table.
        JsonElement styleBuildInName = tableObj.get("style").getAsJsonObject().get("buildInName");
        String tableStyleName = styleBuildInName != null ? styleBuildInName.getAsString() : "";
        tableStyle.setName("TableStyle" + tableStyleName);

        boolean highlightFirstColumn = tableObj.has("highlightFirstColumn") ? tableObj.get("highlightFirstColumn").getAsBoolean() : false;
        tableStyle.setShowFirstColumn(highlightFirstColumn);

        boolean highlightLastColumn = tableObj.has("highlightLastColumn") ? tableObj.get("highlightLastColumn").getAsBoolean() : false;
        tableStyle.setShowLastColumn(highlightLastColumn);

        boolean bandedColumn = tableObj.has("bandColumns") ? tableObj.get("bandColumns").getAsBoolean() : false;
        tableStyle.setShowColumnStripes(bandedColumn);

        boolean bandedRow = tableObj.has("bandRows") ? tableObj.get("bandRows").getAsBoolean() : true;
        tableStyle.setShowRowStripes(bandedRow);

        ctTable = setHeaderAndFooter(ctTable, tableObj, cellRange);

        addTableDesignColumns(xssfSheet, tableObj, ctTable);

        return ctTable.getAutoFilter();
    }

    private CTTable setHeaderAndFooter(CTTable ctTable, JsonObject tableObj, CellRangeAddress cellRange) {
        CTAutoFilter autoFilter = null;
        boolean showHeader = tableObj.has("showHeader") ? tableObj.get("showHeader").getAsBoolean() : true;
        if (showHeader) {
            autoFilter = ctTable.addNewAutoFilter();
        } else {
            ctTable.setHeaderRowCount(0l);
        }

        boolean showFooter = tableObj.has("showFooter") ? tableObj.get("showFooter").getAsBoolean() : false;
        if (showFooter) {
            ctTable.setTotalsRowCount(1l);
            if (showHeader) { //Update Filter reference.
                cellRange.setLastRow(cellRange.getLastRow() - 1);
                autoFilter.setRef(cellRange.formatAsString());
            }
        }
        return ctTable;
    }

    private void addTableDesignColumns(XSSFSheet xssfSheet, JsonObject tableObj, CTTable ctTable) {
        int row = tableObj.get("row").getAsInt();
        int col = tableObj.get("col").getAsInt();
        int colCount = tableObj.get("colCount").getAsInt();

        JsonArray columnsArray = tableObj.get("columns").getAsJsonArray();
        CTTableColumns columns = ctTable.addNewTableColumns();
        columns.setCount((long) colCount);
        XSSFRow currentRow = xssfSheet.getRow(row) != null ? xssfSheet.getRow(row) : xssfSheet.createRow(row);
        for (int i = colCount - 1; i >= 0; i--) {
            CTTableColumn column = columns.addNewTableColumn();

            XSSFCell currentCell = currentRow.getCell(col + i) != null ? currentRow.getCell(col + i) : currentRow.createCell(col + i);
            currentCell.setCellType(Cell.CELL_TYPE_STRING); //Set the Cell Type of all table headers as a String.

            String columnName = columnsArray.get(i).getAsJsonObject().get("name").getAsString();
            column.setName(columnName);

            Long columnId = columnsArray.get(i).getAsJsonObject().get("id").getAsLong();
            column.setId(columnId);
        }
    }

    private void applyFilter(XSSFSheet xssfSheet, JsonObject rowFilter, CTAutoFilter autoFilter, CellRangeAddress cellRange) {
        if (!rowFilter.has("filterItemMap")) return;
        JsonArray filterItemMap = rowFilter.get("filterItemMap").getAsJsonArray();

        int row = cellRange.getFirstRow();
        int col = cellRange.getFirstColumn();
        int rowCount = cellRange.getLastRow() - cellRange.getFirstRow() + 1;

        for (JsonElement filterItem : filterItemMap) {
            JsonObject filterItemObj = filterItem.getAsJsonObject();
            int colIndex = filterItemObj.get("index").getAsInt();

            int expectedColIndex = colIndex - col;
            CTFilterColumn filterColumn = autoFilter.getFilterColumnArray(expectedColIndex);
            CTFilters filters = filterColumn.addNewFilters();
            if (!filterItemObj.has("conditions")) {
                continue;
            }
            JsonArray conditions = filterItemObj.get("conditions").getAsJsonArray();
            List<String> expectedArray = new ArrayList<String>();
            for (JsonElement condition : conditions) {
                JsonObject conditionObj = condition.getAsJsonObject();
                CTFilter filter = filters.addNewFilter();
                String expected = conditionObj.get("expected").getAsString().trim();
                filter.setVal(expected);
                expectedArray.add(expected);
            }

            for (int i = rowCount - 1; i > 0; i--) {
                XSSFRow xssfRow = xssfSheet.getRow(row + i);
                if (xssfRow.getCTRow().getHidden() || xssfRow.getCell(colIndex) == null) {
                    continue;
                }

                hideRowBasedOnCellTypeAndSearchCriteria(xssfRow, expectedArray, colIndex);

            }
        }
    }

    private void hideRowBasedOnCellTypeAndSearchCriteria(XSSFRow xssfRow, List<String> expectedArray, int colIndex) {
        XSSFCell xssfCell = xssfRow.getCell(colIndex);
        String cellValueObj = null;
        switch (xssfCell.getCellType()) {
            case XSSFCell.CELL_TYPE_NUMERIC:
                cellValueObj = Double.toString(xssfCell.getNumericCellValue()).trim();
                break;
            case XSSFCell.CELL_TYPE_FORMULA:
                cellValueObj = xssfCell.getCellFormula().trim();
                break;
            case XSSFCell.CELL_TYPE_ERROR:
                cellValueObj = xssfCell.getErrorCellString().trim();
                break;
            case XSSFCell.CELL_TYPE_BOOLEAN:
                cellValueObj = String.valueOf(xssfCell.getBooleanCellValue()).trim();
                break;
            case XSSFCell.CELL_TYPE_STRING:
                cellValueObj = xssfCell.getStringCellValue().trim();
                break;
            default:
                break;
        }

        if (cellValueObj != null && !expectedArray.contains(cellValueObj)) {
            xssfRow.getCTRow().setHidden(true);
        }
    }

    private XSSFColor getXSSFColorObjFromColorString(String colorString) {
        Color color;
        if (isHEXColor(colorString)) {
            color = hex2Rgb(colorString);
        } else if (isRGBColor(colorString)) {
            Matcher m = rgbPattern.matcher(colorString);
            color = new Color(Integer.valueOf(m.group(1)), //r
                    Integer.valueOf(m.group(2)), // g
                    Integer.valueOf(m.group(3))); // b 
        } else {
            color = colorUtil.getColorRGBFromName(colorString);
        }
        if (color != null) {
            return new XSSFColor(color);
        }

        return null;
    }
}
