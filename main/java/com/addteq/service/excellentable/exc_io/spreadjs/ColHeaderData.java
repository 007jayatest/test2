package com.addteq.service.excellentable.exc_io.spreadjs;

import java.util.ArrayList;
import java.util.List;

public class ColHeaderData extends ColRowHeaderData{

	private List<Cell> rowDataArray;
	
	public ColHeaderData(){
		rowDataArray = new ArrayList<Cell>();
	}

	@Override
	List<Cell> getHeaderList() {
		return rowDataArray;
	}

	@Override
	void addHeader(Cell cell) {
		rowDataArray.add(cell);
		
	}
	
}