package com.addteq.confluence.plugin.excellentable.importfile.impl;

import com.addteq.service.excellentable.exc_io.importfile.impl.ImportXlsx;
import com.addteq.service.excellentable.exc_io.importfile.impl.SheetLimitExceededException;
import com.google.gson.JsonObject;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Created by saurabh on 3/24/17.
 */
public class ImportXlsxTest {

    private Workbook mockWb = mock(Workbook.class);
    private TestableXSSFSheet mockSheet = mock(TestableXSSFSheet.class);

    @Mock
    private static Logger logger;


    @Test
    public void shouldReturnCorrectNumberOfSheets() throws Exception {
        Mockito.when(mockWb.getNumberOfSheets()).thenReturn(1);
        Mockito.when(mockWb.getSheetAt(0)).thenReturn(mockSheet);
        Mockito.when(mockSheet.getTotalNumberOfCells()).thenReturn(10);

        JsonObject outputSheets = new JsonObject();
        JsonObject errorObj = new JsonObject();

        ImportXlsx importXlsx = new TestableImportXlsx();

        int actualValue = importXlsx.getTotalCellsCount_AND_LoadOutputSheets(mockWb, outputSheets, errorObj);
        int expectedValue = 10;

        assertTrue(actualValue == expectedValue);
    }

    @Test(expected=SheetLimitExceededException.class)
    public void shouldThrowExceptionWhenXlsxFileHasOver250000Cells() throws SheetLimitExceededException {
        Mockito.when(mockWb.getNumberOfSheets()).thenReturn(1);
        Mockito.when(mockWb.getSheetAt(0)).thenReturn(mockSheet);
        Mockito.when(mockSheet.getTotalNumberOfCells()).thenReturn(250_001);

        JsonObject outputSheets = new JsonObject();
        JsonObject errorObj = new JsonObject();

        ImportXlsx importXlsx = new TestableImportXlsx();

        int actualValue = importXlsx.getTotalCellsCount_AND_LoadOutputSheets(mockWb, outputSheets, errorObj);
    }


    private class TestableImportXlsx extends ImportXlsx {
        TestableImportXlsx(){
        }

        @Override
        public int getTotalCellsCount_AND_LoadOutputSheets(Workbook wb, JsonObject outputSheets, JsonObject warnings) throws SheetLimitExceededException {

            return super.getTotalCellsCount_AND_LoadOutputSheets(wb, outputSheets, warnings);
        }

        @Override
        public int getNumberOfCellsInSheet(Sheet inputSheet) {
            return ((TestableXSSFSheet) inputSheet).getTotalNumberOfCells();
        }

        @Override
        public JsonObject importSheet(XSSFSheet inputSheet, JsonObject warnings) {
            return new JsonObject();
        }
    }

    private class TestableXSSFSheet extends XSSFSheet {

        public int getTotalNumberOfCells() {
            return 0;
        }
    }
}
