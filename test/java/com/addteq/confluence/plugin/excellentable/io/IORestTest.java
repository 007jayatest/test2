package com.addteq.confluence.plugin.excellentable.io;

import com.addteq.service.excellentable.exc_io.importfile.Importable;
import com.addteq.service.excellentable.exc_io.importfile.impl.ImportXlsx;
import com.google.gson.JsonObject;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({IORest.class, ServletFileUpload.class})
class IORestTest {

    public IORestTest(){};
    ImportFile importFile = new ImportFile(null);
    IORest ioRest = new IORest(importFile, null, null);

    @Test
    public void importData() throws Exception {
        //Create a work with sample data
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");
        Row row  = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setAsActiveCell();
        cell.setCellValue(100);//Numeric value
        cell = row.createCell(1);
        cell.setCellValue("Testing");//String value
        cell = row.createCell(2);
        cell.setCellValue(true);//Boolean value
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date date = sdf.parse("23/03/2018");
        cell = row.createCell(3);
        cell.setCellValue(date);//Date value

        //Import the file and get Output
        Importable importfile = new ImportXlsx();
        JsonObject jsonObject = importfile.buildImportSheetJson(workbook,"9.40.20161.5");

        //Assert values
        Assert.assertNotNull(jsonObject);
        JsonObject sheets = jsonObject.getAsJsonObject("sheets");
        Assert.assertNotNull(sheets);
        JsonObject sheet1 = sheets.getAsJsonObject("Sheet1");
        Assert.assertNotNull(sheet1);
        JsonObject data = sheet1.getAsJsonObject("data");
        Assert.assertNotNull(data);
        JsonObject dataTable = data.getAsJsonObject("dataTable");
        Assert.assertNotNull(dataTable);
        JsonObject row0 = dataTable.getAsJsonObject("0");
        Assert.assertNotNull(row0);
        JsonObject col0 = row0.getAsJsonObject("0");
        Assert.assertNotNull(col0);
        int col0Val = col0.get("value").getAsInt();
        Assert.assertNotNull(col0Val);
        Assert.assertEquals(100, col0Val);
        Assert.assertEquals("Testing", row0.getAsJsonObject("1").get("value").getAsString());
        Assert.assertEquals(true, row0.getAsJsonObject("2").get("value").getAsBoolean());
        Assert.assertEquals("43182.0", row0.getAsJsonObject("3").get("value").getAsString());

    }

