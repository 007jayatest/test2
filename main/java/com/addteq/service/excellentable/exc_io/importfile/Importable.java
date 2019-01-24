/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.addteq.service.excellentable.exc_io.importfile;

import com.addteq.service.excellentable.exc_io.spreadjs.Fonts;
import com.google.gson.JsonObject;
import org.apache.poi.hssf.util.PaneInformation;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;

import java.util.HashSet;

/**
 * @author neeraj bodhe
 */
public interface Importable {

    JsonObject buildImportSheetJson(Object sheetData, String version) throws Exception;

    //Will iterate through sheet and get the numbers of cell in it.
    default int getNumberOfCellsInSheet(Sheet inputSheet) {
        int totalCells = 0;
        int rowStart = inputSheet.getFirstRowNum();
        int rowEnd = inputSheet.getPhysicalNumberOfRows();
        for (int rowIndex = rowStart; rowIndex < rowEnd; rowIndex++) {
            Row row = inputSheet.getRow(rowIndex);
            if (row == null) continue;
            int lastColumn = row.getPhysicalNumberOfCells() + 1;
            totalCells += (lastColumn);
        }
        return totalCells;
    }

    default void addGridlinesToSheet(JsonObject outputSheet, boolean displayGridlines, String gridlineColor) {
        if (!displayGridlines) {
            JsonObject gridlineObj = new JsonObject();
            gridlineObj.addProperty("color", gridlineColor);
            gridlineObj.addProperty("showVerticalGridline", false);
            gridlineObj.addProperty("showHorizontalGridline", false);
            outputSheet.add("gridline", gridlineObj);
        }
    }

    default void addPaneInformationToSheet(JsonObject outputSheet, PaneInformation paneInformation) {
        if (paneInformation != null) {
            short frozenRowCont = paneInformation.getHorizontalSplitPosition();
            short frozenColCount = paneInformation.getVerticalSplitPosition();
            outputSheet.addProperty("frozenRowCount", frozenRowCont);
            outputSheet.addProperty("frozenColCount", frozenColCount);
        }
    }

    default void addBoldItalicFontSizeAndStyleToCell(JsonObject tempVal, JsonObject tempCol, String cellColumnIndex, JsonObject errorObj, HashSet<String> fontNames, JsonObject cellstyle, String fontName, short fontSize, String fontString, boolean italic, boolean bold, short minFontSize, short maxFontSize, Logger logger) {
        try {
            if (italic) {
                fontString = fontString + "italic" + " ";
            }
            if (bold) {
                fontString = fontString + "bold" + " ";
            }

            if (fontSize < minFontSize) {
                fontSize = minFontSize;
                errorObj.addProperty("minFontSize", "Fontsize <strong>" + fontSize + "</strong> isn't available in Excellentable. It is replaced by <strong>" + minFontSize + "</strong>");
            } else if (fontSize > maxFontSize) {
                fontSize = maxFontSize;
                errorObj.addProperty("maxFontSize", "FontSize <strong>" + fontSize + "</strong> isn't available in Excellentable. It is replaced by <strong>" + maxFontSize + "</strong>");
            }
            fontString = fontString + fontSize + "pt" + " ";

            if (Fonts.hasFont(fontName)) {
                fontString = fontString + fontName;
            } else {
                fontString = fontString + Fonts.getDefault();
                String fontNamesString = fontNamesToString(fontName, fontNames);
                errorObj.addProperty("fontFamily", "com.addteq.confluence.plugin.excellentable.import.style.fontFamily.error"); //Verdana in i18n file,
                errorObj.addProperty("fontNames", fontNamesString);
            }
            cellstyle.addProperty("font", fontString);
            tempVal.add("style", cellstyle);
            tempCol.add(cellColumnIndex, tempVal);
        } catch (Exception ex) {
            logAndUpdateWarnings(
                    errorObj,
                    "You may lose some font style and font size",
                    ex,
                    "font",
                    "com.addteq.confluence.plugin.excellentable.import.style.font.error ",
                    logger
            );
        }
    }

    default void addStrikeoutAndUnderlineToCell(JsonObject tempVal, JsonObject tempCol, String cellColumnIndex, JsonObject errorObj, JsonObject cellStyle, boolean isStrikeOut, byte isUnderline, Logger logger) {
        try {
            if (isStrikeOut && isUnderline == 1) {
                cellStyle.addProperty("textDecoration", 3);
            } else if (isUnderline == 1) {
                cellStyle.addProperty("textDecoration", 1);
            } else if (isStrikeOut) {
                cellStyle.addProperty("textDecoration", 2);
            }
            tempVal.add("style", cellStyle);
            tempCol.add(cellColumnIndex, tempVal);

        } catch (Exception ex) {
            logAndUpdateWarnings(
                    errorObj,
                    "You may lose some text decoration",
                    ex,
                    "textDecoration",
                    "com.addteq.confluence.plugin.excellentable.import.style.textDecoration.error",
                    logger
            );
        }
    }

