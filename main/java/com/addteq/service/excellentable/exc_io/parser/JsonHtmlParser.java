package com.addteq.service.excellentable.exc_io.parser;

import com.addteq.service.excellentable.exc_io.json.ExcellentableSpreadParser;
import com.addteq.service.excellentable.exc_io.spreadjs.*;
import com.addteq.service.excellentable.exc_io.utils.SheetCleanup;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.codec.binary.Base64;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;

public class JsonHtmlParser {

	private static final String DIV = "div";
	private static final String TABLE = "table";
	private static final String STYLE = "style";
	private static final String CLASS = "class";
	private static final String TR = "tr";
	private static final String TD = "td";
	private static final String TH = "th";
    private static final String CELL_WIDTH = "exc-width";
    private static final String CELL_HEIGHT = "exc-height";
	private static final String TBODY = "tbody";
	private static final String THEAD = "thead";
	private static final String HIDDEN = "eui-hidden";
	private static final String H3 = "h3";
	private static final String P = "p";
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(JsonHtmlParser.class);

	public static final int ROW_COUNT_TO_CHECK = 100;
	public static final int COL_COUNT_TO_CHECK = 26;

	public static String getHTML(String metaData, boolean isEvaluation, String evalMessage, boolean includeImages) {

		ExcellentableSpread spread = ExcellentableSpreadParser.parse(metaData);

		if (spread == null) {
			return "";
		}

		Map<String, Sheet> sheets = spread.getSheets();
		int sheetsCt = sheets.size();
		Element div = new Document("").createElement(DIV);

		while (sheetsCt > 0) {
			sheetsCt--;
			Sheet sheet = sheets.get(String.valueOf(sheetsCt));

			JsonObject dataTable = sheet.getData().getDataTable();

			int dataTableRowCount = findMaxRowIndex(dataTable);
			int dataTableMaxColCount = findMaxColIndex(dataTable);

			//Additional check on the sheet objects to avoid redundant rows and columns getting printed on pdf export.
			if (dataTableRowCount <= ROW_COUNT_TO_CHECK || dataTableMaxColCount <= COL_COUNT_TO_CHECK) {
				sheet = SheetCleanup.clean(sheet, dataTableRowCount, dataTableMaxColCount);
			}

			Element tableDiv = getSingleTable(sheet, includeImages);
			div.prependChild(tableDiv);
		}

		applyEvaluationMessage(div, isEvaluation, evalMessage);

		return div.outerHtml();
	}

	public static int findMaxRowIndex(JsonObject dataTable) {
		Set<String> dataTableKeys = dataTable.keySet();
		List<Integer> rowSizes = new ArrayList<>();
		dataTableKeys.stream()
				.forEach(i -> rowSizes.add(Integer.parseInt(i)));

		int maxRowCount = rowSizes.stream().reduce(Integer.MIN_VALUE, (a, b) -> Integer.max(a, b));

		return maxRowCount + 1;
	}

	public static int findMaxColIndex(JsonObject dataTable) {
		Set<String> dataTableKeys = dataTable.keySet();
		List<Integer> colSizesPerRow = new ArrayList<>();

		dataTableKeys.stream()
				.forEach(i -> {
					JsonElement dataTableRow = dataTable.get(i);
					Set<String> keySetOfColIndicesPerRow = dataTableRow.getAsJsonObject().keySet();

					int highestIndex = findHighestIndex(keySetOfColIndicesPerRow);
					colSizesPerRow.add(highestIndex + 1);
				});

		int maxColCount = colSizesPerRow.stream().reduce(Integer.MIN_VALUE, (a, b) -> Integer.max(a, b));

		return maxColCount;
	}

	private static int findHighestIndex(Set<String> keySetOfColIndicesPerRow) {
		List<Integer> colIndices = new ArrayList<>();
		keySetOfColIndicesPerRow.forEach( i -> colIndices.add(Integer.parseInt(i)));

		return colIndices.stream().reduce(Integer.MIN_VALUE, (a, b) -> Integer.max(a, b));
	}

