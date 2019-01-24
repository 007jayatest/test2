package com.addteq.service.excellentable.exc_io.importfile.impl;

import com.addteq.service.excellentable.exc_io.importfile.CellStyle;
import com.addteq.service.excellentable.exc_io.importfile.Importable;
import com.addteq.service.excellentable.exc_io.spreadjs.Data;
import com.addteq.service.excellentable.exc_io.spreadjs.ExcellentableSpread;
import com.atlassian.botocss.Botocss;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.LoggerFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.addteq.service.excellentable.exc_io.spreadjs.Sheet;

import java.util.List;

public class ImportHtml implements Importable {

    private org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ImportHtml.class);

    private JsonObject dataTable = new JsonObject();

    private int rowCount = 0;
    private int columnCount = 1;
    private static final String HYPERLINK_TYPE_NAME = "8";  //Cell type name for hyperlink in spreadjs

    @Override
    public JsonObject buildImportSheetJson(Object sheetData, String version)
            throws Exception {
    	 InputStream importFile = null;
         
         if(sheetData.getClass().equals(String.class)) {
         	String sheetDataString = (String)sheetData;

         	importFile  = new ByteArrayInputStream(sheetDataString.getBytes(StandardCharsets.UTF_8));
         }
         else{
         	importFile  = (InputStream) sheetData;
         }
        
        ExcellentableSpread spread = new ExcellentableSpread();          
        Sheet sheet = new Sheet();
        Data data = new Data();
        
        String ENCODE = "UTF-8";
        String html = IOUtils.toString(importFile, ENCODE);

        html = Botocss.inject(html);

        Document htmlDoc = Jsoup.parse( html, "");
     // google sheet headers
        htmlDoc.select(".column-headers-background, .row-headers-background").remove();
        
        convertHtmlToJson(htmlDoc.select("body").first());
        sheet.setName("Sheet1");
        sheet.setRowCount(rowCount);
        sheet.setColCount(columnCount);
        data.setDataTable(dataTable);
        sheet.setData(data);

        spread.getSheets().put("Sheet1", sheet);
        Gson gson = new Gson();
        JsonParser jsonParser = new JsonParser();
        JsonObject jo = (JsonObject) jsonParser.parse(gson.toJson(spread));
        
        return jo;
    }

    private void convertHtmlToJson(Element e) {

        List<Element> children = e.children();
        String row = String.valueOf(rowCount);

        if (children.isEmpty()) {

            if (!"".equals(e.text())) {

                dataTable.add(row, getColData(e.text(), e));
                rowCount++;
            }
        }
        else{

            for (Element child : children) {
                if ("table".equals(child.tagName())) {

                    addTable(child);
                    rowCount++;
                }
                else{
                    convertHtmlToJson(child);
                }
            }
        }
    }

    private JsonObject getColData(String val, Element el) {

        JsonObject colData = new JsonObject();
        JsonObject value = new JsonObject();

        value.addProperty("value", val);
        value.add("style", getCellStyle(el).toJson());
        colData.add("0", value);

        return colData;

    }

    private JsonObject getColDataForTds(List<Element> tds, int size) {

        JsonObject tdsData = new JsonObject();

        for (int i = 0; i < size; i++) {

            Element td = tds.get(i);
            String val = td.text();
            JsonObject data = new JsonObject();

            data.addProperty("value", val);
            data.add("style", getCellStyle(td).toJson());
            getHyperlink(td, data, val);
            tdsData.add(String.valueOf(i), data);
        }

        return tdsData;
    }
    /**
     * If any hyperlink in the cell then put to the json object response
     */
    private void getHyperlink(Element td, JsonObject data, String val) {
        JsonObject linkObj = new JsonObject();
        JsonObject cellstyle = new JsonObject();
        if (data.has("style")) {
            cellstyle = (JsonObject) data.get("style");
        }
        Elements anchorTag = td.getElementsByTag("a");
        String url = td.getAllElements().attr("href");
        if (!anchorTag.isEmpty() && anchorTag.text().trim().equals(val.trim())) {
            data.addProperty("value", url);
            linkObj.addProperty("typeName", HYPERLINK_TYPE_NAME);
            linkObj.addProperty("text", val);
            linkObj.addProperty("linkToolTip", url);
            cellstyle.add("cellType", linkObj);
            data.add("style", cellstyle);
        }

    }

    private CellStyle getCellStyle(Element td) {
        CellStyle style = new CellStyle(td);

        style.applyStyleAttributes(td);
        style.applyHighlightColor();
        style.additionalFormatting();
        return style;
    }

    private void addTable(Element table) {

        List<Element> rows = table.select("tr");

        for (Element row : rows) {

            List<Element> tds = row.select("td, th");
            int tdsSize = tds.size();
            JsonObject tdsData;

            if (tdsSize > columnCount) {
                columnCount = tdsSize;
            }

            tdsData = getColDataForTds(tds, tdsSize);

            dataTable.add(String.valueOf(rowCount), tdsData);
            rowCount++;
        }
    }
}
