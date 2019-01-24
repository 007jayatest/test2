package com.addteq.confluence.plugin.excellentable.json;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.addteq.service.excellentable.exc_io.json.ExcellentableSpreadParser;
import org.junit.Test;

import com.addteq.confluence.plugin.excellentable.TestingUtils;
import com.addteq.service.excellentable.exc_io.spreadjs.Border;
import com.addteq.service.excellentable.exc_io.spreadjs.Cell;
import com.addteq.service.excellentable.exc_io.spreadjs.CellStyle;
import com.addteq.service.excellentable.exc_io.spreadjs.ColHeaderData;
import com.addteq.service.excellentable.exc_io.spreadjs.ColRowInfo;
import com.addteq.service.excellentable.exc_io.spreadjs.ExcellentableSpread;
import com.addteq.service.excellentable.exc_io.spreadjs.Font;
import com.addteq.service.excellentable.exc_io.spreadjs.Image;
import com.addteq.service.excellentable.exc_io.spreadjs.LinkCellType;
import com.addteq.service.excellentable.exc_io.spreadjs.Range;
import com.addteq.service.excellentable.exc_io.spreadjs.RowHeaderData;
import com.addteq.service.excellentable.exc_io.spreadjs.Sheet;
import com.addteq.service.excellentable.exc_io.spreadjs.TableStyle;
import com.addteq.service.excellentable.exc_io.spreadjs.TableStyleHeader;

import org.junit.Assert;

public class SpreadJSParseTest {

	public Sheet sheet;
	public Cell cell;
	public CellStyle style;
	public Border border;
	public Font font;

