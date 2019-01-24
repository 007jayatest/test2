package com.addteq.service.excellentable.exc_io.spreadjs;

// Base class for cell type such as LinkType
public class CellType {
	
	private int typeName;
	
	public CellType(int type){
		this.typeName = type;
	}
	
	public int getTypeNumber(){
		return this.typeName;
	}

}