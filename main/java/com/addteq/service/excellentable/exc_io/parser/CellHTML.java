package com.addteq.service.excellentable.exc_io.parser;

import com.addteq.service.excellentable.exc_io.spreadjs.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by rober on 10/17/2016.
 */
public class CellHTML {

	private static final org.slf4j.Logger log = LoggerFactory.getLogger(CellHTML.class);

	private static final String DIV = "div";
	private static final String A = "a";
	private String value = "";

	private Set<String> classes = new HashSet<String>();
	private List<String> style = new ArrayList<String>();
	private Element cellDom = new Document("").createElement(DIV);
	private Cell excCell;

	public CellHTML(Cell excCell) {
		this.excCell = excCell;
		this.value = excCell.getFormattedText();
		CellStyle cellStyle = excCell.getStyle();

		if (cellStyle != null) {
			setClassesAndStyle(cellStyle);
			CellType cellType = cellStyle.getCellType();
			// link type
			if (cellType != null && cellType.getTypeNumber() == 8) {
				applyLink((LinkCellType) cellType);
			}
		}
	}

	private void setFontProperties(Font font) {

		if (font == null) {
			return;
		}

		if (font.isBold()) {
			classes.add("bold");
		}

		if (font.isItalic()) {
			classes.add("italic");
		}

		String familyFont = font.getFamily().replace(" ", "_").toLowerCase();
		classes.add(familyFont);

		style.add("font-size:" + font.getSize());

	}

	private void setClassesAndStyle(CellStyle excStyle) {

		if (excStyle.gethAlign() == 1) {
			classes.add("hCenter");
		} else if (excStyle.gethAlign() == 2 ||
		// If align is not defined && (values is numeric OR value is a date)
				(excStyle.gethAlign() == -1 && (NumberUtils.isCreatable(excCell.getValue()) || excCell.isDate()))) {
			classes.add("hRight");
			// css text-indent can not be apply to right align text. we use padding as workaround
			setTextIndent("padding-right", excStyle.getTextIndentInPixels() );
		}
		else {
			setTextIndent("margin-left", excStyle.getTextIndentInPixels() );
		}
		

		setFontProperties(excStyle.getFont());

		if (excStyle.getFontColor() != null) {
			style.add("color:" + excStyle.getFontColor());
		}

		if (excStyle.getBackColor() != null) {
			style.add("background:" + excStyle.getBackColor());
		}

		if (excStyle.isUnderLine()) {
			classes.add("underline");
		}

		if (excStyle.isStrike()) {
			classes.add("strike");
		}

		if (excStyle.getvAlign() == 0) {
			classes.add("vTop");
		} else if (excStyle.getvAlign() == 2) {
			classes.add("vBottom");
		}

		setBorder(excStyle.getBorderLeft(), "Left");
		setBorder(excStyle.getBorderRight(), "Right");
		setBorder(excStyle.getBorderTop(), "Top");
		setBorder(excStyle.getBorderBottom(), "Bottom");

	}
	
	private void setTextIndent(String prop, int value) {
		
		if(value != 1) {
			style.add(String.format("%s:%spx !important",prop,value));
		}
	}

	private void applyLink(LinkCellType cellType) {

		Element link = new Document("").createElement(A);
		String text = (cellType.getText() != null) ? cellType.getText() : this.value;
		String linkUrl = (cellType.getLinkToolTip() != null) ? cellType.getLinkToolTip() : this.value;
		
		link.attr("href", linkUrl);
		link.text(text);
		this.value = link.outerHtml();

	}

	private void setBorder(Border border, String position) {

		if (border == null) {
			return;
		}

		int styleInt = border.getStyle();
		String bckg = String.format("border-%s-color:%s", position.toLowerCase(), border.getColor().toLowerCase());
		String borderStyle = "";

		switch (styleInt) {
		case 1:
			borderStyle = "solid%s thin%s";
			break;
		case 2:
			borderStyle = "solid%s medium%s";
			break;
		case 3:
		case 9:
			borderStyle = "dashed%s";
			break;
		case 4:
		case 7:
		case 11:
			borderStyle = "dotted%s";
			break;
		case 5:
			borderStyle = "solid%s thick%s";
			break;
		case 6:
			borderStyle = "double%s";
			break;
		case 8:
		case 10:
			borderStyle = "medium%s dashed%s";
			break;
		case 12:
		case 13:
			borderStyle = "medium%s dotted%s";
			break;
		default:
			log.info("Unable to find border style for styleInt" + styleInt);
			break;
		}

		borderStyle = String.format(borderStyle, position, position);
		this.classes.add(borderStyle);
		this.style.add(bckg);
	}

	public void addStyle(String style) {
		this.style.add(style);
	}

	public Element asAppendableDOMNode() {

		return cellDom.html(this.value);
	}

	public Set<String> getClassest() {
		return this.classes;
	}

	public String getStyle() {
		return StringUtils.join(this.style, ";");
	}

	public String getValue() {
		return this.value;
	}
}