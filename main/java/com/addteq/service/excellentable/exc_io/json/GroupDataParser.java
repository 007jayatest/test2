package com.addteq.service.excellentable.exc_io.json;

import com.addteq.service.excellentable.exc_io.spreadjs.GroupData;
import com.addteq.service.excellentable.exc_io.spreadjs.GroupDataInfo;
import com.google.gson.JsonObject;

public class GroupDataParser extends JsonParser{

	public GroupData parse(JsonObject groupDataJson){
		
		int index = groupDataJson.get("index").getAsInt();
		int count = groupDataJson.get("count").getAsInt();
		GroupDataInfo info = GroupDataInfoParser.parse(groupDataJson.getAsJsonObject("info"));
		
		return new GroupData(index, count, info);
	}
}