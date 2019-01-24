package com.addteq.service.excellentable.exc_io.json;

import com.addteq.service.excellentable.exc_io.spreadjs.Cell;
import com.addteq.service.excellentable.exc_io.spreadjs.CellStyle;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class CellParser {
	
	private static final String STYLE = "style";
	private static final String VALUE = "value";
	private static final String FORMULA = "formula";
	
	public static Cell parse(JsonObject excCellJson, int row, int col){
		
		String value = "";
				
		if(excCellJson.has(VALUE)){
			
			JsonElement valueElement = excCellJson.get(VALUE);
			
			if(valueElement.isJsonPrimitive()){
				value = excCellJson.get(VALUE).getAsString();
			}
		}
		
		Cell cell = new Cell(value, row, col);
		
		if(excCellJson.has(FORMULA)){
			cell.setFormula(excCellJson.get(FORMULA).getAsString());
		} 
		
		if(excCellJson.has(STYLE)){
			CellStyle cellStyle = CellStyleParser.parse(excCellJson.getAsJsonObject(STYLE));
			cell.setStyle(cellStyle);
		}
		
		return cell;
		 
	}

}