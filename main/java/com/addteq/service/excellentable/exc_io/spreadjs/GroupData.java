package com.addteq.service.excellentable.exc_io.spreadjs;

public class GroupData {

	private int index;
	private int count;
	private GroupDataInfo info;
	
	public GroupData(int index, int count, GroupDataInfo info){
		this.index = index;
		this.count = count;
		this.info = info;
	}
	
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public GroupDataInfo getInfo() {
		return info;
	}
	public void setInfo(GroupDataInfo info) {
		this.info = info;
	}
	
	
}
