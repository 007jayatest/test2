package com.addteq.service.excellentable.exc_io.json;

import com.addteq.service.excellentable.exc_io.spreadjs.Range;
import com.google.gson.JsonObject;

public class RangeParser extends JsonParser {
	
	public Range parse(JsonObject rangeJson){
		int row = rangeJson.get("row").getAsInt();
		int col = rangeJson.get("col").getAsInt();
		int rowCount = rangeJson.get("rowCount").getAsInt();
		int colCount = rangeJson.get("colCount").getAsInt();
		
		return new Range(row, col, rowCount, colCount);
	}

}