	public String loadJson(String fileName) {

		File jsonFile = new File(getClass().getClassLoader().getResource("json/" + fileName + ".json").getFile());

		try {
			return TestingUtils.readFileIntoString(jsonFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "";
	}

	@Test
	public void formattedText() {

		ExcellentableSpread spread = ExcellentableSpreadParser.parse(loadJson("Table1"));
		sheet = spread.getSheets().get("0");

		setCellTest(0, 0);

		Assert.assertEquals("aaa", cell.getValue());

		setCellTest(1, 0);

		Assert.assertEquals("123", cell.getValue());

		setCellTest(2, 0);
		Assert.assertEquals("/OADate(43099)/", cell.getValue());
		Assert.assertEquals("12/30/2017", cell.getFormattedText());
		Assert.assertEquals(true, cell.isDate());

		setCellTest(3, 0);
		Assert.assertEquals("/OADate(42739)/", cell.getValue());
		Assert.assertEquals("1/4/2017", cell.getFormattedText());
		Assert.assertEquals(true, cell.isDate());

		setCellTest(4, 0);
		Assert.assertEquals("10", cell.getValue());
		Assert.assertEquals("10.00", cell.getFormattedText());

		setCellTest(5, 0);
		Assert.assertEquals("2200", cell.getValue());
		Assert.assertEquals("2,200.00", cell.getFormattedText());

		setCellTest(6, 0);
		Assert.assertEquals("1234", cell.getValue());
		Assert.assertEquals("$1,234.00", cell.getFormattedText());

		setCellTest(7, 0);
		Assert.assertEquals("344", cell.getValue());
		Assert.assertEquals("34400.00%", cell.getFormattedText());

		setCellTest(8, 0);
		Assert.assertEquals("/OADate(279)/", cell.getValue());
		Assert.assertEquals("10/5/1900", cell.getFormattedText());

		setCellTest(8, 0);
		Assert.assertEquals("/OADate(279)/", cell.getValue());
		Assert.assertEquals("10/5/1900", cell.getFormattedText());

		setCellTest(10, 0);

		Assert.assertEquals("1/4/2017", cell.getFormattedText());

		setCellTest(11, 1);

		Assert.assertEquals("5345", cell.getFormattedText());

		setCellTest(11, 2);
		Assert.assertEquals("5%", cell.getFormattedText());

	}

	@Test
	public void testColRowHeader() {

		ExcellentableSpread spread = ExcellentableSpreadParser.parse(loadJson("Table5"));
		sheet = spread.getSheets().get("0");

		ColHeaderData colH = sheet.getColHeaderData();
		RowHeaderData rowH = sheet.getRowHeaderData();

		CellStyle colStyle = colH.getHeaderDataArray().get(0).getStyle();
		CellStyle rowStyle = rowH.getHeaderDataArray().get(0).getStyle();
		String crimson = "crimson";

		Assert.assertEquals(crimson, colStyle.getBackColor());
		Assert.assertEquals(crimson, rowStyle.getBackColor());

	}

	@Test
	public void testSpans() {
		ExcellentableSpread spread = ExcellentableSpreadParser.parse(loadJson("Table4"));
		sheet = spread.getSheets().get("0");
		List<Range> spans = sheet.getSpans();
		Range span = spans.get(0);

		Assert.assertEquals(21, span.getRow());
		Assert.assertEquals(1, span.getRowCount());
		Assert.assertEquals(6, span.getCol());
		Assert.assertEquals(2, span.getColCount());

		setCellTest(13, 7);
		LinkCellType link = (LinkCellType) style.getCellType();

		Assert.assertEquals("Click Here", link.getText());

	}

	@Test
	public void testImages() {
		ExcellentableSpread spread = ExcellentableSpreadParser.parse(loadJson("Table3"));
		List<Image> imgs = spread.getSheets().get("0").getImages();
		Image img = imgs.get(0);

		Assert.assertEquals("#000000", img.getBorderColor());
		Assert.assertEquals(3, img.getBorderRadius());
		Assert.assertEquals(1, img.getFloatingObjectType());
		Assert.assertEquals(1082.0, img.getX(), 0.0);
		Assert.assertEquals(466.0, img.getY(), 0.0);
		Assert.assertEquals(94.933349609375, img.getHeight(), 0.0);
		Assert.assertEquals(107.0, img.getWidth(), 0.0);
		Assert.assertEquals("png", img.getType());

	}

	@Test
	public void test() {
		ExcellentableSpread spread = ExcellentableSpreadParser.parse(loadJson("Table2"));

		Assert.assertNotNull(spread.getVersion());
		Assert.assertEquals(false, spread.isTabStripVisible());
		Assert.assertEquals(false, spread.isNewTabVisible());
		Assert.assertEquals(3, spread.getShowScrollTip());
		Assert.assertEquals(false, spread.isShowVerticalScrollBar());
		Assert.assertEquals(true, spread.isScrollbarMaxAlign());
		Assert.assertEquals("Transparent", spread.getGrayAreaBackColor());

		sheet = spread.getSheets().get("0");

		Assert.assertEquals("Sheet1", sheet.getName());

		Range selection = sheet.getSelection();

		Assert.assertEquals(13, selection.getRow());
		Assert.assertEquals(1, selection.getRowCount());
		Assert.assertEquals(6, selection.getCol());
		Assert.assertEquals(1, selection.getColCount());

		List<ColRowInfo> columns = sheet.getColumns();
		Assert.assertEquals(7, columns.size());

		List<ColRowInfo> rows = sheet.getRows();
		Assert.assertEquals(19, rows.size());

		Assert.assertEquals(19, sheet.getRowCount());
		Assert.assertEquals(7, sheet.getColCount());

		Assert.assertEquals("#47b54d", sheet.getSelectionBorderColor());

		Assert.assertEquals(6, sheet.getActiveCol());
		Assert.assertEquals(13, sheet.getActiveRow());

		Assert.assertEquals(true, sheet.isAllowCellOverFlow());
		Assert.assertEquals("Office", sheet.getTheme());

		TableStyle table = sheet.getTables().get(0);
		Assert.assertEquals("table1", table.getName());
		// table range
		Assert.assertEquals(2, table.getRow());
		Assert.assertEquals(9, table.getRowCount());
		Assert.assertEquals(5, table.getCol());
		Assert.assertEquals(2, table.getColCount());
		Assert.assertEquals("Light2", table.getStyle());
		// table headers
		List<TableStyleHeader> headers = table.getHeaders();
		Assert.assertEquals("Column1", headers.get(0).getName());
		Assert.assertEquals(2, headers.get(1).getId());

		setCellTest(0, 0);

		Assert.assertEquals("Bold", cell.getValue());
		Assert.assertEquals(0, cell.getColIndex());
		Assert.assertEquals(0, cell.getRowIndex());

		// Assert.assertEquals(0, style.gethAlign());
		Assert.assertEquals(true, font.isBold());
		Assert.assertEquals("Verdana", font.getFamily());
		String fontString = font.getSize();
		Assert.assertEquals(13.33, font.getFontSizeNum(fontString), 0.0);

		setCellTest(0, 1);

		Assert.assertEquals("Left", cell.getValue());
		Assert.assertEquals(0, style.gethAlign());

		setCellTest(1, 0);

		Assert.assertEquals("Italic", cell.getValue());
		Assert.assertEquals(true, font.isItalic());

		setCellTest(1, 1);

		Assert.assertEquals("Center", cell.getValue());
		Assert.assertEquals(1, style.gethAlign());

		setCellTest(2, 0);

		Assert.assertEquals("Red", cell.getValue());
		Assert.assertEquals("#FF0000", style.getFontColor());

		setCellTest(2, 1);

		Assert.assertEquals("Right", cell.getValue());
		Assert.assertEquals(2, style.gethAlign());

		setCellTest(2, 3);

		Assert.assertEquals("", cell.getValue());

		border = style.getBorderLeft();
		Assert.assertEquals("Black", border.getColor());
		Assert.assertEquals(1, border.getStyle());

		setCellTest(3, 0);

		Assert.assertEquals("#FFFFFF", style.getFontColor());
		Assert.assertEquals("#0000FF", style.getBackColor());

		setCellTest(4, 0);
		Assert.assertEquals(true, style.isUnderLine());

		setCellTest(4, 1);
		Assert.assertEquals("Top", cell.getValue());

		Assert.assertEquals(0, style.getvAlign());

		setCellTest(4, 3);

		border = style.getBorderRight();
		Assert.assertEquals("Black", border.getColor());
		Assert.assertEquals(2, border.getStyle());

		setCellTest(5, 0);

		Assert.assertEquals(true, style.isStrike());

		setCellTest(6, 0);

		Assert.assertEquals(true, font.isBold());
		Assert.assertEquals(true, font.isItalic());

		setCellTest(6, 1);

		Assert.assertEquals("Bottom", cell.getValue());
		Assert.assertEquals(2, style.getvAlign());

		setCellTest(6, 3);

		border = style.getBorderTop();
		Assert.assertEquals(3, border.getStyle());

	}

	public void setCellTest(int row, int col) {
		cell = sheet.getCell(row, col);
		style = cell.getStyle();

		if (style != null) {
			font = style.getFont();
		}

	}

}