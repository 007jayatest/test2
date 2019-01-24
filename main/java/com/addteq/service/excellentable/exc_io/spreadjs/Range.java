package com.addteq.service.excellentable.exc_io.spreadjs;

// Represent an area, such span, tablestyle
public class Range {
	
	private int row;
	private int col;
	private int rowCount;
	private int colCount;
	
	public Range(int row, int col, int rowCount, int colCount ){
		
		this.row = row;
		this.col = col;
		this.rowCount = rowCount;
		this.colCount = colCount;
		
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}

	public int getRowCount() {
		return rowCount;
	}

	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}

	public int getColCount() {
		return colCount;
	}

	public void setColCount(int colCount) {
		this.colCount = colCount;
	}
	
	

}
