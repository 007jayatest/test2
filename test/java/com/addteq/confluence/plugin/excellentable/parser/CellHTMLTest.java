package com.addteq.confluence.plugin.excellentable.parser;

import com.addteq.confluence.plugin.excellentable.TestingUtils;
import com.addteq.service.excellentable.exc_io.json.CellParser;
import com.addteq.service.excellentable.exc_io.parser.CellHTML;
import com.addteq.service.excellentable.exc_io.spreadjs.Cell;
import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * Created by rober on 10/17/2016.
 */
public class CellHTMLTest {

    JsonObject sampleCells;

    @Before
    public void setUp() throws IOException {
        File jsonFile = TestingUtils.getResourceFile(getClass(), "parser/SampleCells.json");
        sampleCells = TestingUtils.readFileIntoJson(jsonFile);
    }

    @Test
    public void testGetValueCorrectlyFromACell() throws IOException {
        
    	Cell cell = CellParser.parse(sampleCells.getAsJsonObject("normal"), 1, 1);
    	CellHTML cellHTML = new CellHTML(cell);

        String expectedValue = "normal value";
        String actualValue = cellHTML.getValue();

        assertTrue(actualValue.equals(expectedValue));
    }

   // @Test
    //public void test

}
