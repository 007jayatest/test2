package com.addteq.confluence.plugin.excellentable.export;

import com.addteq.confluence.plugin.excellentable.TestingUtils;
import com.addteq.service.excellentable.exc_io.utils.ToCSV;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * Created by rober on 8/11/2016.
 */
public class SpreadJSON2CSVTest {

    @Test
    public void TableInJSONIsProperlyConvertedToCSV() throws IOException {
        // Data used to validate this test. Converted JSON should be equal to CSV
        File jsonFile = TestingUtils.getResourceFile(getClass(), "export/SampleTable1.json");
        File csvFile = TestingUtils.getResourceFile(getClass(), "export/SampleTable1.csv");

        String expected = TestingUtils.readFileIntoString(csvFile);
        String actual   = ToCSV.fromSpreadJson(TestingUtils.readFileIntoJson(jsonFile));

        assertTrue(expected.equals(actual));
    }


}
