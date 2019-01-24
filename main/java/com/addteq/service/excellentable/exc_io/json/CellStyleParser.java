package com.addteq.service.excellentable.exc_io.json;

import com.addteq.service.excellentable.exc_io.spreadjs.CellStyle;
import com.google.gson.JsonObject;

public class CellStyleParser {
	
	public static CellStyle parse(JsonObject jsonStyle){
		
		if(jsonStyle == null){return null;} 
		
		CellStyle style = new CellStyle();
		
		if(jsonStyle.has("hAlign")){
			style.sethAlign(jsonStyle.get("hAlign").getAsInt());
		}
		if(jsonStyle.has("vAlign")){
			style.setvAlign(jsonStyle.get("vAlign").getAsInt());
		}
		if(jsonStyle.has("font")){
			style.setFont(FontParser.parse(jsonStyle.get("font").getAsString()));
		}
		if(jsonStyle.has("foreColor")){
			style.setFontColor(jsonStyle.get("foreColor").getAsString());
		}
		if(jsonStyle.has("backColor")){
			style.setBackColor(jsonStyle.get("backColor").getAsString());
		}
		if(jsonStyle.has("textDecoration")){
			style.setTextDecoration(jsonStyle.get("textDecoration").getAsInt());
		}
		if(jsonStyle.has("textIndent")){
			style.setTextIndent(jsonStyle.get("textIndent").getAsInt());
		}
		if(jsonStyle.has("imeMode")){
			//TODO
		}
		if(jsonStyle.has("wordWrap")){
			style.setWordWrap(jsonStyle.get("wordWrap").getAsBoolean());
		}
		if(jsonStyle.has("borderLeft")){
			style.setBorderLeft(BorderParser.parse(jsonStyle.getAsJsonObject("borderLeft")));
		}
		if(jsonStyle.has("borderRight")){
			style.setBorderRight(BorderParser.parse(jsonStyle.getAsJsonObject("borderRight")));
		}
		if(jsonStyle.has("borderTop")){
			style.setBorderTop(BorderParser.parse(jsonStyle.getAsJsonObject("borderTop")));
		}
		if(jsonStyle.has("borderBottom")){
			style.setBorderBottom(BorderParser.parse(jsonStyle.getAsJsonObject("borderBottom")));
		}
		
		if(jsonStyle.has("cellType")){
			//TODO celTypeParser 
			if(jsonStyle.getAsJsonObject("cellType").get("typeName").getAsInt() == 8){
				style.setCellType(LinkCellTypeParser.parse(jsonStyle.getAsJsonObject("cellType")));

			}
		}
		
		if(jsonStyle.has("formatter")){
                    /*  Ref: EXC-2745
                     *  When we import any sheet from Google spreadsheet, some of the formatters has double quotes in between.
                     *  e.g ("mmm" "d") & ("$"#,##0) this causes formatting issue while exporting to PDF.
                     *  hence we are removing all extra double quotes from the formatter with replaceAll method.
                    */
                    style.setFormatter(jsonStyle.get("formatter").getAsString().replaceAll("\"", ""));	
		}
		
		if(jsonStyle.has("autoFormatter")){	
			style.setAutoFormatter(CellFormatterParser.parse(jsonStyle.getAsJsonObject("autoFormatter")));
			
		}
		
		return style;
		
	}

}
