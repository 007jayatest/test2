package com.addteq.service.excellentable.exc_io.spreadjs;

import com.addteq.service.excellentable.exc_io.json.CellParser;
import com.google.gson.JsonObject;

import java.util.List;

public class Sheet {
	
	private String name;
	private Range selection;
	private SheetDefaults defaults = new SheetDefaults();
	private List<ColRowInfo> columns;
	private List<ColRowInfo> rows;
	private int rowCount;
	private int columnCount;
	private List<Range> spans;
	private String selectionBorderColor = "#47b54d";
	private int activeRow;
	private int activeCol;
	private boolean allowCellOverFlow;
	private String theme;
        private double rowHeaderWidth;
        private double colHeaderHeight;
	private List<ColRowInfo> colHeaderRowInfos;
	private List<TableStyle> tables;
	private List<Image> images;
	private List<Note> comments;
	private int frozenRowCount;
	private int frozenColCount;
	private int index;
	private Data data;
	private RangeGroup rowRangeGroup;
	private RangeGroup colRangeGroup;
	private RowHeaderData rowHeaderData;
	private ColHeaderData colHeaderData;
	private Gridline gridline;
	
	
	public Sheet(){
		this.data = new Data();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Range getSelection() {
		return selection;
	}

	public Gridline getGridline() {
		return gridline;
	}

	public void setGridline(Gridline gridline) {
		this.gridline = gridline;
	}

	public void setSelection(Range selection) {
		this.selection = selection;
	}

	public List<ColRowInfo> getColumns() {
		return columns;
	}
	
	public void setColumns(List<ColRowInfo> columns) {
		this.columns = columns;
	}

	public void addColumnInfo(ColRowInfo column) {
		this.columns.add(column);
	}

	public List<ColRowInfo> getRows() {
		return rows;
	}

	public void setRows(List<ColRowInfo> rows) {
		this.rows = rows;
	}
	
	public void addRowInfo(ColRowInfo row) {
		this.rows.add(row);
	}

	public int getRowCount() {
		return rowCount;
	}

	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}

	public int getColCount() {
		return columnCount;
	}

	public void setColCount(int colCount) {
		this.columnCount = colCount;
	}

	public List<Range> getSpans() {
		return spans;
	}

	public void setSpans(List<Range> spans) {
		this.spans = spans;
	}
	
	public void addSpan(Range span) {
		this.spans.add(span);
	}

	public String getSelectionBorderColor() {
		return selectionBorderColor;
	}

	public void setSelectionBorderColor(String selectionBorderColor) {
		this.selectionBorderColor = selectionBorderColor;
	}

	public int getActiveRow() {
		return activeRow;
	}

	public void setActiveRow(int activeRow) {
		this.activeRow = activeRow;
	}

	public int getActiveCol() {
		return activeCol;
	}

	public void setActiveCol(int activeCol) {
		this.activeCol = activeCol;
	}

	public boolean isAllowCellOverFlow() {
		return allowCellOverFlow;
	}

	public void setAllowCellOverFlow(boolean allowCellOverFlow) {
		this.allowCellOverFlow = allowCellOverFlow;
	}

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}
        
        public double getRowHeaderWidth() {
		return rowHeaderWidth;
	}

	public void setRowHeaderWidth(double rowHeaderWidth) {
		this.rowHeaderWidth = rowHeaderWidth;
	}
        public double getColHeaderHeight() {
		return colHeaderHeight;
	}

	public void setColHeaderHeight(double colHeaderHeight) {
		this.colHeaderHeight = colHeaderHeight;
	}
        
	public List<ColRowInfo> getColHeaderRowInfos() {
		return colHeaderRowInfos;
	}

	public void setColHeaderRowInfos(List<ColRowInfo> colHeaderRowInfos) {
		this.colHeaderRowInfos = colHeaderRowInfos;
	}

	public List<TableStyle> getTables() {
		return tables;
	}
	
	public void setTables(List<TableStyle> tables) {
		this.tables = tables;
	}

	public void addTableStyle(TableStyle table) {
		this.tables.add(table);
	}

	public List<Image> getImages() {
		return images;
	}

	public void setImages(List<Image> images) {
		this.images = images;
	}

	public List<Note> getComments() {
		return comments;
	}
	
	public void setComments(List<Note> comments) {
		this.comments = comments;
	}

	public int getFrozenRowCount() {
		return frozenRowCount;
	}

	public void setFrozenRowCount(int frozenRowCount) {
		this.frozenRowCount = frozenRowCount;
	}

	public int getFrozenColCount() {
		return frozenColCount;
	}

	public void setFrozenColCount(int frozenColCount) {
		this.frozenColCount = frozenColCount;
	}
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
	public void setData(Data data){
		this.data = data;
	}
	public Data getData(){
		return this.data;
	}
	public SheetDefaults getDefaults() {
		return defaults;
	}

	public void setDefaults(SheetDefaults defaults) {
		this.defaults = defaults;
	}

	public RangeGroup getRowRangeGroup() {
		return rowRangeGroup;
	}

	public void setRowRangeGroup(RangeGroup rowRangeGroup) {
		this.rowRangeGroup = rowRangeGroup;
	}

	public RangeGroup getColRangeGroup() {
		return colRangeGroup;
	}

	public void setColRangeGroup(RangeGroup colRangeGroup) {
		this.colRangeGroup = colRangeGroup;
	}

	public RowHeaderData getRowHeaderData() {
		return rowHeaderData;
	}

	public void setRowHeaderData(RowHeaderData rowHeaderData) {
		this.rowHeaderData = rowHeaderData;
	}

	public ColHeaderData getColHeaderData() {
		return colHeaderData;
	}

	public void setColHeaderData(ColHeaderData colHeaderData) {
		this.colHeaderData = colHeaderData;
	}

	public Cell getCell(int row, int col){
		
		String rowS = String.valueOf(row);
		JsonObject dataTable = this.data.getDataTable();
		
		if(dataTable.has(rowS)){
			String colS = String.valueOf(col);
			JsonObject rowData = dataTable.getAsJsonObject(rowS);
			if(rowData.has(colS)){
				
				return CellParser.parse(rowData.getAsJsonObject(colS), row, col);
			}
		}
		return null;
	}

}
