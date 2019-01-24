package com.addteq.service.excellentable.exc_io.json;

import com.addteq.service.excellentable.exc_io.spreadjs.Range;
import com.addteq.service.excellentable.exc_io.spreadjs.RowFilter;
import com.google.gson.JsonObject;

public class RowFilterParser {
	
	public static RowFilter parse(JsonObject rowFilter){
		RowFilter filter = new RowFilter();
		
		Range range = ParserUtils.getRangeParser().parse(rowFilter.getAsJsonObject("range"));
		filter.setRange(range);
		
		if(rowFilter.has("showFilterButton")){
			filter.setShowFilterButton(rowFilter.get("showFilterButton").getAsBoolean());
			
		}
		
		if(rowFilter.has("filterButtonVisibleInfo")){
			
			rowFilter.getAsJsonObject("filterButtonVisibleInfo").entrySet().forEach(entry -> {
				int index = Integer.parseInt( entry.getKey());
				boolean visible = entry.getValue().getAsBoolean();
				filter.addFilterButton(index, visible);
			});
				
		
		}
		return filter;
	}

}
