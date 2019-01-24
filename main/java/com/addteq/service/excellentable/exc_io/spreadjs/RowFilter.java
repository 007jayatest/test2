package com.addteq.service.excellentable.exc_io.spreadjs;

import java.util.HashMap;
import java.util.Map;

public class RowFilter {
	
	private boolean showFilterButton;
	private Map<Integer,Boolean> filterButtonVisible = new HashMap<Integer, Boolean>();
	private Range range;
	
	public boolean isShowFilterButton() {
		return showFilterButton;
	}
	public void setShowFilterButton(boolean showFilterButton) {
		this.showFilterButton = showFilterButton;
	}
	public Map<Integer, Boolean> getFilterButtonVisible() {
		return filterButtonVisible;
	}
	public void addFilterButton(int index, boolean isVisible) {
		this.filterButtonVisible.put(index, isVisible);
	}
	public Range getRange() {
		return range;
	}
	public void setRange(Range range) {
		this.range = range;
	}

}