    default void addHorizontolAlignmentToCell(JsonObject tempVal, JsonObject tempCol, String cellColumnIndex, JsonObject errorObj, JsonObject cellStyle, short textAlignment, Logger logger) {
        try {
            if (textAlignment > 0) {
                cellStyle.addProperty("hAlign", textAlignment - 1);
            }
            tempVal.add("style", cellStyle);
            tempCol.add(cellColumnIndex, tempVal);

        } catch (Exception ex) {
            logAndUpdateWarnings(
                    errorObj,
                    "You may lose some horizontal alighment",
                    ex,
                    "horizontalAlignment",
                    "com.addteq.confluence.plugin.excellentable.import.style.horizontalAlignment.error",
                    logger
            );
        }
    }

    default void addVerticalAlignmentToCell(JsonObject tempVal, JsonObject tempCol, String cellColumnIndex, JsonObject errorObj, JsonObject cellstyle, short verticalAlignment, Logger logger) {
        try {
            if (verticalAlignment >= 0) {
                cellstyle.addProperty("vAlign", verticalAlignment);
            }
            tempVal.add("style", cellstyle);
            tempCol.add(cellColumnIndex, tempVal);
        } catch (Exception ex) {
            logAndUpdateWarnings(
                    errorObj,
                    "You may lose some vertical alighment",
                    ex,
                    "verticalAlignment",
                    "com.addteq.confluence.plugin.excellentable.import.style.verticalAlignment.error",
                    logger
            );
        }
    }

    default void addWordwrapToCell(JsonObject tempVal, JsonObject tempCol, String cellColumnIndex, JsonObject errorObj, JsonObject cellstyle, boolean wrapText, Logger logger) {
        try {
            if (wrapText) {
                cellstyle.addProperty("wordWrap", true);
            }
            tempVal.add("style", cellstyle);
            tempCol.add(cellColumnIndex, tempVal);
        } catch (Exception ex) {
            logAndUpdateWarnings(
                    errorObj,
                    "You may lose text wrap",
                    ex,
                    "wordWrap",
                    "com.addteq.confluence.plugin.excellentable.import.style.wordWrap.error",
                    logger
            );
        }
    }

    default void addIndentationToCell(JsonObject tempVal, JsonObject tempCol, String cellColumnIndex, JsonObject errorObj, JsonObject cellStyle, short textIndent, Logger logger) {
        try {
            if (textIndent >= 0) {
                cellStyle.addProperty("textIndent", textIndent);
                tempVal.add("style", cellStyle);
                tempCol.add(cellColumnIndex, tempVal);
            }
        } catch (Exception ex) {
            logAndUpdateWarnings(
                    errorObj,
                    "You may lose text indentation",
                    ex,
                    "textIndentation",
                    "com.addteq.confluence.plugin.excellentable.import.style.textIndentation.error",
                    logger
            );
        }
    }

    default void addFormattingToCell(JsonObject tempVal, JsonObject tempCol, String cellColumnIndex, JsonObject errorObj, JsonObject cellStyle, String formatter, Logger logger) {
        try {
            if (formatter != null && formatter.startsWith("$")) {
                cellStyle.addProperty("formatter", "$#,##0.00");

            } else if (formatter != null && formatter.contains("%")) {
                cellStyle.addProperty("formatter", "0.00%");
                /*  Ref: EXC-2289
                XLS sets a General formatter as a default formatter to all the cells which do not have any custom formatter applied.
                if user enters a date in the cell which has "General" formatter then it results a long number in SpreadJS.
                As a resolution do not import "General" cell formatter. */
            } else if (!"General".equalsIgnoreCase(formatter)) {
                cellStyle.addProperty("formatter", formatter);
            }
            tempVal.add("style", cellStyle);
            tempCol.add(cellColumnIndex, tempVal);
        } catch (Exception ex) {
            logAndUpdateWarnings(
                    errorObj,
                    "You may lose some formatter",
                    ex,
                    "formatter",
                    "com.addteq.confluence.plugin.excellentable.import.style.formatter.error",
                    logger
            );
        }
    }

    default String fontNamesToString(String fontName, HashSet<String> fontNames) {
        fontNames.add(fontName);
        StringBuilder fontNamesString = new StringBuilder("");
        StringBuilder comma = new StringBuilder(", ");
        for (String s : fontNames) {
            fontNamesString.append(s);
            fontNamesString.append(comma);
        }
        return fontNamesString.substring(0, fontNamesString.length() - 2);
    }

    default void logAndUpdateWarnings(JsonObject errorObj, String warningMessage, Exception ex, String propertyName,
                                      String propertyValue, Logger logger) {
        if (errorObj.has(propertyName)) {
            return;
        }
        logger.warn(warningMessage, ex);
        errorObj.addProperty(propertyName, propertyValue);
    }

}
