package com.addteq.service.excellentable.exc_io.spreadjs;

// Size holds column-width row-height value.  
public class ColRowInfo {
	
        private int colRowNo;
        private double colRowOffSet; //Used to hold offset value in case of image(i.e floating object) 
        private double size;
	private boolean resizable = true;

        public int getColRowNo() {
            return colRowNo;
        }

        public void setColRowNo(int colRowNo) {
            this.colRowNo = colRowNo;
        }

        public double getColRowOffSet() {
            return colRowOffSet;
        }

        public void setColRowOffSet(double colRowOffSet) {
            this.colRowOffSet = colRowOffSet;
        }
        
	public void setSize(double d){
		this.size = d;
	}
	
	public double getSize(){
		return this.size;
	}
	
	public void isResizable(boolean value){
		this.resizable = value;
	}
	
	public boolean isResizable(){
		return this.resizable;
	}
}