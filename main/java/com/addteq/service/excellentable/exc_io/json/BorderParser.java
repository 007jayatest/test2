package com.addteq.service.excellentable.exc_io.json;

import com.addteq.service.excellentable.exc_io.spreadjs.Border;
import com.google.gson.JsonObject;

public class BorderParser {
	
	public static Border parse(JsonObject borderJson){
		
		if(!borderJson.has("style") || !borderJson.has("color")) {
			return null;
		}
		
		int style = borderJson.get("style").getAsInt();
		String color = borderJson.get("color").getAsString();
		return new Border(style, color);
	}

}