	private static Element getSingleTable(Sheet sheet, boolean includeImages) {

		Element div = new Document("").createElement(DIV);

		if (tableIsEmpty(sheet)) {
			return div;
		}
		div.addClass("eui-table-html");

		Element title = new Document("").createElement(H3);
		title.text(sheet.getName());

		div.appendChild(title);

		List<ColRowInfo> colsInfo = sheet.getColumns();
		List<ColRowInfo> rowsInfo = sheet.getRows();

		int columnCt = sheet.getColCount();
		int rowCt = sheet.getRowCount();

		Element table = new Document("").createElement(TABLE);
		table.addClass("confluenceTable");

		table.appendElement(THEAD);

		Data data = sheet.getData();
		List<StyledItem> rowStyles = data.getRowDataArray();
		List<StyledItem> colStyles = data.getColumnDataArray();

		Element tBody = table.appendElement(TBODY);
		for (int rowIndex = 0; rowIndex < rowCt; rowIndex++) {

			Element row = getTR(tBody, rowIndex, rowsInfo, sheet.getDefaults().getRowHeight());
			StyledItem rowStyle = getColRowStyle(rowStyles, rowIndex);

			for (int colIndex = 0; colIndex < columnCt; colIndex++) {
                                Element td = getTD(row, colIndex, colsInfo, sheet.getDefaults().getColWidth());
				StyledItem colStyle = getColRowStyle(colStyles, colIndex);

				applyCellData(sheet.getCell(rowIndex, colIndex), td, rowStyle, colStyle);
			}
		}

		applyTableStyles(tBody, sheet.getTables());
		applySpans(tBody, sheet.getSpans());
		applyGridlines(table, sheet.getGridline());

		div.appendChild(table);

		if (includeImages) {
			applyImages(table, sheet.getImages());
		}

		applyColRowHeaderData(table, sheet,columnCt);

		return div;
	}

	private static boolean tableIsEmpty(Sheet sheet) {
		boolean noData = sheet.getData().getDataTable().entrySet().isEmpty();
		boolean noImages = (sheet.getImages() == null) || sheet.getImages().isEmpty();
		return noData && noImages;

	}

	private static void applyGridlines(Element table, Gridline grid) {
		if (grid != null && !grid.isShowHorizontalGridline()) {
			table.addClass("no-grid");
		}
	}

	private static StyledItem getColRowStyle(List<StyledItem> styles, int index) {
		if (styles == null || styles.isEmpty() || styles.size() <= index) {
			return null;
		}

		return styles.get(index);
	}

	private static Element getTR(Element table, int rowIndex, List<ColRowInfo> colsRowInfo, int defaultSize) {

		return applyCellHeightWidth(table, TR, rowIndex, "height:%spx; min-height:%spx; max-height:%spx", colsRowInfo,
				defaultSize, CELL_HEIGHT);
	}

	private static Element getTD(Element row, int colIndex, List<ColRowInfo> colsRowInfo, int defaultSize) {

		return applyCellHeightWidth(row, TD, colIndex, "width:%spx; min-width:%spx; max-width:%spx", colsRowInfo,
				defaultSize, CELL_WIDTH);
	}

	private static void applyEvaluationMessage(Element div, boolean isEvaluation, String evalMessage) {

		if (isEvaluation) {
			Element divMessage = div.appendElement(DIV);
			divMessage.addClass("eui-license-eval");
			divMessage.html(evalMessage);
		}
	}

	private static Element applyCellHeightWidth(Element parent, String tag, int index, String format,
			List<ColRowInfo> colsRowInfo, int defaultSize, String customAttr) {
		Element el = parent.appendElement(tag);

		try {
			ColRowInfo rowInfo = colsRowInfo.get(index);
			double size = rowInfo.getSize();

			if (size > 0) {
                            el.attr(STYLE, String.format(format, size, size, size)).attr(customAttr,Double.toString(size));
			}

		} catch (Exception e) {
			el.attr(STYLE, String.format(format, defaultSize, defaultSize, defaultSize)).attr(customAttr,Double.toString(defaultSize));
		}
		return el;
	}

	private static void applySpans(Element table, List<Range> spans) {

		if (spans == null) {
			return;
		}

		for (Range span : spans) {

			int col = span.getCol();
			int row = span.getRow();
			String colCount = Integer.toString(span.getColCount());
			String rowCount = Integer.toString(span.getRowCount());

			String tdSelector = String.format("tr:eq(%s) td:eq(%s)", row, col);
			table.select(tdSelector).attr("rowspan", rowCount);
			table.select(tdSelector).attr("colspan", colCount);

			int rowTo = row + span.getRowCount();
			int colTo = col + span.getColCount();

			String selector = "%s:eq(%s), %s:gt(%s):lt(%s)";
			String rowSelector = String.format(selector, "tr", row, "tr", row, rowTo);
			String tdsSelector = String.format(selector, "td", col, "td", col, colTo);

			Elements trs = table.select(rowSelector);

			if (trs.isEmpty()) {
				return;
			}

			Elements tds = trs.select(tdsSelector);

			tds.addClass(HIDDEN);
			table.select(tdSelector).removeClass(HIDDEN);
		}
	}

