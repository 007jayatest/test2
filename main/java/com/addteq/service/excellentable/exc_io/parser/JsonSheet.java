package com.addteq.service.excellentable.exc_io.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class JsonSheet {
	
	private double defaultHeight = 25.0;
	private double defaultWidth = 145.0;
	private JsonArray colswidth;
	private JsonArray rowsHeight;
	private int rowsSize;
	private int colSize;
	private JsonObject tableData;
	private JsonArray tableStyles;
	private JsonArray images;
	private JsonArray tableSpans;
	
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(JsonSheet.class);
	
	
	public JsonSheet(String metaData){
		
		JsonObject sheet = getSheet(metaData);
		colSize = sheet.get("columnCount").getAsInt();
		rowsSize = sheet.get("rowCount").getAsInt();
		
		if(sheet.has("data") && sheet.getAsJsonObject("data").has("dataTable")){		
			tableData = sheet.getAsJsonObject("data").getAsJsonObject("dataTable");
		}
		
		if(sheet.has("tables")){
			tableStyles =  sheet.getAsJsonArray("tables");
		}
		else{
			tableStyles = new JsonArray();
		}
		if(sheet.has("rows")){
			rowsHeight =  sheet.getAsJsonArray("rows");
		}
		if(sheet.has("columns")){
			colswidth =  sheet.getAsJsonArray("columns");
		}
		if(sheet.has("floatingObjects")){
			images = sheet.getAsJsonArray("floatingObjects");
		}
		else{
			images = new JsonArray();
		}
		if(sheet.has("spans")){
			tableSpans = sheet.getAsJsonArray("spans");
		}
		
		
	}
	
	public JsonArray getSpans(){
		
		return tableSpans;
	}
	
	public JsonArray getImages(){
		return images;
	}
	
	public double getRowHeight(int rowIndex){
		
		try{
			return rowsHeight.get(rowIndex).getAsJsonObject().get("size").getAsDouble();
		}
		catch(Exception e){
			log.info("defaul row height for row index: " + rowIndex);
			return defaultHeight;
		}
		
	}
	
	public double getColWidth(int colIndex){
		
		try{
			// -20 padding
			return colswidth.get(colIndex).getAsJsonObject().get("size").getAsDouble();
		}
		catch(Exception e){
			log.info("defaul column width for col index: " + colIndex);
			return defaultWidth;
		}
		
	}

	public JsonArray getTableStyles(){
		return this.tableStyles;
	}
	
	public JsonObject getTableData(){
		return this.tableData;
	}
	
	public int getRowSize(){	
		return this.rowsSize;
	}
	
	public int getColSize(){
		return this.colSize;
	}
	
	private  JsonObject getSheet(String metaData){
		JsonObject jsonSheet = new JsonObject();
		JsonParser jsonParser = new JsonParser();
		JsonObject sheets = ((JsonObject) jsonParser.parse(metaData)).getAsJsonObject("sheets");
		
        try{
        	
        	for (Map.Entry<String, JsonElement> entry : sheets.entrySet()) {
            	
        		String sheetName = entry.getKey();
        		jsonSheet =  sheets.getAsJsonObject(sheetName);
        	}
    		
        }
        catch(Exception e){
        	log.info("Unable to get sheet for metadata " + metaData);
        }
        return jsonSheet;
	}
	

}
