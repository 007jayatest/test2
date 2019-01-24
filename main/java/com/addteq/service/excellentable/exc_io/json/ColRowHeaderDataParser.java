package com.addteq.service.excellentable.exc_io.json;

import com.addteq.service.excellentable.exc_io.spreadjs.Cell;
import com.addteq.service.excellentable.exc_io.spreadjs.ColHeaderData;
import com.addteq.service.excellentable.exc_io.spreadjs.ColRowHeaderData;
import com.addteq.service.excellentable.exc_io.spreadjs.RowHeaderData;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ColRowHeaderDataParser {

	public static ColRowHeaderData parse(JsonObject json, String colRow){
		
		ColRowHeaderData colRowHeader = null;
		
		JsonArray colRowJson = json.getAsJsonArray(colRow);
		JsonObject defaultNode = json.getAsJsonObject("defaultDataNode");

		if("columnDataArray".equals(colRow)){
			colRowHeader = new RowHeaderData();
		}
		else{
			colRowHeader = new ColHeaderData();
		}
		
		if(colRowJson != null){
			for(JsonElement el : colRowJson){
				Cell cell = CellParser.parse(el.getAsJsonObject(), 0, 0);
				colRowHeader.addData(cell.getStyle());
			}
		}
		
		if(defaultNode != null){
			
			Cell defaultCellNode = CellParser.parse(defaultNode, 0, 0);
			colRowHeader.setDefaultDataNode(defaultCellNode.getStyle());
		}
		
		return colRowHeader;
	}

		
		
}