package com.addteq.service.excellentable.exc_io.json;

import com.addteq.service.excellentable.exc_io.spreadjs.Data;
import com.addteq.service.excellentable.exc_io.spreadjs.StyledItem;
import com.google.gson.JsonObject;

public class DataParser {

	public static Data parse(JsonObject jsonData){
		
		Data data = new Data();
		if(jsonData.has("dataTable")){
			data.setDataTable(jsonData.getAsJsonObject("dataTable"));
		}
		
		JsonArrayParser<StyledItem> styleItemsParser = new JsonArrayParser<StyledItem>();
		StyledItemParser styledItemParser = new StyledItemParser();
		
		if(jsonData.has("columnDataArray")){
			
			data.setColumnDataArray(styleItemsParser.parse(jsonData.getAsJsonArray("columnDataArray"), styledItemParser));
		}
		
		if(jsonData.has("rowDataArray")){
			
			data.setRowDataArray(styleItemsParser.parse(jsonData.getAsJsonArray("rowDataArray"), styledItemParser));
		}
		
		if(jsonData.has("defaultDataNode")){
			//TODO data.setDefaultDataNode(jsonData.getAsJsonObject("defaultDataNode"));
		}
		
		return data;
		
	}
}
