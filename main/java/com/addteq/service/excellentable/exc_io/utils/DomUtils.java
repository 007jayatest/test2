package com.addteq.service.excellentable.exc_io.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.atlassian.confluence.core.ContentEntityManager;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.core.DefaultSaveContext;
import com.atlassian.confluence.core.SaveContext;

public class DomUtils {

	
	public static Document getContentDom(String body) {
		Document dom = Jsoup.parse(body);
		dom.outputSettings().syntax(Document.OutputSettings.Syntax.xml); 
		dom.outputSettings().indentAmount(0).prettyPrint(false);
		return dom;
	}
	
	public static Elements getExcellentableMacros(Document dom) {
		return dom.getElementsByAttributeValue("ac:name", "excellentable");
	}
	public static Element findMacroByExcellentableId(int id, Document doc) {
		
		Elements macros = getExcellentableMacros( doc) ;
		String idS = String.valueOf(id);
		for(Element macro : macros) {
			
			if(macro.text().equals(idS)) {
				return macro;
			}
		}
		
		return null;
		
	}
	
	public static Element closestByTag(String tag, Element el ) {
		
		for(Element parent : el.parents()) {
			
			if(parent.tagName().equals(tag)) {
				return parent;
			}
		}
		
		return null;
	}
	
	public static void updateContent(ContentEntityObject content, Document dom, ContentEntityManager contentEntityManager) {
		
		String body = dom.select("body").html();
		content.setBodyAsString(body);
		
		SaveContext context = DefaultSaveContext.builder().suppressEvents(true)
		.suppressAutowatch(true)
		.suppressNotifications(true)
		.build();
		
		contentEntityManager.saveContentEntity(content, context);
	}
}
