package com.addteq.service.excellentable.exc_io.json;

import com.addteq.service.excellentable.exc_io.spreadjs.Padding;
import com.google.gson.JsonObject;

public class PaddingParser {
	
	public static Padding parse(JsonObject paddingJson){
	
		int left = paddingJson.get("left").getAsInt();
		int right = paddingJson.get("right").getAsInt();
		int top = paddingJson.get("top").getAsInt();
		int bottom = paddingJson.get("bottom").getAsInt();
		
		return new Padding(left, right, top, bottom);
	}

}
