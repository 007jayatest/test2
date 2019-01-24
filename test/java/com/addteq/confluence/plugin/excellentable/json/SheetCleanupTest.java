package com.addteq.confluence.plugin.excellentable.json;

import com.addteq.confluence.plugin.excellentable.TestingUtils;
import com.addteq.service.excellentable.exc_io.parser.JsonHtmlParser;
import com.addteq.service.excellentable.exc_io.spreadjs.ColRowInfo;
import com.addteq.service.excellentable.exc_io.spreadjs.ExcellentableSpread;
import com.addteq.service.excellentable.exc_io.spreadjs.Sheet;
import com.addteq.service.excellentable.exc_io.utils.SheetCleanup;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static com.addteq.service.excellentable.exc_io.json.ExcellentableSpreadParser.parse;

/**
 * Created by yagnesh.bhat on 7/27/18.
 */
public class SheetCleanupTest {

    @Test
    public void rowAndColCountsMatchData() throws Exception {

        File jsonFile = new File(getClass().getClassLoader().getResource("json/cleanupJSONActual.json").getFile());
        String actualJSON = TestingUtils.readFileIntoString(jsonFile);
        ExcellentableSpread actualSpread = parse(actualJSON);

        jsonFile =  new File(getClass().getClassLoader().getResource("json/cleanupJSONExpected.json").getFile());
        String expectedJSON = TestingUtils.readFileIntoString(jsonFile);
        ExcellentableSpread expectedSpread = parse(expectedJSON);
        Sheet expectedSheet = expectedSpread.getSheets().get("0");

        Sheet actualSheet = actualSpread.getSheets().get("0");
        int dataTableRowCount = JsonHtmlParser.findMaxRowIndex(actualSheet.getData().getDataTable());
        int dataTableColCount = JsonHtmlParser.findMaxColIndex(actualSheet.getData().getDataTable());

        actualSheet = SheetCleanup.clean(actualSheet, dataTableRowCount, dataTableColCount);

        Assert.assertEquals("Row Counts should match the data", actualSheet.getRowCount(), expectedSheet.getRowCount());
        Assert.assertEquals("Column Counts should match the data", actualSheet.getColCount(), expectedSheet.getColCount());

        List<ColRowInfo> rowsInfo = actualSheet.getRows();
        List<ColRowInfo> rowsInfoExpected = expectedSheet.getRows();
        Assert.assertEquals("Rows key should have 7 entries", rowsInfo.size(),rowsInfoExpected.size());

        List<ColRowInfo> colsInfo = actualSheet.getColumns();
        List<ColRowInfo> colsInfoExpected = expectedSheet.getColumns();
        Assert.assertEquals("Cols key should have 4 entries", colsInfo.size(),colsInfoExpected.size());
    }



}