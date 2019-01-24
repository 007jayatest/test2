package com.addteq.confluence.plugin.excellentable.listener;

import javax.inject.Named;

import org.apache.commons.lang3.math.NumberUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.addteq.confluence.plugin.excellentable.ao.ExcellentableManager;
import com.addteq.confluence.plugin.excellentable.model.ExcellentableModel;
import com.addteq.service.excellentable.exc_io.utils.DomUtils;
import com.atlassian.confluence.core.ContentEntityManager;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.core.DefaultSaveContext;
import com.atlassian.confluence.core.SaveContext;
import com.atlassian.confluence.event.events.content.blogpost.BlogPostCreateEvent;
import com.atlassian.confluence.event.events.content.page.PageCreateEvent;
import com.atlassian.confluence.event.events.template.TemplateUpdateEvent;
import com.atlassian.confluence.pages.BlogPost;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.templates.PageTemplate;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.spring.scanner.annotation.imports.ConfluenceImport;

@Named
public class ContentListener implements InitializingBean, DisposableBean {

	private EventPublisher eventPublisher;
	private ExcellentableManager excManager;
	private ContentEntityManager contentEntityManager;
	private static final Logger log = LoggerFactory.getLogger(ContentListener.class);

	@Autowired
	public ContentListener(@ConfluenceImport EventPublisher eventPublisher,  ExcellentableManager excManager,
			@ConfluenceImport @Qualifier("contentEntityManager")ContentEntityManager contentEntityManager) {
		this.eventPublisher = eventPublisher;
		this.excManager = excManager;
		this.contentEntityManager = contentEntityManager;
	}
	
	@EventListener
	public void templateCreateEvent(TemplateUpdateEvent event) {
		
		// Old template is null for new templates
		if(event.getOldTemplate() != null) {
			return;
		}
		
		PageTemplate template = event.getNewTemplate();
		Document dom = DomUtils.getContentDom(template.getContent());
		Elements macros = dom.getElementsByAttributeValue("ac:name", "excellentable");
		
		for(Element macro : macros) {
			
			ExcellentableModel excTable= getExcellentableModel(macro);

			if(!sameId(excTable.getContentEntityId(), template.getId())){
				excTable.setContentEntityId(template.getId());
				excManager.updateWithoutHistory(excTable);
			}
		}
	}

	@EventListener
	public void pageCreateEvent(PageCreateEvent event) {
		Page page = event.getPage();
		updateExcellentableMacros(page, page.getSpaceKey());
	}

	@EventListener
	public void blogCreateEvent(BlogPostCreateEvent event) {
		BlogPost blog = event.getBlogPost();
		updateExcellentableMacros(blog, blog.getSpaceKey());
	}
	
	private boolean sameId(long excId, long contentId) {
		return Long.compare(excId, contentId) == 0;
	}

	private void updateExcellentableMacros(ContentEntityObject content, String spaceKey) {
		
		Document dom = DomUtils.getContentDom(content.getBodyAsString()); 
		Elements macros = dom.getElementsByAttributeValue("ac:name", "excellentable");
		boolean updateStorageFormat = false;
		
		for(Element macro : macros) {
			ExcellentableModel excTable= getExcellentableModel(macro);

			if(excTable == null)
				continue;
			
			if("draft".equals(excTable.getContentType())) {
				// update content id
					excTable.setContentType(content.getType());
					excTable.setContentEntityId(content.getId());
					excManager.updateWithoutHistory(excTable);
			}
			else if(!sameId(excTable.getContentEntityId(),content.getId())) {

				excTable.setContentEntityId(content.getId());
				excTable.setCreator(content.getCreator().getName());
				excTable.setSpaceKey(spaceKey);
				excTable.setContentType(content.getType());

				excTable = excManager.createExcellentable(excTable);
				
				setExcellentableId(macro, excTable.getID());
				updateStorageFormat = true;
				
			}
		}
		
		if(updateStorageFormat) {
			DomUtils.updateContent(content, dom, contentEntityManager);

		}
	}

	private ExcellentableModel getExcellentableModel(Element macro) {
		Elements params = macro.getElementsByAttributeValue("ac:name", "excellentable-id");
		
		if(params.isEmpty() || !NumberUtils.isCreatable(params.first().text().trim())) {
			return null;
		}
		Integer excId = Integer.valueOf(params.first().text().trim());
		
		return excManager.getExcellentable(excId);
	}
	

	
	private void setExcellentableId(Element macro, int id) {
		Elements params = macro.getElementsByAttributeValue("ac:name", "excellentable-id");			
		params.first().text(String.valueOf(id).trim());
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		eventPublisher.register(this);
	}

	@Override
	public void destroy() throws Exception {
		eventPublisher.unregister(this);
	}
}