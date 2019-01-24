package com.addteq.service.excellentable.exc_io.json;

import com.addteq.service.excellentable.exc_io.spreadjs.LinkCellType;
import com.google.gson.JsonObject;

public class LinkCellTypeParser {

	public static LinkCellType parse(JsonObject linkJson){

		LinkCellType link = new LinkCellType();
		
		if( linkJson.has("text")){
			link.setText(linkJson.get("text").getAsString());
		}
		
		if(linkJson.has("linkToolTip")){
			link.setLinkToolTip(linkJson.get("linkToolTip").getAsString());
		}
				
			
		return link;
	}
}
