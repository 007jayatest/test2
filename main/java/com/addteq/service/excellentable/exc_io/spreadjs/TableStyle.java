package com.addteq.service.excellentable.exc_io.spreadjs;

import com.google.gson.JsonObject;

import java.util.List;

// Represents a predefined formatted table
public class TableStyle extends Range {

	private String name;
	private JsonObject style = new JsonObject();
	private boolean bandColumns = false;
	private boolean bandRows;
	private boolean showFooter;
	private boolean showHeader = true;
	private boolean highLightFirst;
	private boolean highLightLast;
	private List<TableStyleHeader> headers;
	private RowFilter rowFilter;
	 
	public TableStyle(int row, int col, int rowCount, int colCount) {
		super(row, col, rowCount, colCount);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStyle() {
		
		if(!style.has("buildInName"))
			return "";
		
		return style.get("buildInName").getAsString();
	}

	public void setStyle(String style) {
		this.style.addProperty("buildInName", style);
	}
	
	public void setStyle(JsonObject style) {
		this.style = style;
	}

	public boolean isBandColumns() {
		return bandColumns;
	}

	public void setBandColumns(boolean bandColumns) {
		this.bandColumns = bandColumns;
	}
	
	public boolean isBandRows() {
		return bandRows;
	}

	public void setBandRows(boolean bandRows) {
		this.bandRows = bandRows;
	}

	public boolean isShowFooter() {
		return showFooter;
	}

	public void setShowFooter(boolean showFooter) {
		this.showFooter = showFooter;
	}

	public boolean isShowHeader() {
		return showHeader;
	}

	public void setShowHeader(boolean showHeader) {
		this.showHeader = showHeader;
	}

	public boolean isHighLightFirst() {
		return highLightFirst;
	}

	public void setHighLightFirst(boolean highLightFirst) {
		this.highLightFirst = highLightFirst;
	}

	public boolean isHighLightLast() {
		return highLightLast;
	}

	public void setHighLightLast(boolean highLightLast) {
		this.highLightLast = highLightLast;
	}

	public List<TableStyleHeader> getHeaders() {
		return headers;
	}

	public void setHeaders(List<TableStyleHeader> headers) {
		this.headers = headers;
	}

	public RowFilter getRowFilter() {
		return rowFilter;
	}

	public void setRowFilter(RowFilter rowFilter) {
		this.rowFilter = rowFilter;
	}

}