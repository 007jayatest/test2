package com.addteq.service.excellentable.exc_io.spreadjs;

public class Border {

	private String color;
	private int style;
	
	public Border(int style, String color){
		this.style = style;
		this.color = color;
	}

	public String getColor() {
		return color;
	}
	public int getStyle() {
		return style;
	}
	
}