	private static void applyCellData(Cell excCell, Element td, StyledItem rowStyle, StyledItem colStyle) {

		if (excCell == null) {
			return;
		}
		excCell = applyRowAndColFormatting(excCell, rowStyle, colStyle);

		CellHTML cell = new CellHTML(excCell);



		td.classNames(cell.getClassest());
		cell.addStyle(td.attr(STYLE));
		td.attr(STYLE, cell.getStyle());
		//New lines in cell data should be replaced with <br>,
		// no need to put closing tags as Jsoup will store it as <br> only.
		//Also to fix the alignment issue, per the new design, we are
		//wrapping up the cell data in <p> tags and inherit the style
		//from the enclosing td
		StringBuilder cellData = new StringBuilder();
		cellData.append("<"+P+" style=\"" + td.attr(STYLE) + "\" class=\""+ removeBorderAttributesFromClass(td.attr(CLASS)) +"\">")
				.append(cell.getValue().replaceAll("\\n","<br>"))
				.append("</"+P+">");

		td.append(cellData.toString());

	}

	/**
	 * Helper method to remove the border related classes from the list of classes.
	 * This is required during the html/pdf export to avoid showing the borders twice, once for the paragraph
	 * and once for the enclosing td.
	 * @param classData
	 * @return String of classes without the border related classes
	 */
	private static String removeBorderAttributesFromClass(String classData) {
		String[] classes = classData.split(" ");
		for (int i = 0 ; i < classes.length ; i++) {
			if (classes[i].startsWith("thin") ||
					classes[i].startsWith("medium") ||
					classes[i].startsWith("dashed")||
					classes[i].startsWith("thick") ||
					classes[i].startsWith("solid") ||
					classes[i].startsWith("dotted") ||
					classes[i].startsWith("double")) {
				classes[i] = "";
			}
		}

		return String.join(" ",classes).trim();
	}

	private static Cell applyRowAndColFormatting(Cell cell, StyledItem rowStyle, StyledItem colStyle) {

		CellStyle style = (cell.getStyle() == null) ? new CellStyle() : cell.getStyle();
		String formatter = style.getFormatter();

		if (formatter == null || formatter.isEmpty()) {

			String rowFormatter = getRowColFomatter(rowStyle);
			String colFormatter = getRowColFomatter(colStyle);

			formatter = (rowFormatter != null) ? rowFormatter : colFormatter;
			style.setFormatter(formatter);
			cell.setStyle(style);
		}

		return cell;
	}

	private static String getRowColFomatter(StyledItem rowColstyle) {

		if (rowColstyle == null || rowColstyle.getStyle() == null || rowColstyle.getStyle().getFormatter() == null) {
			return null;
		}
		return rowColstyle.getStyle().getFormatter();
	}

	private static void applyImages(Element table, List<Image> images) {

		if (images == null) {
			return;
		}
               
        for (Image image : images) {

        	String style = "height:%spx; width:%spx; background-image:url('%s'); top:%spx; left:%spx;";
        	String styleValue = "";
        	double xPos = image.getX();
			double yPos = image.getY();
			double h = image.getHeight();
			double w = image.getWidth();
			String src = getImage(image.getSrc(), (int) w, (int) h, image.getType());
			
			Element img = new Document("").createElement(DIV);

			if(image.getStartRow() > -1 && image.getStartColumn() > -1) {

    			styleValue = String.format(style, h, w, src, image.getStartRowOffset(), image.getStartColumnOffset());
    			Element td = table.select("tbody").get(0).child(image.getStartRow()).child(image.getStartColumn()); 
    			td.appendChild(img);
    			td.addClass("relative-position");

        	}
        	else {
    			styleValue = String.format(style, h, w, src, yPos, xPos);
    			table.prependChild(img);
        	}

			img.attr(STYLE, styleValue + " background-size:100% 100%; position:absolute;");                       
        }
	}

