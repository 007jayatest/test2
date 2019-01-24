/**
 * Confluence.HighlightAction provides registerButtonHandler function that lets us to register our own button to Highlight Actions panel. 
 * To successfully register the button, there are 2 parameters must be provided:
 * 1. PLUGIN_KEY: plugin_key is the combination of the plugin key and button web item’s key. 
 * 2. options: contains the group of event handlers needed for the button, and the important property shouldDisplay 
 * to configure the context in which the button should displayed when text is selected, there are 3 supported contexts:
 * MAINCONTENT_ONLY: in page main content section.
 * COMMENT_ONLY: in comment section.
 * MAINCONTENT_AND_COMMENT: in both page main content and comment sections.
 * 
 */
AJS.toInit(function($) {
    var PLUGIN_KEY = "Addteq.Excellentable:insert-excellentable";
    
    var target = {
    		
    		CONFLUENCE_TABLE : "table.confluenceTable",   		
    		body : "",
    		
    		set : function(el){
    			
    			this.body = "";
    			
    			if(el == null){return;}
    			
    			
    			if(el.closest(this.CONFLUENCE_TABLE).length){
    				this.body = this.getTableBody(el.closest(this.CONFLUENCE_TABLE));
    			} 
    			
    			if(el.hasClass("table-wrap") && el.find(this.CONFLUENCE_TABLE).length){
    				this.body = this.getTableBody(el.find(this.CONFLUENCE_TABLE));
    			}

    		},
    		getBody : function(){
    			return this.body;
    		},
    		isTable : function(){
    			return this.body.length;
    		},
    		getTableBody : function(t){
				
				var $tableClone = $(t).clone();
				if($tableClone.find("thead").length == 2){
					$tableClone.find("thead:first").remove();
				}
				return $tableClone[0].outerHTML;   				
			}
    }
    
    // check if plugin Confluence-highlight-action is enabled
    Confluence.HighlightAction && Confluence.HighlightAction.registerButtonHandler(PLUGIN_KEY, {
        onClick: function(selectionObject) {
        	
            Confluence.HighlightDialogs.showHighlightDialog(selectionObject);
            target.set($(selectionObject.containingElement));
        },
        // this plugin should only affect the main content of a page
        shouldDisplay: Confluence.HighlightAction.WORKING_AREA.MAINCONTENT_ONLY
    });
    
    function removeFailedInlineTable(tableId) {
        var response = jQuery('body').ExcellentableDBOperations({"operation": "remove", ID: tableId});
        response.success(function(data){
            console.debug("The failed Inline Table has been removed successfully.");
        });
        response.error(function(data){
            console.debug("The failed Inline Table could  not be removed.");
        });
    }
    
    jQuery('body').on('click', '#euiInsertExcInlineBtn', function () {
    	
    	var response;
    	// 1. If highlighted text is inside a confluence table or is the table wrapper, then we convert the html table to json spreadjs.
    	// 2. We use the spreadjs to populate the new excellentable
		if(target.isTable()){
			
	            $.ajax({
		                "url": AJS.params.baseUrl + "/rest/excellentable/1.0/content/table/json",
		                "type": "POST",
		                "async": false,
		                "data": JSON.stringify({
									html : [target.getBody()]
								}),
						dataType    : 'json',
		                contentType: "application/json"
	                	
	            }).done(function(result){
					response = jQuery('body').ExcellentableDBOperations({"operation": "create", "metaData": JSON.stringify(result)});

	            });
			
		} 
		// 1. If highlighted text is NOT inside a confluence table
		// 2. Excellentable will be empty
		else{
			response = jQuery('body').ExcellentableDBOperations({"operation": "create", "metaData": ""});
		}

        response.success(function (data) {
            var tableId = data.id;
            var currentTimeStamp = new Date().getTime();
            var selectedText = jQuery("#euiHighlightedText").val();
            var postData = {
                "pageId": AJS.params.pageId,
                "selectedText": selectedText,
                "index": jQuery("#euiIndex").val(),
                "numMatches": jQuery("#euiNumOccurrences").val(),
                "lastFetchTime": currentTimeStamp,
                "xmlModification": "<ac:structured-macro ac:name='excellentable' ac:schema-version='1'><ac:parameter ac:name='excellentable-id'>" + tableId + "</ac:parameter></ac:structured-macro>"
            };
            var settings = {
                "url": AJS.params.baseUrl+"/rest/highlighting/1.0/insert-storage-fragment",
                "type": "POST",
                "processData": false,
                "data": JSON.stringify(postData),
                contentType: "application/json",
                beforeSend: function (xhr) {
                    xhr.setRequestHeader("X-Atlassian-Token", "no-check");
                }
            };
           
            
            $.ajax(settings).done(function (response) {
                if (response === false) {
                	 var notifyHighlightError = jQuery("body").ExcellentableNotification({
                         title: AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.highlight.error"),
                         fadeout: false
                     });
                	notifyHighlightError.showErrorMsg();
                    removeFailedInlineTable(tableId);
                } else {
                	
                	if(target.isTable()){
                		
                		var removeTableUrl = AJS.params.baseUrl+ "/rest/excellentable/1.0/content/table/highlight-action-update"+
                		"?excId=" +  tableId + "&pageId=" + AJS.params.pageId;
                								
                		
                		$.get(removeTableUrl).done(function(){
                    		location.reload();

                		}).fail(function(){
                			 var notifyHighlightError = jQuery("body").ExcellentableNotification({
                                 title: AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.highlight.error.replace"),
                                 fadeout: false
                             });
                        	notifyHighlightError.showErrorMsg();
                		});
                	}
                	else{
                		location.reload();
                	}
                    
                }
            }).error(function (response) {
                var notify = jQuery("body").ExcellentableNotification({
                    title: AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.highlight.error"),
                    fadeout: false
                });
                notify.showErrorMsg();
                removeFailedInlineTable(tableId);
            });
        });
    });
    
});