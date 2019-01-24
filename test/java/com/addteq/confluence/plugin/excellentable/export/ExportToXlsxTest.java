package com.addteq.confluence.plugin.excellentable.export;

import com.addteq.confluence.plugin.excellentable.TestingUtils;
import com.addteq.service.excellentable.exc_io.utils.ToCSV;
import com.addteq.service.excellentable.exc_io.export.ExportToXlsx;
import com.google.gson.JsonObject;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.apache.poi.xssf.usermodel.XSSFPictureData;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Test;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;

import static org.junit.Assert.*;

/**
 * Created by rober on 8/11/2016.
 */
public class ExportToXlsxTest {

    private JsonObject spreadJson;
    private XSSFWorkbook xlsx;
    private XSSFSheet sheet;
    private static final int TWIPS_TO_PX = 15;  //Twips to pixel conversion (1 pixel = 15 twips)

    // This link contians the Excellentable instance used for test data
    // All hardcoded data referred below is defined in it
    // https://nebula.addteq.com/display/9effa87ff4f4d5ab20e489c6187e46f8/Excellentable+Instance+for+Unit+tests

    @Before
    public void createXlsxWorkbook_FromSampleSpreadJson() throws Exception {
        File jsonFile = TestingUtils.getResourceFile(getClass(), "export/SampleTable1.json");

        this.spreadJson = TestingUtils.readFileIntoJson(jsonFile);
        this.xlsx = new ExportToXlsx().createWorkbook(this.spreadJson);
        this.sheet = this.xlsx.getSheetAt(0);      // Single sheet approach
    }

    @Test
    public void DataInSpreadJSONIsExportedToXLSX() throws Exception {
        // When converted to CSV, only data is maintained, perfect for this test.
        String expectedCSV = ToCSV.fromSpreadJson(this.spreadJson);
        String actualCSV = ToCSV.fromWorkbook(this.xlsx);

        assertTrue(expectedCSV.equals(actualCSV));
    }

    @Test
    public void MergedRegionsInSpreadJSONAreExportedToXLSX() throws Exception {
        // Remember to check nebula link to find where this test data comes from

        // First check we have the correct number of regions
        int expectedNumOfMergedRegions = 2;
        int actualNumberOfMergedRegions = this.sheet.getNumMergedRegions();
        assertEquals(expectedNumOfMergedRegions, actualNumberOfMergedRegions);

        // Now check the regions rows and columns are correct
        boolean mergedRegionsAreCorrect;

        CellRangeAddress mergedRegion1 = this.sheet.getMergedRegion(0);
        mergedRegionsAreCorrect = (mergedRegion1.formatAsString().equals("B7:C9"));

        CellRangeAddress mergedRegion2 = this.sheet.getMergedRegion(1);
        mergedRegionsAreCorrect = (mergedRegion2.formatAsString().equals("D3:D9"));

        assertTrue(mergedRegionsAreCorrect);
    }

    @Test
    public void ResizedRowsInSpreadJSONAreExportedToXLSX() {
        short expectedHeight_Row2 = (short) (this.sheet.getRow(1).getHeight()/TWIPS_TO_PX);
        assertTrue(expectedHeight_Row2 == 55);

        short expectedHeight_Row6 = (short) (this.sheet.getRow(5).getHeight()/TWIPS_TO_PX);
        assertTrue(expectedHeight_Row6 == 72);
    }

    // Test for column width - to clarify calculation made

    // TODO: Decide if comment style should also be tested
    // It's simple logic and deals with implementation, not just behavior
    @Test
    public void CellCommentsAreExportedToXLSX() {
        XSSFComment comment1 = this.sheet.getCellComment(5, 0);
        assertNotNull(comment1);

        // XSSFComment.getString() returns a XSSFRichTextString
        // This needs another getString() to convert to a proper String
        String comment1_text = comment1.getString().getString();
        assertTrue(comment1_text.equals("Test Note 1"));

        // Do the same for the second comment that should be here
        XSSFComment comment2 = this.sheet.getCellComment(0, 3);
        assertNotNull(comment2);

        String comment2_text = comment2.getString().getString();
        assertTrue(comment2_text.equals("Multi\nLine\nTest\nNote"));
    }

    // TODO: Add test for the image size and position in sheet
    // Apache POI methods make this seemingly untestable in a nice way, have to modify ExportToXlsx
    @Test
    public void InsertedImagesAreExportedToXLSX() throws IOException {
        
        List<XSSFPictureData> allPictures = this.xlsx.getAllPictures();
        byte[] data = allPictures.get(0).getData();
        BufferedImage actualImage = ImageIO.read(new ByteArrayInputStream(data));
        int actualHeight = actualImage.getHeight();//height of the image from sheet
        int actualWidth = actualImage.getWidth();//width of the image from sheet
        int expectedHeight = 465; // height from json of floaitng object
        int expectedWidth = 491; //width from json of floating object
        
        assertEquals(expectedHeight, actualHeight);
        assertEquals(expectedWidth,actualWidth );
        assertEquals(1,allPictures.size());
    }
}  