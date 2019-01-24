package com.addteq.service.excellentable.exc_io.spreadjs;

public class Gridline {
	
	private String color = "rgb(212, 212, 212)";
	private boolean showVerticalGridline = true;
	private boolean showHorizontalGridline = true;
	
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public boolean isShowVerticalGridline() {
		return showVerticalGridline;
	}
	public void setShowVerticalGridline(boolean showVerticalGridline) {
		this.showVerticalGridline = showVerticalGridline;
	}
	public boolean isShowHorizontalGridline() {
		return showHorizontalGridline;
	}
	public void setShowHorizontalGridline(boolean showHorizontalGridline) {
		this.showHorizontalGridline = showHorizontalGridline;
	}

}
