package com.addteq.service.excellentable.exc_io.spreadjs;

import java.util.ArrayList;
import java.util.List;

public class RowHeaderData extends ColRowHeaderData{
	
	private List<Cell> columnDataArray;

	public RowHeaderData(){
		columnDataArray = new ArrayList<Cell>();
	}
	
	@Override
	List<Cell> getHeaderList() {

		return columnDataArray;
	}

	@Override
	void addHeader(Cell cell) {

		columnDataArray.add(cell);
		
	}

}