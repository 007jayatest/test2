package com.addteq.service.excellentable.exc_io.spreadjs;

public class StyledItem {
	
	private CellStyle style;
	
	public StyledItem(CellStyle style){
		
		this.style = style;
	}
	
	public CellStyle getStyle(){
		return this.style;
	}

}