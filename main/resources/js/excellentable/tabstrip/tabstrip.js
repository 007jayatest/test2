(function ($) {
	$.fn.ExcellentableTabStrip = function(options, spread){
		$.fn.ExcellentableTabStrip.defaults = {
			tabstripId: '#eui-tab-strip',
			tabstripMenuId: '.eui-tabs-menu',
			tabstripDDId: '.eui-tab-strip-dd',
			tabstripDDList:'.eui-menu-list',
			navClass: 'eui-tab-nav',
			tabClass: '.menu-item',
			scrollbarClass: '.eui-tab-strip-scrollbar',
			spreadScrollClass: '.scroll-container',
			activeTabClass: 'active-tab',
			renameDivClass: '.eui-rename-tab',
			renameDivInput: '.eui-rename-tab input',
			placeholder : 'sortable-placeholder',
			btnAddSheetId : '#euiTabstripAddSheet',
			btnOptionsId : '#euiTabstripOptions',
			btnScrollLeftId: '#euiScrollLeft',
			btnScrollRightId: '#euiScrollRight',
			clickScrollId: '.eui-scrolloptions',
			tabsContainerId: '.eui-tab-strip-tabs',
			tabContextMenuId: '#eui-tab-context-menu',
			protectedClass: 'protected',
			protectedCellClass: 'protected-cell',
			tabContextMenuTriggerId: '#eui-tab-context-menu-trigger',
			initialEditOpenScrollClassCount: 1,
			reopenEditScrollClassCount: 3,
			quickEditScrollClassCount: 5,
			sourceTabIndex: -1,
			targetTabIndex: -1,
			sheetNameMaxLength: 24,
			protectMessage : AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.context.menu.protect"),
		    unProtectMessage : AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.context.menu.unprotect"),
		    newSheetToolTipText : AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.add.newsheet"),
			sheetTemplate: '<li class="menu-item animated fadeInRight" data-id="{{sheetId}}" data-name="{{sheetName}}">\
								<div class="menu-item-container">\
                					<p><span class="icon-placeholder aui-icon aui-icon-small aui-iconfont-locked"></span>{{sheetName}}</p>\
                				</div>\
            				</li>',
            renameTemplate: '<div class="eui-rename-tab" data-id="{{sheetId}}" data-name="{{sheetName}}" style="width:{{width}}px;"><input type="text" style="height:{{height}}px;" /></div>'
		};

		options = $.extend({}, $.fn.ExcellentableTabStrip.defaults, options);

		var detectGlobalNotifications = function() {
			var msg = jQuery(".eui-aui-msg");
			var isGlobal = msg.attr('data-global');
			if (msg.length !== 0 && isGlobal === 'false') {
				jQuery("body").ExcellentableNotification().removeMsg();
			}
			
		}

		var triggerUnsavedContentManager = function() {
			//this is done to trick the unsavedcontentmanager that a save should occur
			spread._trigger(GcSpread.Sheets.Events.SheetNameChanged,{
				sheet: spread.sheets[0],
				oldValue: spread.sheets[0]._name,
				newValue: spread.sheets[0]._name
			});
		}

		var initializeUnfocusedList = function(navType, elem){
			var activeId = $(elem).attr('data-id');
			spread.sheets.forEach(function(sheet) {
				var sheetTab = options.sheetTemplate.replace(/{{sheetName}}/ig, sheet._name).replace(/{{sheetId}}/ig, sheet._id);
				
				if(navType === 'tabs'){
					$(options.tabstripMenuId).append(sheetTab);
				}

				if(navType === 'menu'){				
					$(options.tabstripDDId).find('.menu-list').prepend(sheetTab);
				}

				$(options.tabClass).removeClass('animated fadeInRight');

				if(parseInt(activeId) === sheet._id){
					var $sheetId = options.tabClass + '[data-id="' + sheet._id + '"]';
					$(options.tabstripDDId).find($sheetId).addClass(options.activeTabClass);
				}	
			});
		}

		var updateOrder = function(navType, elem){
			var selector = options.tabstripId + ' .' + options.navClass + '[data-nav-type="' + navType + '"]';
			$(selector).find('li.menu-item').remove();

			initializeUnfocusedList(navType, elem);
			ExcellentableTriggerCustomValueChangeEvent(spread,'SheetOrderChanged');
		}

		var removeAnimationClass = function(){
			$(options.tabClass).removeClass('animated fadeInRight');
		}

		var loadScrollbar = function() {
			var spreadScrollbar = $(options.spreadScrollClass);

			if(spreadScrollbar.length > options.quickEditScrollClassCount){
				spreadScrollbar = $(options.spreadScrollClass).eq(options.quickEditScrollClassCount);
			}else if(spreadScrollbar.length > options.reopenEditScrollClassCount){
				spreadScrollbar = $(options.spreadScrollClass).eq(options.reopenEditScrollClassCount);
			}else {
				spreadScrollbar = $(options.spreadScrollClass).eq(options.initialEditOpenScrollClassCount);
			}

			$(options.scrollbarClass).html(spreadScrollbar);
		}

		var setActiveTab = function(sheetName, sheetId) {
			spread.setActiveSheet(sheetName);
			var $sheetId = options.tabClass + '[data-id="' + sheetId + '"]';

			$(options.tabstripMenuId).find(options.tabClass).removeClass(options.activeTabClass);
			$(options.tabstripMenuId).find($sheetId).addClass(options.activeTabClass);

			$(options.tabstripDDId).find(options.tabClass).removeClass(options.activeTabClass);
			$(options.tabstripDDId).find($sheetId).addClass(options.activeTabClass);

			loadScrollbar();
			detectGlobalNotifications();
		}

		var changeSheetIndex = function(oldPos, newPos) {
			while (oldPos < 0) {
			    oldPos += spread.sheets.length;
			}
			while (newPos < 0) {
			    newPos += spread.sheets.length;
			}
			if (newPos >= spread.sheets.length) {
			    var k = newPos - spread.sheets.length;
			    while ((k--) + 1) {
			        spread.sheets.push(undefined);
			    }
			}
			spread.sheets.splice(newPos, 0, spread.sheets.splice(oldPos, 1)[0]); 

			triggerUnsavedContentManager();
		}

		var sortableStart = function(event, ui){
			ui.item.toggleClass(options.placeholder);
			removeAnimationClass();
			
			options.sourceTabIndex = $('.eui-tabs-menu .menu-item').index(ui.item);

            setActiveTab(ui.item.attr('data-name'), ui.item.attr('data-id'));
            spread.allowSheetReorder(true);
            options.oldindex = ui.item.index(options.tabClass) - 1;
		}

		var sortableStop = function(event, ui) {
			ui.item.toggleClass(options.placeholder);

			options.targetTabIndex = $('.eui-tabs-menu .menu-item').index(ui.item);

            changeSheetIndex(options.sourceTabIndex, options.targetTabIndex);

            if($(ui.item).parent().attr('data-nav-type') === 'tabs'){
            	var type = 'menu';
            }
            if($(ui.item).parent().attr('data-nav-type') === 'menu'){
            	var type = 'tabs';
            }

            updateOrder(type, ui.item);
		}

		var initiateSortableTabs = function(){
			$(options.tabstripId).find(options.tabstripMenuId).sortable({
				axis: 'x',
		        placeholder: options.placeholder,
		        start: sortableStart,
                stop: sortableStop
		    });
		}

		var scrollTabs = function(direction) {
			var scroll = direction === 'left' ? '-=80' : '+=80';
			$(options.tabstripMenuId).animate({scrollLeft: scroll });
		}

		/**
		 * This is a helper method to extract the Node value from an html element without taking into consideration
		 * the subelements within it. This is currently used adjustProtectedLockAndSheetTabColorInContextMenu method to adjust the value of the
		 * anchor tag which has a span tag inside it.
		 *
		 * @param $elem
		 * @param text
		 */
		var setTextContents = function($elem, text) {
		    $elem.contents().filter(function() {
		        if (this.nodeType === Node.TEXT_NODE) {
		            this.nodeValue = text;
		        }
		    });
		}

		/**
		 * Adjusts the protected/vs unprotected lock in the context menu to be shown to the user,
		 * depending on whether is sheet is protected or not. If the sheet is protected, it will show the option to unprotect
		 * and vice versa.
		 *
		 * @param SHEET_TAB_MENU
		 * @param isprotected
		 */
		var adjustProtectedLockAndSheetTabColorInContextMenu = function(isProtected, hasProtectedCell, $elem) {
		    var protectedSpan = $(options.tabContextMenuId).find("aui-item-link[data-name='protectsheet'] > a > span");
		    var protectedAnchor = $(options.tabContextMenuId).find("aui-item-link[data-name='protectsheet'] > a");
		    
		    if (isProtected) {
		    	//Add lock icon and set text to Protect
		        protectedSpan.removeClass("aui-iconfont-unlocked");
		        protectedSpan.addClass("aui-iconfont-locked");
		        setTextContents(protectedAnchor, " " + options.unProtectMessage);
		        $elem.addClass(options.protectedClass);
		        $elem.removeClass(options.protectedCellClass);
		    }else if(hasProtectedCell > 0){
		    	protectedSpan.addClass("aui-iconfont-locked");
		        protectedSpan.removeClass("aui-iconfont-unlocked");
		        setTextContents(protectedAnchor, " " + options.protectMessage);
		        $elem.removeClass(options.protectedClass);
		        $elem.addClass(options.protectedCellClass);
		    }else {
	    		// Remove unlock icon and set text to Unprotect
		        protectedSpan.removeClass("aui-iconfont-locked");
		        protectedSpan.addClass("aui-iconfont-unlocked");
		        setTextContents(protectedAnchor, " " + options.protectMessage);
		        $elem.removeClass(options.protectedCellClass);
		        $elem.removeClass(options.protectedClass);
	    	}
		}

		var setProtections = function(sheet) {
			var isProtected = sheet.getIsProtected();
			var hasProtectedCell = sheetHasProtectedCell(sheet);
			var sheetId = options.tabClass + '[data-id="' + sheet._id + '"]';
			var tab = $(options.tabstripMenuId).find(sheetId);

			adjustProtectedLockAndSheetTabColorInContextMenu(isProtected, hasProtectedCell, tab);
		}

		//initialize the tabs with the name 
		var initTabs = function(sheet) {
			var sheetTab = options.sheetTemplate.replace(/{{sheetName}}/ig, sheet._name).replace(/{{sheetId}}/ig, sheet._id);
			$(options.tabstripMenuId).append(sheetTab);

			setProtections(sheet);

			//init menu
			$(options.tabstripDDId).find('.menu-list').prepend(sheetTab);

			initiateSortableTabs();

		    $(options.tabstripId).find(options.tabstripMenuId).disableSelection();
		    removeAnimationClass();
		};

		var checkIfTabsShouldScroll = function() {
			if($(options.tabstripMenuId).length > 0){
				if($(options.tabstripMenuId)[0].scrollWidth > $(options.tabstripMenuId).innerWidth()){
					$(options.clickScrollId).removeClass('hidden');
					$(options.tabsContainerId).addClass('overflow');
				}else {
					$(options.clickScrollId).addClass('hidden');
					$(options.tabsContainerId).removeClass('overflow');
				}
			}
		}

		var loadTabs = function() {
			$(options.tabstripMenuId).find('li.menu-item').remove();
			$(options.tabstripDDId).find('li.menu-item').remove();
			spread.sheets.forEach(function(sheet) {
				initTabs(sheet);
			});

			var activeSheet = spread.getActiveSheet();
			setActiveTab(activeSheet._name, activeSheet._id);
		};

		var removeScrollbar = function() {
			$(options.scrollbarClass).remove();
		}

		var initBlankSheet = function(sheet) {
            sheet.isPaintSuspended(true);
            sheet.setRowCount(options.defaultRowCount);
            sheet.setColumnCount(options.defaultColumnCount);
            sheet.selectionBackColor("transparent");
            sheet.defaults.rowHeight = options.defaultRowHeight;
            sheet.defaults.colWidth = options.defaultColWidth;
            sheet.allowCellOverflow(true);
            sheet.getColumns(0, sheet.getColumnCount()).textIndent(1).vAlign($.wijmo.wijspread.VerticalAlign.center);
            sheet.setRowHeight(0, sheet.defaults.rowHeight, $.wijmo.wijspread.SheetArea.colHeader);
            sheet.getColumn(0, $.wijmo.wijspread.SheetArea.rowHeader).font(options.defaultFontSize+options.defaultFontFamily);
            sheet.getRow(0, $.wijmo.wijspread.SheetArea.colHeader).font(options.defaultFontSize+options.defaultFontFamily);
            //set default font style as Verdana
            defaultStyle =  new $.wijmo.wijspread.Style();
            defaultStyle.font = options.defaultTabFontSize+options.defaultFontFamily;
            sheet.setDefaultStyle(defaultStyle, $.wijmo.wijspread.SheetArea.viewport);
            sheet.isPaintSuspended(false);
		}

		//adds new sheet to spread
		var addNewSheet = function() {
			var sheetName = 'Sheet' + (spread.sheets.length + 1);
			var sheet = new GcSpread.Sheets.Sheet();

			if(spread.getSheetFromName(sheetName)){
				sheetName = sheetName + '_1';
			}
			sheet.setName(sheetName);
			spread.addSheet(spread.sheets.length + 1, sheet);
			var sheetTab = options.sheetTemplate.replace(/{{sheetName}}/ig, sheetName).replace(/{{sheetId}}/ig, sheet._id);
			$(options.tabstripMenuId).append(sheetTab);
			$(options.tabstripDDId).find('.menu-list').prepend(sheetTab);

			initBlankSheet(sheet);
			ExcellentableTriggerCustomValueChangeEvent(spread,'AddNewSheet');
			checkIfTabsShouldScroll();
		};

		var showTabContextMenu = function($elem) {
			var sheetName = $elem.attr('data-name');
			var sheetId = $elem.attr('data-id');

			setActiveTab(sheetName, sheetId);

			$(options.tabContextMenuTriggerId).trigger("click");
			$(options.tabContextMenuId).css({"left": $elem.offset().left + 'px'});

			var $deleteSheetItem = $(options.tabContextMenuId).find("aui-item-link[data-name='deletesheet']");
            //Do not give user option to delete a sheet when spread contains only one sheet
            if(spread.getSheetCount() === 1){
                $deleteSheetItem.addClass('hidden');
            }
            else {
                $deleteSheetItem.removeClass('hidden aui-dropdown2-hidden').find('a').removeAttr('aria-disabled');
            }

			var sheet = spread.getSheetFromName(sheetName);
			var isprotected = sheet.getIsProtected();
			var hasProtectedCell = sheetHasProtectedCell(sheet);

			adjustProtectedLockAndSheetTabColorInContextMenu(isprotected, hasProtectedCell, $elem);
		}

		var isValidSheetName = function(sheetName) {
            if (sheetName === undefined || sheetName === null || sheetName === ""){
                return false
            }
            var currentChar;
            for (var i = 0; i < sheetName.length; i++){
                currentChar = sheetName.charAt(i);
                if (currentChar === '*' || currentChar === ':' || currentChar === '[' || currentChar === ']' || currentChar === '?' || currentChar === '\\' || currentChar === '/'){
                    return false
                }
            }
            var length = spread.sheets.length;
            for (var i = 0; i < length; i++){
                var sheet = spread.sheets[i];
                if (sheetName === sheet._name){
                    return false
                }
            }
            return true
        }


		var renameTab = function(sheetName, sheetId, value) {
			if(value.length > options.sheetNameMaxLength){
				jQuery("body").ExcellentableNotification({
		            body: AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.sheetname.validation.warning")
		            }).showWarningMsg();
				$(options.renameDivClass).remove();
			}else if(isValidSheetName(value)){
				var tab = options.tabClass + '[data-id="' + sheetId + '"]';
				var renameSheet = spread.getSheetFromName(sheetName);
				renameSheet.setName(value);
				$(options.tabstripMenuId).find(tab).find('p').html(value);
				$(options.tabstripMenuId).find(tab).attr('data-name', value);

				$(options.tabstripDDId).find(tab).find('p').html(value);
				$(options.tabstripDDId).find(tab).attr('data-name', value);

				$(options.renameDivClass).remove();
				triggerUnsavedContentManager();
				ExcellentableTriggerCustomValueChangeEvent(spread,'SheetNameChanged');
			}else {
				$(options.renameDivClass).remove();
			}
			
		}

		var deleteSheet = function() {
			//If sheet is NOT protected, only remove it then
            if (!protectedCheck(spread.getActiveSheet())) {
                var sheet_delete_warning_dialog = AJS.dialog2("#sheet-delete-warning-dialog");

                //Showing sheet delete warning dialog
                sheet_delete_warning_dialog.show();
            }
		}

		var renameSheet = function() {
			$(options.tabstripMenuId).find('.active-tab').trigger('dblclick');
		}

		var protectSheet = function() {
			var activeSheet = spread.getActiveSheet();
			var isprotected = !activeSheet.getIsProtected();
			var hasProtectedCell = sheetHasProtectedCell(activeSheet);
			activeSheet.setIsProtected(isprotected);
			var tab = $(options.tabstripMenuId).find('.active-tab');

		    adjustProtectedLockAndSheetTabColorInContextMenu(isprotected, hasProtectedCell, tab);
			ExcellentableTriggerCustomValueChangeEvent(spread,'ProtectSheet');
		}

		var performContextMenuAction = function(action) {
			$(options.tabContextMenuTriggerId).trigger("click");
			switch(action){
				case 'remove':
					deleteSheet();
				break;
				case 'rename':
					renameSheet();
				break;
				case 'protect':
					protectSheet();
				break;
				default :
				break;
			}
		}

		//Binds all tab strip events
		var bindEvents = function(){
			//unbind all events due to issue with Version History restore causing multiple event binds
			jQuery(options.btnAddSheetId).off();
			jQuery(options.btnOptionsId).off();
			jQuery(options.btnScrollLeftId).off();
			jQuery(options.btnScrollRightId).off();
			jQuery(options.tabstripId).off('dblclick');
			jQuery(options.tabstripMenuId).off('contextmenu');
			jQuery(options.tabContextMenuId).off();
			
			if(options.edit.allowAddNewSheet){
				jQuery(options.btnAddSheetId).on('click', function() {
					addNewSheet();
				});	
			}
			
			jQuery(options.btnOptionsId).on('click', function() {
				removeAnimationClass();
				$(options.tabstripDDId).toggleClass('hidden');
			});

			jQuery(options.btnScrollLeftId).on('click', function() {
				scrollTabs('left');
			});

			jQuery(options.btnScrollRightId).on('click', function() {
				scrollTabs('right');
			});

			jQuery(options.tabstripId).on('click', options.tabClass, function() {
				setActiveTab($(this).attr('data-name'), $(this).attr('data-id'));
			});

			jQuery(options.tabstripMenuId).on('contextmenu', options.tabClass, function(ev) {
				ev.preventDefault();
				showTabContextMenu($(this));	
			});

			jQuery(options.tabstripId).on('dblclick', options.tabClass, function() {
				if(!$(options.renameDivClass).is(":visible")){
					var height = $(this).height() - 6;
					var width = $(this).width();
					var sheetId = $(this).attr('data-id');
					var sheetName = $(this).attr('data-name');
					var renameDiv = options.renameTemplate.replace(/{{sheetName}}/ig, sheetName).replace(/{{sheetId}}/ig, sheetId).replace(/{{width}}/ig, width).replace(/{{height}}/ig, height);
					$(this).append(renameDiv);
					$(options.renameDivInput).focus();
				}else {
					$(options.renameDivClass).remove();
				}
			});

			jQuery(options.tabstripId).on('keydown', options.renameDivInput, function(ev) {
				if(ev.key === 'Escape'){$(options.renameDivClass).remove();}
				if(ev.key === 'Tab'){ev.preventDefault();}
				if(ev.key === 'Enter' || ev.key === 'Tab'){
					renameTab($(this).parent().attr('data-name'), $(this).parent().attr('data-id'),$(this).val());
				}
			});

			jQuery(document).on('click', function() {
				if($(options.renameDivClass).is(":visible")){
					var parentTab = $(options.renameDivInput).parent();
					renameTab(parentTab.attr('data-name'), parentTab.attr('data-id'), $(options.renameDivInput).val());
				}
			});

			jQuery(window).on('resize', function() {
				checkIfTabsShouldScroll();
			});

			jQuery(options.tabContextMenuId).on('click',"aui-item-link", function(){
				var action = $(this).attr("data-action");
				performContextMenuAction(action);
			});

			jQuery(options.btnAddSheetId).tooltip({gravity : "s"});
			jQuery(options.btnOptionsId).tooltip({gravity : "s"});
			    
		};

		this.init = function() {
		    loadTabs();

		    if(options.type === 'edit'){
		    	bindEvents();
		    	loadScrollbar();
		    }else {
		    	removeScrollbar();
		    }
		};

		$.fn.ExcellentableTabStrip.update = function() {
			loadTabs();
		}

		this.init();
		return this;

	}
})(jQuery);