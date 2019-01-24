package com.addteq.service.excellentable.exc_io.json;

import com.addteq.service.excellentable.exc_io.spreadjs.Font;
import com.addteq.service.excellentable.exc_io.spreadjs.Fonts;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FontParser {

	public static Font parse(String font){
		
		Font excFont = new Font();
		
		excFont.setFamily( getFontFamily(font));
		excFont.setBold(isBold(font));
		excFont.setItalic(isItalic(font));
		excFont.setSize(getFontSizeString(font));
		
		return excFont;
	}
	
    public static String getFontFamily(String font) {

    	return Fonts.getAvailableFamily(font);
    }
	
	public static boolean isBold(String font) {
		
		return (font.contains("bold") || font.contains("700"));
	}
	public static boolean isItalic(String font) {
		
		return font.contains("italic");
	}
	public static String getFontSizeString(String font) {
		
		Pattern pattern = Pattern.compile("\\d+\\.\\d+px|\\d+px");
		Matcher matcher = pattern.matcher(font);
		
		if(matcher.find()){
			return matcher.group(0);
		}
		
		return "13px";
		
	}

}
