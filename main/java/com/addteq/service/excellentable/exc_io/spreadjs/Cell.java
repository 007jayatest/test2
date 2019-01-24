package com.addteq.service.excellentable.exc_io.spreadjs;

import com.addteq.service.excellentable.exc_io.utils.ETDateUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.regex.Pattern;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class Cell {

    private int rowIndex;
    private int colIndex;
    private String value;
    private CellStyle style;
    private String formula;
    static final Logger LOGGER = LoggerFactory.getLogger(Cell.class);
    private transient boolean isDate = false;

    public Cell(String value, int row, int column) {
        this.value = value;
        rowIndex = row;
        colIndex = column;
    }

    protected Cell() {
    }

    public boolean hasFormula() {
        return formula != null && !formula.trim().equals("");
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public int getColIndex() {
        return colIndex;
    }

    public String getValue() {
        return value;
    }

    public CellStyle getStyle() {
        return style;
    }

    public void setStyle(CellStyle cellStyle) {
        style = cellStyle;
    }

    private String getDateFormat(String formatter, CellFormatter autoFormat) {
        String dateFormat = "MM/dd/yyyy";

        if (formatter != null && !formatter.isEmpty()) {
            dateFormat = formatter;
        } else if (autoFormat != null) {
            dateFormat = autoFormat.getFormatCached();
        }

        /*
		 * Ref:EXC-2745 As per microsoft office formatter documentation The "m"
		 * or "mm" must appear immediately after the "h" or "hh" format code or
		 * immediately before the "ss" code, or Graph displays the month instead
		 * of the minutes. hence we have used (?<!h:)m(?!:s) regex to exclude m
		 * which is not preceded by h: or not followed by :s ref link:
		 * https://support.office.com/en-us/article/Formats-for-dates-and-
		 * times-f014b617-7a4a-415c-95bc-a911c9cfe3ae?ui=en-US&rs=en-US&ad= US
         */
        if (dateFormat != null) {
            dateFormat = dateFormat.replaceAll("(?<!h:)m(?!:s)", "M");
        }

        return dateFormat;
    }

    private String getNumberFormat(String formatter) {
    	if(formatter == null || formatter.trim().isEmpty()) {return null;}

        return formatter;
    }

    public String getFormattedText() {
        String val = getValue();
        String formatter = null;
        CellFormatter autoFormat = null;
        SimpleDateFormat dateFormatter = null;

        if (getStyle() != null) {
            formatter = getStyle().getFormatter();
            autoFormat = getStyle().getAutoFormatter();
        }

        if ((formatter == null && autoFormat == null && !hasFormula()) || (formatter != null && "GENERAL".equalsIgnoreCase(formatter))) {
            return val;
        }


        try {
            Date date = null;
            String dateFormat = getDateFormat(formatter, autoFormat);
            if (dateFormat != null) {
                dateFormatter = new SimpleDateFormat(dateFormat);
            }

            /*
			 * Ref:EXC-2745 In some cases (e.g when we import sheet from Google
			 * spreadsheets) some of the date formatted cells have value in the
			 * form of OADate e.g.OADate(3456) in this case get the date double
			 * value from cell value (e.g.3456) & get the date object from this
			 * double value.
             */
            if (val.contains("OADate") && Pattern.compile("\\d+").matcher(val).find()) {
                String excelNum = val.split(Pattern.quote("("))[1].split(Pattern.quote(")"))[0];
                date = getDateFromDouble(Double.parseDouble(excelNum));
                if (dateFormatter != null) {
                    val = dateFormatter.format(date);
                    this.isDate = true;
                }
            } /*
			 * Ref:EXC-2745 If the cell is having formula in it & date
			 * formatting is applied on that cell.
             */ else if (hasFormula() && Pattern.compile("(?i:d|m|y)").matcher(dateFormat).find()
                    && StringUtils.isNumeric(val)) {
                date = getDateFromDouble(Double.parseDouble(val));

                /**
                 * Ref: EXC-4575 - Patch for this - if the JSON cell value has a formula with no autoformatters
                 * or formatters in it, we only need the value - so dont do any further calculations
                 * - JUST return the value!
                 */
                if (isBlank(formatter) && (autoFormat == null)) {
                    return val;
                }

                if (dateFormatter != null) {
                    val = dateFormatter.format(date);
                    this.isDate = true;
                }
            } /*
			 * In some scenario cell has date value in the form of string which
			 * in form of yyyy-mm-dd'T'hh:mm:ssZ
             */ else if (val.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z")) {
                date = Date.from(Instant.parse(val));
                if (dateFormatter != null) {
                    val = dateFormatter.format(date);
                    this.isDate = true;
                }
            } else {
                /**
                 * If there is no formatter but only an autoformatter, then check the element called formatCached within
                 * autoformatter and if that is present then set the formatter to formatCached. This typically happens
                 * when the user explicitly types value like '5%' within the cell without applying percentage format.
                 * Ref : EXC-4079
                 */
                if (formatter == null && autoFormat != null) {
                    String formatCached = autoFormat.getFormatCached();
                    if (isNotBlank(formatCached)) {
                        formatter = formatCached;
                    }
                }

                try {
                	String stringVal = val.trim();
       			 	String numberFormat = getNumberFormat(formatter);

            		
            		if(NumberUtils.isCreatable(stringVal) && numberFormat != null) {
                         DecimalFormat numberFormatter = new DecimalFormat(numberFormat);
                         double numVal = Double.parseDouble(stringVal);
                         val = numberFormatter.format(numVal);
            		}
                } catch (Exception exception) {
                    LOGGER.error("Number exception while applying formatter", exception);
                }
            }
            
        } catch (Exception exception) {
            LOGGER.error("Date exception while applying formatter ", exception);
        }

        return val;
    }

    public boolean isDate() {
        return this.isDate;
    }

    private Date getDateFromDouble(Double excelDateDouble) {
        if (excelDateDouble < 0) {
            return ETDateUtils.fromDoubleToDateTime(excelDateDouble);
        } else {
            return DateUtil.getJavaDate(excelDateDouble);
        }
    }
}