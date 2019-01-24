package com.addteq.service.excellentable.exc_io.json;

import com.addteq.service.excellentable.exc_io.spreadjs.ColRowInfo;
import com.google.gson.JsonObject;

public class ColRowInfoParser extends JsonParser {
	
	private int defaultColRowSize;
	
	public ColRowInfo parse(JsonObject colRowInfoJson){
		
		 
		ColRowInfo info = new ColRowInfo();
		
		if(colRowInfoJson == null){
			info.setSize(getDefaultSize());
			return info;
		}
		
		if(colRowInfoJson.has("resizable")){
			info.isResizable(colRowInfoJson.get("resizable").getAsBoolean());
		}
		
		if(colRowInfoJson.has("size")){
			info.setSize(colRowInfoJson.get("size").getAsNumber().doubleValue());
		}
		else{
			info.setSize(getDefaultSize());
		}
		
		return info;
	}
	
	public double getDefaultSize(){
		return (double)defaultColRowSize;
	}
	public void setDefaultSize(int defaultColRowSize){
		this.defaultColRowSize = defaultColRowSize;
	}

}
