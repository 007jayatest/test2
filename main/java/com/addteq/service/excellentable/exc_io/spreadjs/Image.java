package com.addteq.service.excellentable.exc_io.spreadjs;

public class Image {
	
	private String name;
	private double x;
	private double y;
	private double width;
	private double height;
	private boolean isSelected = false;
	private String src;
	private int floatingObjectType;
	private String backColor = "#FFFFFF"; 
	private int borderRadius = 3;
	private String borderStyle = "solid";
	private String borderColor = "#000000";
	private int startColumn = -1;
	private int startColumnOffset = 5;
	private int startRow = -1;
	private int startRowOffset = 5;

	public Image(String name, String src){
		this.name = name;
		this.src = src;
	}
	
	public String getType(){
		String typeRaw = src.split(",") [0];
		
		if(typeRaw.contains("png")){
			return "png";
		}
		else if(typeRaw.contains("jpeg")){
			return "jpeg";
		}
		return typeRaw;
		
	}
	
	public String getBase64(){
		return src.split(",") [1];
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	public int getFloatingObjectType() {
		return floatingObjectType;
	}

	public void setFloatingObjectType(int floatingObjectType) {
		this.floatingObjectType = floatingObjectType;
	}

	public String getBackColor() {
		return backColor;
	}

	public void setBackColor(String backColor) {
		this.backColor = backColor;
	}

	public int getBorderRadius() {
		return borderRadius;
	}

	public void setBorderRadius(int borderRadius) {
		this.borderRadius = borderRadius;
	}

	public String getBorderStyle() {
		return borderStyle;
	}

	public void setBorderStyle(String borderStyle) {
		this.borderStyle = borderStyle;
	}

	public String getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(String borderColor) {
		this.borderColor = borderColor;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	
	public int getStartColumn() {
		return startColumn;
	}

	public void setStartColumn(int startColumn) {
		this.startColumn = startColumn;
	}

	public int getStartColumnOffset() {
		return startColumnOffset;
	}

	public void setStartColumnOffset(int startColumnOffset) {
		this.startColumnOffset = startColumnOffset;
	}

	public int getStartRow() {
		return startRow;
	}

	public void setStartRow(int startRow) {
		this.startRow = startRow;
	}

	public int getStartRowOffset() {
		return startRowOffset;
	}

	public void setStartRowOffset(int startRowOffset) {
		this.startRowOffset = startRowOffset;
	}

}