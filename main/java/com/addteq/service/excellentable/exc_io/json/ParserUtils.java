package com.addteq.service.excellentable.exc_io.json;

import com.google.gson.JsonElement;

public class ParserUtils {
	private static RangeParser rangeParser = new RangeParser();
	
	public static double getAsDouble(JsonElement numJson){
		String numS = numJson.getAsString();
		return Double.parseDouble(numS);
	}

	public static RangeParser getRangeParser(){
		return rangeParser;
	} 
}
