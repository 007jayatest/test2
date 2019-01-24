package com.addteq.service.excellentable.exc_io.utils;

import com.addteq.service.excellentable.exc_io.spreadjs.ColRowInfo;
import com.addteq.service.excellentable.exc_io.spreadjs.Sheet;
import java.util.List;

/**
 * Utility class that cleans up the Sheet meta data, if necessary, before passing it to PDF export.
 * This is needed to ensure that the row and column indices match as expected.
 *
 * Created by yagnesh.bhat on 7/27/18.
 */
public class SheetCleanup {

    /**
     * Cleans up the sheet object's properties to ensure the row and column indices match the data.
     * @param sheet
     * @param dataTableRowCount
     * @return sheet - that has the correct track of row and column indices
     */
    public static Sheet clean(Sheet sheet, int dataTableRowCount, int dataTableMaxColCount) {
        //Set the row count to the proper value if its not the same as determined.
        if (sheet.getRowCount() != dataTableRowCount) {
            sheet.setRowCount(dataTableRowCount);
        }
        List<ColRowInfo> rowsInfo = sheet.getRows();
        if (rowsInfo != null && (rowsInfo.size() > dataTableRowCount)) {
            int difference = rowsInfo.size() - dataTableRowCount;
            rowsInfo.subList(rowsInfo.size() - difference, rowsInfo.size()).clear();
        }

        //Set the column count to the proper value if its not the same as determined
        if (sheet.getColCount() != dataTableMaxColCount) {
            sheet.setColCount(dataTableMaxColCount);
        }
        List<ColRowInfo> colsInfo = sheet.getColumns();
        if (colsInfo != null && (colsInfo.size() > dataTableMaxColCount)) {
            int difference = colsInfo.size() - dataTableMaxColCount;
            colsInfo.subList(colsInfo.size() - difference, colsInfo.size()).clear();
        }

        return sheet;
    }
}
