package com.addteq.service.excellentable.exc_io.spreadjs;

import com.google.gson.JsonObject;

import java.util.List;

public class Data {

	JsonObject dataTable;

	List<StyledItem> columnDataArray;
	List<StyledItem> rowDataArray;

	JsonObject defaultDataNode;
	
	public Data(){
		this.dataTable = new JsonObject();
	}
	
	public JsonObject getDataTable() {
		return dataTable;
	}
	public void setDataTable(JsonObject dataTable) {
		this.dataTable = dataTable;
	}
	public List<StyledItem> getColumnDataArray() {
		return columnDataArray;
	}
	public void setColumnDataArray(List<StyledItem> columnDataArray) {
		this.columnDataArray = columnDataArray;
	}
	public List<StyledItem> getRowDataArray() {
		return rowDataArray;
	}
	public void setRowDataArray(List<StyledItem> rowDataArray) {
		this.rowDataArray = rowDataArray;
	}
	public JsonObject getDefaultDataNode() {
		return defaultDataNode;
	}
	public void setDefaultDataNode(JsonObject defaultDataNode) {
		this.defaultDataNode = defaultDataNode;
	}
		
}