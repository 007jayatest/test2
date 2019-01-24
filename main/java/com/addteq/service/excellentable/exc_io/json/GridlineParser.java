package com.addteq.service.excellentable.exc_io.json;

import com.addteq.service.excellentable.exc_io.spreadjs.Gridline;
import com.google.gson.JsonObject;

public class GridlineParser {

	public static Gridline parse(JsonObject gridJson){
	
		Gridline grid = new Gridline();
		if(gridJson.has("color")){
			grid.setColor(gridJson.get("color").getAsString());
		}
		
		if(gridJson.has("showVerticalGridline")){
			grid.setShowVerticalGridline(gridJson.get("showVerticalGridline").getAsBoolean());
		}
		
		if(gridJson.has("showHorizontalGridline")){
			grid.setShowHorizontalGridline(gridJson.get("showHorizontalGridline").getAsBoolean());
		}
		
		return grid;
	}
}
