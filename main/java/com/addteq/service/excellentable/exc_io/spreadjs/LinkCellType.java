package com.addteq.service.excellentable.exc_io.spreadjs;

public class LinkCellType extends CellType{
	
	private static final String DEFAULT_COLOR = "#0000FF";
	private String linkColor = DEFAULT_COLOR;
	private String visitedLinkColor = DEFAULT_COLOR;
	private String text;
	private String linkToolTip;
	
	public LinkCellType(){
		super(8);
	}

	public String getLinkColor() {
		return linkColor;
	}

	public void setLinkColor(String linkColor) {
		this.linkColor = linkColor;
	}

	public String getVisitedLinkColor() {
		return visitedLinkColor;
	}

	public void setVisitedLinkColor(String visitedLinkColor) {
		this.visitedLinkColor = visitedLinkColor;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getLinkToolTip() {
		return linkToolTip;
	}

	public void setLinkToolTip(String linkToolTip) {
		this.linkToolTip = linkToolTip;
	}
	 
}