    @Test
    public void exportData() throws Exception {
        //Mock request
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContentType()).thenReturn("multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW");
        when(request.getHeader("Accept"))
                .thenReturn("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        FileItem item0 = mock(FileItem.class);//type
        FileItem item1 = mock(FileItem.class);//data
        FileItem item2 = mock(FileItem.class);//name
        List<FileItem> formItems = new ArrayList<>();
        formItems.add(item0);
        formItems.add(item1);
        formItems.add(item2);
        when(item0.isFormField()).thenReturn(true);
        when(item1.isFormField()).thenReturn(true);
        when(item2.isFormField()).thenReturn(true);
        when(item0.getFieldName()).thenReturn("type");
        when(item1.getFieldName()).thenReturn("data");
        when(item2.getFieldName()).thenReturn("name");
        when(item0.getString()).thenReturn("xlsx");
        when(item1.getString(StandardCharsets.UTF_8.name())).thenReturn("{\"version\":\"9.40.20161.5\",\"tabEditable" +
                "\":false,\"newTabVisible\":false,\"showScrollTip\":3,\"showHorizontalScrollbar\":false,\"showVertica" +
                "lScrollbar\":false,\"scrollbarMaxAlign\":true,\"grayAreaBackColor\":\"Transparent\",\"tabNavigationV" +
                "isible\":false,\"sheets\":{\"Sheet2\":{\"name\":\"Sheet2\",\"selections\":{\"length\":0,\"activeSele" +
                "ctedRangeIndex\":-1},\"defaults\":{\"rowHeight\":25,\"colWidth\":150,\"rowHeaderColWidth\":40,\"colH" +
                "eaderRowHeight\":20},\"rowCount\":4,\"columnCount\":3,\"selectionBackColor\":\"transparent\",\"activ" +
                "eRow\":0,\"activeCol\":0,\"allowCellOverflow\":true,\"theme\":\"Office\",\"colHeaderRowInfos\":[{\"s" +
                "ize\":25}],\"isProtected\":true,\"protectionOption\":{\"allowFilter\":true,\"allowSort\":true,\"allo" +
                "wResizeRows\":true,\"allowResizeColumns\":true,\"allowEditObjects\":false},\"rowHeaderData\":{\"colu" +
                "mnDataArray\":[{\"style\":{\"font\":\"14.6667px Verdana\"}}],\"defaultDataNode\":{\"style\":{\"theme" +
                "Font\":\"Body\"}}},\"colHeaderData\":{\"rowDataArray\":[{\"style\":{\"font\":\"14.6667px Verdana\"}}" +
                "],\"defaultDataNode\":{\"style\":{\"themeFont\":\"Body\"}}},\"data\":{\"dataTable\":{\"0\":{\"0\":{" +
                "\"value\":\"Test\",\"style\":{\"autoFormatter\":{}}},\"1\":{\"value\":1,\"style\":{\"autoFormatter\"" +
                ":{}}},\"2\":{\"value\":2,\"style\":{\"autoFormatter\":{}}}},\"1\":{\"0\":{\"value\":\"a\",\"style\":" +
                "{\"autoFormatter\":{}}},\"1\":{\"value\":4,\"style\":{\"autoFormatter\":{}}},\"2\":{\"value\":5,\"st" +
                "yle\":{\"autoFormatter\":{}}}},\"2\":{\"0\":{\"value\":\"b\",\"style\":{\"autoFormatter\":{}}},\"1\"" +
                ":{\"value\":6,\"style\":{\"autoFormatter\":{}}},\"2\":{\"value\":7,\"style\":{\"autoFormatter\":{}}}" +
                "},\"3\":{\"0\":{\"value\":\"c\",\"style\":{\"autoFormatter\":{}}},\"1\":{\"value\":8,\"style\":{\"au" +
                "toFormatter\":{}}},\"2\":{\"value\":9,\"style\":{\"autoFormatter\":{}}}}},\"columnDataArray\":[{\"st" +
                "yle\":{\"vAlign\":1,\"textIndent\":1}},{\"style\":{\"vAlign\":1,\"textIndent\":1}},{\"style\":{\"vAl" +
                "ign\":1,\"textIndent\":1}}],\"defaultDataNode\":{\"style\":{\"font\":\"13.3333px Verdana\"}}},\"inde" +
                "x\":0}}}");
        when(item2.getString()).thenReturn("TestFile");

        //Basic export test xlsx
        ServletFileUpload servletFileUpload = mock(ServletFileUpload.class);
        PowerMockito.whenNew(ServletFileUpload.class).withAnyArguments().thenReturn(servletFileUpload);
        PowerMockito.doReturn(formItems).when(servletFileUpload).parseRequest(request);
        IORest ioRest = new IORest(null, null, null);
        Response response = ioRest.exportXLSX(request);
//        TODO initialize the object returned from response and check for values from a1:a4 and b1:b4
//        assertTrue(FileUtils.contentEquals(expectedResult, actualResult));
    }

    private File writeToFile(Response response) throws IOException {
        StreamingOutput streamingOutput = (StreamingOutput) response.getEntity();
        File file = File.createTempFile("IORestActual", ".xlsx");
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        streamingOutput.write(fileOutputStream);
        return file;
    }
    private String writeToString(Response response) throws UnsupportedEncodingException {
        StreamingOutput streamingOutput = (StreamingOutput) response.getEntity();
        ByteArrayOutputStream byteArrayOutputStream = (ByteArrayOutputStream)streamingOutput;
        return new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8.name());
    }
}