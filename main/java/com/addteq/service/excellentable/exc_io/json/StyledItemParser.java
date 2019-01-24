package com.addteq.service.excellentable.exc_io.json;

import com.addteq.service.excellentable.exc_io.spreadjs.CellStyle;
import com.addteq.service.excellentable.exc_io.spreadjs.StyledItem;
import com.google.gson.JsonObject;

public class StyledItemParser extends JsonParser{

	private static final String STYLE = "style";

	public StyledItem parse(JsonObject styleJson){
			
		if(styleJson == null) {return null;}
		
		StyledItem styledItem = null ;
			
			if(styleJson.has(STYLE)){
				
				CellStyle style = CellStyleParser.parse(styleJson.getAsJsonObject(STYLE));
				styledItem = new StyledItem(style);
			}
			
			return styledItem;
			 
		}
}