	private static String getImage(String sourceData, int w, int h, String type) {

		try {
			byte[] imageByte = Base64.decodeBase64(sourceData.split(",")[1]);
			ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
			BufferedImage image = ImageIO.read(bis);
			bis.close();

			if (w == image.getWidth() && h == image.getHeight()) {
				return sourceData;
			}

			// If width and height do not match, then we scale image for proper
			// display
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			java.awt.Image i = image.getScaledInstance(w, h, java.awt.Image.SCALE_DEFAULT);
			BufferedImage bufferedImage = new BufferedImage(i.getWidth(null), i.getHeight(null),
					BufferedImage.TYPE_INT_RGB);

			bufferedImage.getGraphics().drawImage(i, 0, 0, null);
			ImageIO.write(bufferedImage, type, baos);
			baos.flush();
			byte[] imageInByte = baos.toByteArray();
			String img = Base64.encodeBase64String(imageInByte);
			baos.close();

			return "data:image/" + type + ";base64," + img;
		} catch (Exception e) {
			log.error("Unable to shrink image " + e.toString());
			return sourceData;
		}
	}

	private static void applyTableStyles(Element table, List<TableStyle> tables) {

		if (tables == null) {
			return;
		}

		for (TableStyle tStyle : tables) {

			String tableClass = tStyle.getStyle();

			int rowFrom = tStyle.getRow();
			int rowTo = rowFrom + tStyle.getRowCount();
			int colFrom = tStyle.getCol();
			int colTo = colFrom + tStyle.getColCount();

			List<TableStyleHeader> jsonHeaders = tStyle.getHeaders();
			String selector = "%s:eq(%s), %s:gt(%s):lt(%s)";

			String rowSelector = String.format(selector, "tr", rowFrom, "tr", rowFrom, rowTo);
			String tdsSelector = String.format(selector, "td", colFrom, "td", colFrom, colTo);

			Elements trs = table.select(rowSelector);

			if (trs.isEmpty()) {
				return;
			}

			Elements tds = trs.select(tdsSelector);

			if (tds.isEmpty()) {
				return;
			}

			tds.addClass(tableClass);
			trs.last().select(tdsSelector).addClass("last");

			for (int i = 0; i < trs.size(); i++) {

				if (i % 2 == 0) {

					Elements tdSection = trs.eq(i).select(tdsSelector);
					if (tStyle.isBandColumns()) {
						tdSection.select(":nth-child(even)").addClass("even");
						tdSection.select(":nth-child(odd)").addClass("odd");

					} else {
						tdSection.addClass("even");
					}

				} else {
					trs.eq(i).select(tdsSelector).addClass("odd");
				}
			}

			Elements headers = trs.eq(0).select(tdsSelector);
			headers.addClass("first");

			if (tStyle.isShowHeader()) {
				headers.addClass("head");

				for (int c = 0; c < headers.size(); c++) {
					Element th = headers.get(c);
					th.text(jsonHeaders.get(c).getName());
					th.removeClass("odd");
				}
			}
		}
	}

	public static void applyColRowHeaderData(Element table, Sheet sheet,int colCount) {
                RowHeaderData rowH = sheet.getRowHeaderData();
                ColHeaderData colH = sheet.getColHeaderData();
		boolean hasRowHeader = isVisibleHeader(rowH);

		Element tBody = table.select(TBODY).first();
		if (hasRowHeader) {
                        
			Elements children = tBody.children();
			for (int rIndex = 0; rIndex < children.size(); rIndex++) {

				Element tr = children.get(rIndex);
				Element td = tr.prependElement(TD);
				td.addClass("td-header-data");
				td.text(String.valueOf(rIndex + 1));

			}
		}

		if (isVisibleHeader(colH)) {
            double colHeaderHeight = sheet.getColHeaderHeight();
            String style = String.format("height:%spx !important;",colHeaderHeight);
			Element tHead = table.select(THEAD).first();
			Element tr = tHead.appendElement(TR).attr(CELL_HEIGHT,Double.toString(colHeaderHeight));
			int startIndex = 1;

			if (hasRowHeader) {
				startIndex = 0;
				colCount++;
			} else {
				colCount++;
			}

			for (int cIndex = startIndex; cIndex < colCount; cIndex++) {
				Element th = tr.appendElement(TH).attr(STYLE,style);;
				th.text(IntToLetters(cIndex));
			}
		}
	}

	public static boolean isVisibleHeader(ColRowHeaderData colRow) {

		String crimson = "crimson";

		try {
			String rColor = colRow.getHeaderDataArray().get(0).getStyle().getBackColor();
			if (crimson.equals(rColor)) {
				return false;
			}
		} catch (Exception rowException) {

		}
		return true;
	}

	public static String IntToLetters(int value) {
		String result = "";
		while (--value >= 0) {
			result = (char) ('A' + value % 26) + result;
			value /= 26;
		}
		return result;
	}
}