package com.addteq.service.excellentable.exc_io.spreadjs;

public class GroupDataInfo {

	private int level;
	private boolean collapsed;
	
	public GroupDataInfo(int level, boolean collapsed){
		this.level = level;
		this.collapsed = collapsed;		
	}
	
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public boolean isCollapsed() {
		return collapsed;
	}
	public void setCollapsed(boolean collapsed) {
		this.collapsed = collapsed;
	}
	
}