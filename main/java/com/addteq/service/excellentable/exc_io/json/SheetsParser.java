package com.addteq.service.excellentable.exc_io.json;

import com.addteq.service.excellentable.exc_io.spreadjs.*;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SheetsParser {

	public static Map<String, Sheet> parse(JsonObject jsonSheets){
		
		Map<String, Sheet> sheets = new HashMap<String, Sheet>();
		
		jsonSheets.entrySet().forEach( entry -> {
			Sheet sheet = new Sheet();
			
			JsonObject jsonSheet = entry.getValue().getAsJsonObject();
			
			sheet.setName(entry.getKey());
			if(jsonSheet.getAsJsonObject("selections").has("0")){
				sheet.setSelection(ParserUtils.getRangeParser().parse(jsonSheet.getAsJsonObject("selections").getAsJsonObject("0")));
			}
			sheet.setActiveCol(jsonSheet.get("activeCol").getAsInt());
			sheet.setActiveRow(jsonSheet.get("activeRow").getAsInt());
			
			JsonArrayParser<ColRowInfo> colsRowsParser = new JsonArrayParser<ColRowInfo>();
			ColRowInfoParser colRowParser = new ColRowInfoParser();
			
			SheetDefaults sheetDefaults = new SheetDefaults();
			
			colRowParser.setDefaultSize(sheetDefaults.getColWidth());
			
			if(jsonSheet.has("columns")){
				sheet.setColumns(colsRowsParser.parse(jsonSheet.getAsJsonArray("columns"), colRowParser));
			}
			
			colRowParser.setDefaultSize(sheetDefaults.getRowHeight());
			
			if(jsonSheet.has("rows")){
				sheet.setRows(colsRowsParser.parse(jsonSheet.getAsJsonArray("rows"), colRowParser));
			}
			
			if(jsonSheet.has("colHeaderRowInfos")){
				sheet.setColHeaderRowInfos(colsRowsParser.parse(jsonSheet.getAsJsonArray("colHeaderRowInfos"), colRowParser));
                sheet.setColHeaderHeight(jsonSheet.get("colHeaderRowInfos").getAsJsonArray().get(0).getAsJsonObject().get("size").getAsDouble());
			}else{
                sheet.setColHeaderHeight(jsonSheet.get("defaults").getAsJsonObject().get("colHeaderRowHeight").getAsDouble());
            }
                        
            if(jsonSheet.has("rowHeaderColInfos")){
				sheet.setRowHeaderWidth(jsonSheet.get("rowHeaderColInfos").getAsJsonArray().get(0).getAsJsonObject().get("size").getAsDouble());
			}else{
                sheet.setRowHeaderWidth(jsonSheet.get("defaults").getAsJsonObject().get("rowHeaderColWidth").getAsDouble());
            }
                        
			sheet.setRowCount(jsonSheet.get("rowCount").getAsInt());
			sheet.setColCount(jsonSheet.get("columnCount").getAsInt());

			if(jsonSheet.has("selectionBorderColor")){
				sheet.setSelectionBorderColor(jsonSheet.get("selectionBorderColor").getAsString());
			}
			
			if(jsonSheet.has("allowCellOverflow")) {
				sheet.setAllowCellOverFlow(jsonSheet.get("allowCellOverflow").getAsBoolean());
			}
			
			if(jsonSheet.has("theme")) {
				sheet.setTheme(jsonSheet.get("theme").getAsString());
			}
			
			if(jsonSheet.has("gridline")){
				sheet.setGridline(GridlineParser.parse(jsonSheet.getAsJsonObject("gridline")));	
			}

			if(jsonSheet.has("rowHeaderData")){

				sheet.setRowHeaderData((RowHeaderData) ColRowHeaderDataParser.parse(jsonSheet.getAsJsonObject("rowHeaderData"),"columnDataArray"));
			}
			
			if(jsonSheet.has("colHeaderData")){
				sheet.setColHeaderData((ColHeaderData) ColRowHeaderDataParser.parse(jsonSheet.getAsJsonObject("colHeaderData"),"rowDataArray"));

			}
			
			if(jsonSheet.has("tables")){
				
				JsonArrayParser<TableStyle> tableArrayParser = new JsonArrayParser<TableStyle>();
				TableStyleParser tableStyleParser = new TableStyleParser();
				
				sheet.setTables(tableArrayParser.parse(jsonSheet.getAsJsonArray("tables"), tableStyleParser));
				
			}
			
			if(jsonSheet.has("floatingObjects")){
				
				JsonArrayParser<Image> imgsArrayParser = new JsonArrayParser<Image>();
				ImageParser tableStyleParser = new ImageParser();
				
				sheet.setImages(imgsArrayParser.parse(jsonSheet.getAsJsonArray("floatingObjects"), tableStyleParser));
				
			}
			
			if(jsonSheet.has("spans")){
				JsonArrayParser<Range> spansArrayParser = new JsonArrayParser<Range>();
				sheet.setSpans(spansArrayParser.parse(jsonSheet.getAsJsonArray("spans"), ParserUtils.getRangeParser()));
				
			}
			
			if(jsonSheet.has("data")){
				sheet.setData(DataParser.parse(jsonSheet.getAsJsonObject("data")));
			}
			
			JsonArrayParser<GroupData> groupArrayParser = new JsonArrayParser<GroupData>();
			GroupDataParser groupParser = new GroupDataParser();
			
			if(jsonSheet.has("rowRangeGroup")){
				List<GroupData> rowRange = groupArrayParser.parse(jsonSheet.getAsJsonObject("rowRangeGroup").getAsJsonArray("itemsData"), groupParser);
				sheet.setColRangeGroup(new RangeGroup(rowRange));			}
			
			if(jsonSheet.has("colRangeGroup")){
				List<GroupData> colRange = groupArrayParser.parse(jsonSheet.getAsJsonObject("colRangeGroup").getAsJsonArray("itemsData"), groupParser);
				sheet.setColRangeGroup(new RangeGroup(colRange));			
			}
		
			sheet.setIndex(jsonSheet.get("index").getAsInt());

			sheets.put(String.valueOf(sheet.getIndex()), sheet);
		});
		return sheets;
	}
}