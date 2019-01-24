package com.addteq.service.excellentable.exc_io.json;

import com.addteq.service.excellentable.exc_io.spreadjs.Range;
import com.addteq.service.excellentable.exc_io.spreadjs.RowFilter;
import com.addteq.service.excellentable.exc_io.spreadjs.TableStyle;
import com.addteq.service.excellentable.exc_io.spreadjs.TableStyleHeader;
import com.google.gson.JsonObject;

import java.util.List;

public class TableStyleParser extends JsonParser{
	
	public TableStyle parse(JsonObject tableJson){
		
				Range range = ParserUtils.getRangeParser().parse(tableJson);
				TableStyle tableStyle = new TableStyle(range.getRow(), range.getCol(), 
														range.getRowCount(), range.getColCount());
				
				tableStyle.setName(tableJson.get("name").getAsString());
				
				JsonArrayParser<TableStyleHeader> arrayParser = new JsonArrayParser<TableStyleHeader>();
				List<TableStyleHeader> columns = arrayParser.parse(tableJson.get("columns").getAsJsonArray(), new TableStyleHeaderParser() );
				tableStyle.setHeaders(columns);
				
				if(tableJson.has("rowFilter")){
					RowFilter filter = RowFilterParser.parse(tableJson.getAsJsonObject("rowFilter"));
					tableStyle.setRowFilter(filter);
				}
				
				if(tableJson.has("bandColumns")){
					tableStyle.setBandColumns(tableJson.get("bandColumns").getAsBoolean());
				}
				
				if(tableJson.has("bandRows")){
					tableStyle.setBandRows(tableJson.get("bandRows").getAsBoolean());
				}
				
				if(tableJson.has("showFooter")){
					tableStyle.setShowFooter(tableJson.get("showFooter").getAsBoolean());
				}

				if(tableJson.has("showHeader")){
					tableStyle.setShowHeader(tableJson.get("showHeader").getAsBoolean());
				}

				if(tableJson.has("highLightFirst")){
					tableStyle.setHighLightFirst(tableJson.get("highLightFirst").getAsBoolean());
				}
				
				if(tableJson.has("highLightLast")){
					tableStyle.setHighLightLast(tableJson.get("highLightLast").getAsBoolean());
				}
				if(tableJson.has("style")){
					tableStyle.setStyle(tableJson.getAsJsonObject("style"));
				}
			
				return tableStyle;
	}

}
