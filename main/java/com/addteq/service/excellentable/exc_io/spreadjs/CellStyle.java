package com.addteq.service.excellentable.exc_io.spreadjs;

public class CellStyle {

	private String backgroundImage;
	private String backgroundImageLayout;
	private String backColor;
	private String foreColor;
	private int hAlign = -1;
	private int vAlign = 1;
	private String font;
	private Font fontObj;
	private String themeFont;
	private String formatter;
	private CellFormatter autoFormatter;
	private Border borderLeft;
	private Border borderTop;
	private Border borderRight;
	private Border borderBottom;
	private boolean locked; 
	private int textIndent = 1;
	private boolean wordWrap;
	private boolean shrinkToFit;
	private Padding padding;
	private CellType cellType;
	private int textDecoration;
	
	public String getBackgroundImage() {
		return backgroundImage;
	}
	public void setBackgroundImage(String backgroundImage) {
		this.backgroundImage = backgroundImage;
	}
	public String getBackgroundImageLayout() {
		return backgroundImageLayout;
	}
	public void setBackgroundImageLayout(String backgroundImageLayout) {
		this.backgroundImageLayout = backgroundImageLayout;
	}
	public String getBackColor() {
		return backColor;
	}
	public void setBackColor(String backColor) {
		this.backColor = backColor;
	}
	public String getFontColor() {
		return foreColor;
	}
	public void setFontColor(String foreColor) {
		this.foreColor = foreColor;
	}
	public int gethAlign() {
		return hAlign;
	}
	public void sethAlign(int hAlign) {
		this.hAlign = hAlign;
	}
	public int getvAlign() {
		return vAlign;
	}
	public void setvAlign(int vAlign) {
		this.vAlign = vAlign;
	}
	
	public void setTextDecoration(int textDecoration){
		this.textDecoration = textDecoration;
	}

	public boolean isUnderLine(){

		return (textDecoration == 1 || textDecoration == 3 );
	}
	
	public boolean isStrike(){
		
		return (textDecoration == 2 || textDecoration == 3 );
	}
	
	public void setFont(Font font) {
		this.fontObj = font;
		this.font = font.getValue();
	}
	public Font getFont() {
		return fontObj;
	}
	public String getThemeFont() {
		return themeFont;
	}
	public void setThemeFont(String themeFont) {
		this.themeFont = themeFont;
	}
	public String getFormatter() {
		return formatter;
	}
	public void setFormatter(String formatter) {
		this.formatter = formatter;
	}
	public Border getBorderLeft() {
		return borderLeft;
	}
	public void setBorderLeft(Border borderLeft) {
		this.borderLeft = borderLeft;
	}
	public Border getBorderTop() {
		return borderTop;
	}
	public void setBorderTop(Border borderTop) {
		this.borderTop = borderTop;
	}
	public Border getBorderRight() {
		return borderRight;
	}
	public void setBorderRight(Border borderRight) {
		this.borderRight = borderRight;
	}
	public Border getBorderBottom() {
		return borderBottom;
	}
	public void setBorderBottom(Border borderBottom) {
		this.borderBottom = borderBottom;
	}
	public boolean isLocked() {
		return locked;
	}
	public void setLocked(boolean locked) {
		this.locked = locked;
	}
	public int getTextIndent() {
		return textIndent;
	}
	public int getTextIndentInPixels() {
		return textIndent * 8;
	}
	public void setTextIndent(int textIndent) {
		this.textIndent = textIndent;
	}
	public boolean isWordWrap() {
		return wordWrap;
	}
	public void setWordWrap(boolean wordWrap) {
		this.wordWrap = wordWrap;
	}
	public boolean isShrinkToFit() {
		return shrinkToFit;
	}
	public void setShrinkToFit(boolean shrinkToFit) {
		this.shrinkToFit = shrinkToFit;
	}
	public Padding getPadding() {
		return padding;
	}
	public void setPadding(Padding padding) {
		this.padding = padding;
	}
	public CellType getCellType() {
		return cellType;
	}
	public void setCellType(CellType cellType) {
		this.cellType = cellType;
	}
	public CellFormatter getAutoFormatter() {
		return autoFormatter;
	}
	public void setAutoFormatter(CellFormatter autoFormatter) {
		this.autoFormatter = autoFormatter;
	}
	
	 
}