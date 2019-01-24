package com.addteq.confluence.plugin.excellentable.parser;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import com.addteq.service.excellentable.exc_io.parser.JsonHtmlParser;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import com.addteq.confluence.plugin.excellentable.TestingUtils;

import junit.framework.Assert;

public class JsonHtmlParserTest {

	private Element table = null;

	@Test
	public void getHTMLTable() throws IOException {

		String tableString = JsonHtmlParser.getHTML(getMetadata("parser/SampleSpreadsheet.json"), false, "", true);

		Element div = new Document("").createElement("div");
		table = div.append(tableString);
		Elements trs = table.select("tbody tr");

		// bold
		assertTrue(getTd(0, 0).hasClass("bold"));
		// italic
		assertTrue(getTd(1, 0).hasClass("italic"));
		// font-color red
		assertTrue(getTd(2, 0).attr("style").contains("color:#FF0000"));

		// background:#0000FF;color:#FFFFFF
		assertTrue(getTd(3, 0).attr("style").contains("background:#0000FF"));
		assertTrue(getTd(3, 0).attr("style").contains("color:#FFFFFF"));

		assertTrue(getTd(4, 0).hasClass("underline"));

		assertTrue(getTd(5, 0).hasClass("strike"));

		assertTrue(getTd(6, 0).hasClass("bold"));
		assertTrue(getTd(6, 0).hasClass("italic"));

		assertTrue(getTd(7, 0).hasClass("bold"));
		assertTrue(getTd(7, 0).hasClass("italic"));
		assertTrue(getTd(7, 0).attr("style").contains("color:#FF4000"));

		Assert.assertEquals("5345", getTd(8, 0).text());

		// date align center by user
		Assert.assertEquals("10/04/2017", getTd(22, 1).text());
		Assert.assertEquals("hCenter", getTd(22, 1).className());

		// date auto align right
		Assert.assertEquals("07/06/2017", getTd(22, 2).text());
		Assert.assertEquals("hRight", getTd(22, 2).className());

		// date align left by user do not add class since it is default
		Assert.assertEquals("07/06/2017", getTd(22, 3).text());
		Assert.assertEquals("", getTd(22, 3).className());

		Assert.assertEquals("-$100.00", getTd(21, 7).text());
		Assert.assertEquals("$100.00", getTd(21, 8).text());

		Assert.assertEquals("1.1515", getTd(0, 2).text());

		Assert.assertEquals("3.3545", getTd(0, 3).text());

		Assert.assertEquals("4.57", getTd(0, 4).text());

		Assert.assertEquals("3.4", getTd(0, 5).text());

		Assert.assertEquals("240", getTd(0, 6).text());
		Assert.assertEquals("New lines should be replaced by br tags","<p style=\"" + getTd(24, 2).attr("style") + "\" class=\""+ getTd(24, 2).attr("class") +"\">" +
				"Step 1<br>Step 2<br>Step 3</p>", getTd(24, 2).html());

		Assert.assertEquals("The paragraph tag containing the data should NOT inherit the border classes from the enclosing td tag",
				"<p style=\"" + getTd(24, 3).attr("style") + "\" class=\"arial\">" +
				"Step 1 Step 2 Step 3</p>", getTd(24, 3).html());

		Assert.assertEquals("Class of the paragraph should be arial_black",
				"<p style=\"" + getTd(24, 4).attr("style") + "\" class=\"arial_black\">" +
						"Step 1 Step 2 Step 3</p>", getTd(24, 4).html());

	}

	@Test
	public void colRowFormatting() throws IOException {

		String tableString = JsonHtmlParser.getHTML(getMetadata("json/Table6.json"), false, "", true);

		// System.out.println(tableString);

	}

	@Test
	public void imagesShouldbeIgnoredinHTMLandPDFExport() throws IOException {
		String tableString = JsonHtmlParser.getHTML(getMetadata("parser/tableWithImages.json"), false, "", false);

		Element div = new Document("").createElement("div");
		table = div.append(tableString);
		Assert.assertNotNull("Table should be produced BUT without images", table);
	}

	private Element getTd(int row, int col) {
		col++;
		String tdSelector = String.format("tr:eq(%s) td:eq(%s)", row, col);
		return table.select(tdSelector).first();
	}

	private String getMetadata(String source) throws IOException {

		File sampleSpreadSheetFile = TestingUtils.getResourceFile(getClass(), source);

		return TestingUtils.readFileIntoString(sampleSpreadSheetFile);
	}
}