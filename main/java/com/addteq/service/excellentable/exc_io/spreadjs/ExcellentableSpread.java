package com.addteq.service.excellentable.exc_io.spreadjs;

import java.util.HashMap;
import java.util.Map;

public class ExcellentableSpread {

	private String version;
	private boolean tabStripVisible = false;
	private boolean newTabVisible = false;
	private int showScrollTip = 3;
	private boolean showVerticalScrollBar = false;
	private boolean scrollbarMaxAlign = true;
	private String grayAreaBackColor = "Transparent";
	private Sheet sheet;
	
	private Map<String, Sheet> sheets= new HashMap<String, Sheet>();
	
	public ExcellentableSpread(){
		this.version = "";
		this.tabStripVisible = false;
		this.newTabVisible = false;
		this.showScrollTip = 3;
		this.showVerticalScrollBar = false;
		this.scrollbarMaxAlign = true;
		this.grayAreaBackColor = "Transparent";
		this.sheet = new Sheet();
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public boolean isTabStripVisible() {
		return tabStripVisible;
	}

	public void setTabStripVisible(boolean tabStripVisible) {
		this.tabStripVisible = tabStripVisible;
	}

	public boolean isNewTabVisible() {
		return newTabVisible;
	}

	public void setNewTabVisible(boolean newTabVisible) {
		this.newTabVisible = newTabVisible;
	}

	public int getShowScrollTip() {
		return showScrollTip;
	}

	public void setShowScrollTip(int showScrollTip) {
		this.showScrollTip = showScrollTip;
	}

	public boolean isShowVerticalScrollBar() {
		return showVerticalScrollBar;
	}

	public void setShowVerticalScrollBar(boolean showVerticalScrollBar) {
		this.showVerticalScrollBar = showVerticalScrollBar;
	}

	public boolean isScrollbarMaxAlign() {
		return scrollbarMaxAlign;
	}

	public void setScrollbarMaxAlign(boolean scrollbarMaxAlign) {
		this.scrollbarMaxAlign = scrollbarMaxAlign;
	}

	public String getGrayAreaBackColor() {
		return grayAreaBackColor;
	}

	public void setGrayAreaBackColor(String grayAreaBackColor) {
		this.grayAreaBackColor = grayAreaBackColor;
	}

	public Sheet getSheet(String sheetName) {
		return this.sheets.get(sheetName);
	}

	public void setSheets(Map<String, Sheet> sheets2) {
		this.sheets = sheets2;
	}
	
	public Map<String, Sheet> getSheets() {
		return this.sheets;
	}
	
	
}
