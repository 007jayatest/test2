package com.addteq.confluence.plugin.excellentable.importfile.impl;

import com.addteq.confluence.plugin.excellentable.TestingUtils;
import com.addteq.service.excellentable.exc_io.importfile.impl.ImportHtml;
import com.google.gson.JsonObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

import static org.junit.Assume.assumeTrue;

public class ImportHtmlTest { 

	private JsonObject data;

	@Test
	public void importHtmlTest() throws Exception {

		File testFile = TestingUtils.getResourceFile(getClass(), "html/confluenceTable.html");
		String fileContent = TestingUtils.readFileIntoString(testFile);

		ImportHtml importHtml = new ImportHtml();
		JsonObject json = importHtml.buildImportSheetJson(fileContent, "0");
		data = json.getAsJsonObject("sheets").getAsJsonObject("Sheet1").getAsJsonObject("data")
				.getAsJsonObject("dataTable");

		// bold
		assumeTrue(getCellStyle(2, 1).get("font").getAsString().contains("bold"));
		// italic
		assumeTrue(getCellStyle(2, 2).get("font").getAsString().contains("italic"));
		// underline
		Assert.assertEquals(1, getCellStyle(2, 3).get("textDecoration").getAsInt());
		// red font
		Assert.assertEquals("rgb(255,0,0)", getCellStyle(2, 4).get("foreColor").getAsString());
		// line-through
		Assert.assertEquals(2, getCellStyle(2, 5).get("textDecoration").getAsInt());
		// grey background
		Assert.assertEquals("#f0f0f0", getCellStyle(8, 1).get("backColor").getAsString());
		// red background
		Assert.assertEquals("#ffe7e7", getCellStyle(8, 2).get("backColor").getAsString());
		// green background
		Assert.assertEquals("#ddfade", getCellStyle(8, 3).get("backColor").getAsString());
		// blue background
		Assert.assertEquals("#e0f0ff", getCellStyle(8, 4).get("backColor").getAsString());
		// yellow background
		Assert.assertEquals("#ffd", getCellStyle(8, 5).get("backColor").getAsString());
		// center align
		Assert.assertEquals(1, getCellStyle(7, 3).get("hAlign").getAsInt());
		// right align
		Assert.assertEquals(2, getCellStyle(7, 4).get("hAlign").getAsInt());

	}

	private JsonObject getCellStyle(int row, int col) {
		return getCell(row, col).getAsJsonObject("style");
	}

	private JsonObject getCell(int row, int col) {
		return data.getAsJsonObject(String.valueOf(row)).getAsJsonObject(String.valueOf(col));
	}

}