package com.addteq.service.excellentable.exc_io.json;

import com.addteq.service.excellentable.exc_io.spreadjs.Image;
import com.google.gson.JsonObject;

public class ImageParser extends JsonParser{
	
	public Image parse(JsonObject imgJson){
		
		String name = imgJson.get("name").getAsString();
		String src = imgJson.get("src").getAsString();
		
		Image img = new Image(name, src);
		
		if(imgJson.has("x")){
			img.setX(ParserUtils.getAsDouble(imgJson.get("x")));
		}
		
		if(imgJson.has("y")){
			img.setY(ParserUtils.getAsDouble(imgJson.get("y")));
		}
		
		if(imgJson.has("width")){
			img.setWidth(ParserUtils.getAsDouble(imgJson.get("width")));
		}

		if(imgJson.has("height")){
			img.setHeight(ParserUtils.getAsDouble(imgJson.get("height")));
		}
		
		if(imgJson.has("floatingObjectType")){
			img.setFloatingObjectType(imgJson.get("floatingObjectType").getAsInt());		
		}
		
		if(imgJson.has("backColor")){
			img.setBackColor(imgJson.get("backColor").getAsString());		
		}
		
		if(imgJson.has("borderStyle")){
			img.setBorderStyle(imgJson.get("borderStyle").getAsString());		
		}
		
		if(imgJson.has("borderColor")){
			img.setBorderColor(imgJson.get("borderColor").getAsString());		
		}
		
		if(imgJson.has("borderRadius")){
			img.setBorderRadius(imgJson.get("borderRadius").getAsInt());		
		}
		
		if(imgJson.has("isSelected")){
			img.setSelected(imgJson.get("isSelected").getAsBoolean());
		}
		if(imgJson.has("startColumn")){
			img.setStartColumn(imgJson.get("startColumn").getAsInt());		
		}
		
		if(imgJson.has("startColumnOffset")){
			img.setStartColumnOffset(imgJson.get("startColumnOffset").getAsInt());		
		}
		
		if(imgJson.has("startRow")){
			img.setStartRow(imgJson.get("startRow").getAsInt());		
		}
		
		if(imgJson.has("startRowOffset")){
			img.setStartRowOffset(imgJson.get("startRowOffset").getAsInt());		
		}
		
		return img;
		
	}

}
