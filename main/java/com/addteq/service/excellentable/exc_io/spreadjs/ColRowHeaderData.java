package com.addteq.service.excellentable.exc_io.spreadjs;

import java.util.List;

public abstract class ColRowHeaderData {


	private Cell defaultDataNode;
	
	public ColRowHeaderData(){
	}

	abstract List<Cell> getHeaderList();
	
	abstract void addHeader(Cell cell);
	
	public List<Cell> getHeaderDataArray() {
		return getHeaderList();
	}
	
	public void addData(CellStyle cellStyle) {
		Cell cell = new Cell();
		cell.setStyle(cellStyle);
		addHeader(cell);
	}

	public Cell getDefaultDataNode() {
		return defaultDataNode;
	}

	public void setDefaultDataNode(CellStyle cellStyle) {
		Cell cell = new Cell();
		cell.setStyle(cellStyle);
		
		this.defaultDataNode = cell;
	}
	

}
