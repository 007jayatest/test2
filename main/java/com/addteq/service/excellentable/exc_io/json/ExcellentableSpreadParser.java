package com.addteq.service.excellentable.exc_io.json;

import com.addteq.service.excellentable.exc_io.spreadjs.ExcellentableSpread;
import com.addteq.service.excellentable.exc_io.spreadjs.Sheet;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Map;

public class ExcellentableSpreadParser {

	public static ExcellentableSpread parse(String metaData){
		
		ExcellentableSpread spread = new ExcellentableSpread();
		
		if(metaData == null || metaData.isEmpty()){return null;}
		
		JsonParser jsonParser = new JsonParser();
		
        JsonObject metaJson = (JsonObject) jsonParser.parse(metaData);
        
        if(metaJson.has("version")){
        	spread.setVersion(metaJson.get("version").getAsString());
        }
        Map<String, Sheet> sheets = SheetsParser.parse(metaJson.getAsJsonObject("sheets"));
        spread.setSheets(sheets);
        
        if(metaJson.has("tabStripVisible")){
            spread.setTabStripVisible(metaJson.get("tabStripVisible").getAsBoolean());
        }
        
        if(metaJson.has("newTabVisible")){
        	spread.setNewTabVisible(metaJson.get("newTabVisible").getAsBoolean());
        }
        
        if(metaJson.has("showScrollTip")){
        	spread.setShowScrollTip(metaJson.get("showScrollTip").getAsInt());
        }
        
        if(metaJson.has("showVerticalScrollBar")){
        	spread.setShowVerticalScrollBar(metaJson.get("showVerticalScrollBar").getAsBoolean());
        }
        
        if(metaJson.has("scrollbarMaxAlign")){
        	spread.setScrollbarMaxAlign(metaJson.get("scrollbarMaxAlign").getAsBoolean());
        }
        
        if(metaJson.has("grayAreaBackColor")){
        	spread.setGrayAreaBackColor(metaJson.get("grayAreaBackColor").getAsString());
        }
         
		return spread;
		
	}
}