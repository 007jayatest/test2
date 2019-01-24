package com.addteq.service.excellentable.exc_io.json;

import com.addteq.service.excellentable.exc_io.spreadjs.GroupDataInfo;
import com.google.gson.JsonObject;

public class GroupDataInfoParser {
	
	public static GroupDataInfo parse(JsonObject groupDataInfoJson){
		
		int level = groupDataInfoJson.get("level").getAsInt();
		boolean collapsed = groupDataInfoJson.get("collapsed").getAsBoolean();
		
		return new GroupDataInfo(level, collapsed);
	}
}