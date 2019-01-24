package com.addteq.service.excellentable.exc_io.json;

import com.addteq.service.excellentable.exc_io.spreadjs.CellFormatter;
import com.google.gson.JsonObject;

public class CellFormatterParser {

	public static CellFormatter parse(JsonObject jsonFormatter){
		
		CellFormatter formatter = null;
		
		if(jsonFormatter.has("formatCached")){
			formatter = new CellFormatter();
			
			formatter.setFormatCached(jsonFormatter.get("formatCached").getAsString());
			if(jsonFormatter.has("customerCultureName")){
				formatter.setCustomerCultureName(jsonFormatter.get("customerCultureName").getAsString());
			}
		}
	
		return formatter;
		
	}
}