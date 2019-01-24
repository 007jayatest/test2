package com.addteq.service.excellentable.exc_io.json;

import com.addteq.service.excellentable.exc_io.spreadjs.TableStyleHeader;
import com.google.gson.JsonObject;

public class TableStyleHeaderParser extends JsonParser {

	public TableStyleHeader parse(JsonObject headerJson){
		
		TableStyleHeader header = new TableStyleHeader();
		header.setId(headerJson.get("id").getAsInt());
		header.setName(headerJson.get("name").getAsString());
		return header;
	}
}