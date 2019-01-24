package com.addteq.service.excellentable.exc_io.importfile;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CellStyle {

    final static Logger LOGGER = LoggerFactory.getLogger(CellStyle.class);

    private String backColor;
    private String font = "";
    private String foreColor;
    private int hAlign;
    private int vAlign;
    //private String themeFont ;
    //private String formatter ;

    /*private String borderLeft ;
	private String borderTop ;
	private String borderRight ;
	private String borderBottom ;*/
    //private String locked ;
    private int textIndent;
    //private String wordWrap ;
    private String shrinkToFit;
    //private String backgroundImage ;
    //private String cellType ;
    //private String backgroundImageLayout ;
    //private String tabStop ;
    private int textDecoration = 0;
    //private String imeMode ;
    //private String name ;
    //private String parentName ;
    //private String watermark ;
    private transient String fontSize = "13px";
    private transient String fontFamily = "Verdana";
    private transient String fontWeight = "";
    private transient String fontStyle = "";
    
    private transient Element td;
	
	public CellStyle(Element td) {
		this.td = td;
	}

    public void setPropertyValue(String prop, String value) {

        if ("background".equals(prop) || "background-color".equals(prop)) {
            value = value.toLowerCase();
            if (!"white".equals(value) && !"#ffffff".equals(value) && !"#fff".equals(value)) {
                this.backColor = value;
            }
        } else if ("font".equals(prop)) {
            this.font = value;
        } else if ("font-size".equals(prop) || "line-height".equals(prop)) {
            this.fontSize = value;
        } else if ("font-family".equals(prop)) {
            this.fontFamily = value.replaceAll("\'", "");
        } else if ("font-style".equals(prop)) {
            this.fontStyle = value;
        } else if ("font-weight".equals(prop)) {
            this.fontWeight = value;
        } else if ("color".equals(prop)) {
            this.foreColor = value;
        } else if ("text-align".equals(prop)) {
            this.hAlign = getTextAlign(value);
        } else if ("vertical-align".equals(prop)) {
            this.vAlign = getVerticalAlign(value);
        } /* else if( "border-left".equals(prop)){
            	this.borderLeft = value;
            }
                     
            else if( "border-top".equals(prop)){
            	this.borderTop = value;
            }
                     
            else if( "border-right".equals(prop)){
            	this.borderRight = value;
            }
                     
            else if( "border-bottom".equals(prop)){
            	this.borderBottom = value;
            } */ else if ("text-indent".equals(prop)) {
            this.textIndent = getTextIndent(value);
        } else if ("text-decoration".equals(prop)) {
            setTextDecoration(value);
        }

    }
    
    public void applyHighlightColor() {
		
		if(td.hasAttr("data-highlight-colour")) {
			String color = td.attr("data-highlight-colour");
			String backgroundColor = "";
			
			if("grey".equals(color)) {
				backgroundColor = "#f0f0f0";
			}
			else if("red".equals(color)) {
				backgroundColor = "#ffe7e7";
			}
			else if("green".equals(color)) {
				backgroundColor = "#ddfade";
			}
			else if("blue".equals(color)) {
				backgroundColor = "#e0f0ff";
			}
			else if("yellow".equals(color)) {
				backgroundColor = "#ffd";
			}
			
			setPropertyValue("background-color", backgroundColor);
		}
		
	}
	
	public void additionalFormatting() {
		
		if(shouldApplyFormatting("strong")) {
			setPropertyValue("font-weight", "bold");
		}
		if(shouldApplyFormatting("em")) {
			setPropertyValue("font-style", "italic");	
		}
		if(shouldApplyFormatting("u")) {		
			setPropertyValue("text-decoration", "underline");	

		}
		if(shouldApplyFormatting("span")) {
			Elements els = td.select("span");

			if(sameStyleAttributes(els)) {
				
				applyStyleAttributes(els.first());
				
			}

		}
		if(shouldApplyFormatting("s")) {
			setPropertyValue("text-decoration", "line-through");	
		}
		
	}
	
	private boolean sameStyleAttributes(Elements els) {
		
		List<String> styleAttributes = new ArrayList<>();
		
		for(Element el : els) {
			styleAttributes.add(el.attr("style"));
		}
		
		Set<String> set = new HashSet<>(styleAttributes);
		
		return set.size() == 1;
		
	}
	
	public void applyStyleAttributes(Element el) {
		
		   List<String> attributes = Arrays.asList(el.attr("style").split(";"));


	        for (int i = 0; i < attributes.size(); i++) {
	            try {
	                if (!"".equals(attributes.get(i))) {
	                    String[] attribute = attributes.get(i).split(":");
	                    String attributeName = attribute[0].trim();
	                    String attributeValue = attribute[1].trim();

	                    setPropertyValue(attributeName, attributeValue);
	                }
	            }
	            catch(Exception e){
	                LOGGER.error("could not get style for {}", attributes.get(i), e);
	            }
	        }
	}
	
	private boolean shouldApplyFormatting(String tag) {
		
		Elements els = td.select(tag);

		if(els.isEmpty())
			return false;
		
		return els.text().equals(td.text());	
	}

    public JsonObject toJson() {
        Gson gson = new Gson();
        setFont();
        JsonParser jsonParser = new JsonParser();
        JsonObject jo = (JsonObject) jsonParser.parse(gson.toJson(this));
        return jo;
    }

    private int getTextAlign(String align) {

        int tAling = 0;

        if ("center".equals(align)) {
            tAling = 1;
        } else if ("right".equals(align)) {
            tAling = 2;
        }
        return tAling;
    }

    private int getVerticalAlign(String align) {

        int vAling = 0;

        if ("middle".equals(align)) {
            vAling = 1;
        } else if ("text-bottom".equals(align) || "bottom".equals(align)) {
            vAling = 2;
        }
        return vAling;
    }

    private int getTextIndent(String value) {

        int indent = 0;
        try {
            int px = Integer.valueOf(value.replace("px", "").trim());
            indent = px / 8;
        } catch (Exception e) {
            LOGGER.error("Unable to parse text indent" + e);
        }
        return indent;
    }

    private void setTextDecoration(String value) {

        if (value.contains("underline")) {
            this.textDecoration += 1;
        }

        if (value.contains("overline")) {
            this.textDecoration += 4;
        }

        if (value.contains("line-through")) {
            this.textDecoration += 2;
        }
    }

    private void setFont() {
        String ws = " ";
        if (font.isEmpty()) {
            font = (fontStyle + ws + fontWeight + ws + fontSize + ws + fontFamily).trim();
        }
    }
}
