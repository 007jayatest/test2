package com.addteq.service.excellentable.exc_io.spreadjs;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class Font {
	
	private boolean isBold;
	private boolean isItalic;
	private String family;
	private String size = "13px";
	
	public double getFontSizeNum(String fontInPx){
		return Double.valueOf(fontInPx.replace("px", ""));
	}

	public boolean isBold() {
		return isBold;
	}

	public void setBold(boolean isBold) {
		this.isBold = isBold;
	}

	public boolean isItalic() {
		return isItalic;
	}

	public void setItalic(boolean isItalic) {
		this.isItalic = isItalic;
	}

	public String getFamily() {
		return family;
	}

	public void setFamily(String family) {
		this.family = family;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}
	
	public String getValue(){
		
		List<String> properties = new ArrayList<String>();
		
		if(isItalic()){
			properties.add("italic");
		}
		
		if(isBold()){
			properties.add("bold");
		}
		
		properties.add(size);
		properties.add("\"" + family + "\"" ) ;
		
		return StringUtils.join(properties, " ");
	}	

}
