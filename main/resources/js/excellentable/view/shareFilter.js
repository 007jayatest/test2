
Excellentable = window.Excellentable || {};
AJS.Excellentable = {};
Excellentable.Templates = {};
Excellentable.Templates.ShareFilter = {};
Excellentable.Templates.ShareFilter.Dialog = {};

Excellentable.Templates.ShareFilter.Dialog.shareFilterPopup = function(opt_data, opt_ignored) {
  return '<form action="#" method="post" class="aui eui-share-filter-popup"><div class="eui-field-group"><div class="autocomplete-user-target"><input class="text autocomplete-sharepage" id="euiUsers" data-max="10" data-dropdown-target=".autocomplete-user-target" data-none-message="' + soy.$$escapeHtml("No matches") + '" placeholder="' + soy.$$escapeHtml("User name, group or email") + '"/></div><ol class="eui-recipients"></ol></div><div class="eui-field-group"><textarea class="textarea" id="euiNote" placeholder="' + soy.$$escapeHtml("Add an optional note") + '"/></div><div class="eui-field-group eui-button-panel"><div class="eui-progress-messages-icon"></div><div class="eui-progress-messages"></div><input class="button submit" type="submit" value="' + soy.$$escapeHtml("Share") + '" disabled/><a class="eui-close-dialog" href="#">' + soy.$$escapeHtml("Cancel") + '</a></div></form>';
};

Excellentable.Templates.ShareFilter.Dialog.recipientEmail = function(opt_data, opt_ignored) {
  return '<li data-email="' + soy.$$escapeHtml(opt_data.email) + '" style="display: none" class="recipient-email"><img src="' + soy.$$escapeHtml(opt_data.icon) + '" title="' + soy.$$escapeHtml(opt_data.email) + '"/><span class="title" title="' + soy.$$escapeHtml(opt_data.email) + '">' + soy.$$escapeHtml(opt_data.email) + '</span><span class="remove-recipient"/></li>';
};
Excellentable.Templates.ShareFilter.Dialog.recipientUser = function(opt_data, opt_ignored) {
  return '<li data-userkey="' + soy.$$escapeHtml(opt_data.userKey) + '" style="display: none" class="recipient-user"><img src="' + soy.$$escapeHtml(opt_data.thumbnailLink.href) + '" title="' + soy.$$escapeHtml(opt_data.title) + '"/><span class="title" title="' + soy.$$escapeHtml(opt_data.title) + '">' + soy.$$escapeHtml(opt_data.title) + '</span><span class="remove-recipient"/></li>';
};
Excellentable.Templates.ShareFilter.Dialog.recipientGroup = function(opt_data, opt_ignored) {
  return '<li data-group="' + soy.$$escapeHtml(opt_data.title) + '" style="display: none" class="recipient-group"><span><img src="' + soy.$$escapeHtml(opt_data.thumbnailLink.href) + '" title="' + soy.$$escapeHtml(opt_data.title) + '"/><span>' + soy.$$escapeHtml(opt_data.title) + '</span><span class="remove-recipient"/></span></li>';
};


AJS.Excellentable.ShareFilter = {};

AJS.Excellentable.ShareFilter.autocompleteUser = function (scope) {
    scope = scope || document.body;
    var $ = AJS.$,
            emailExpression = /^([a-zA-Z0-9_\.\-\+\!#\$%&'\*/=\?\^_`{|}~])+\@.*/;

    var makeRestMatrixFromData = function (restObj) {
        if (!restObj || !restObj.result) {
            throw new Error("Invalid JSON format");
        }
        var matrix = [];
        for (var i = 0; i < restObj.result.length; i++) {
            var data = restObj.result[i];
            if (data.type == "group") {
                data = makeGroupObjectFromData(data);
            }
        }

        matrix.push(restObj.result);
        return matrix;
    };

    function makeGroupObjectFromData(data) {
        if (data.name == "confluence-users" ||
                data.name == "confluence-administrators") {
            return data;
        }
        data.title = data.name;
        data.group = data.name;
        data.thumbnailLink = {
            "href": Confluence.getContextPath() + "/download/resources/Addteq.Excellentable:share-filter-resources/group.png",
            "type": "image/png",
            "rel": "thumbnail"
        };
        data.link = [{
                "href": Confluence.getContextPath(),
                "rel": "self"
            }];
        return data;
    }

    $("input.autocomplete-sharefilter", scope).each(function () {
        var $this = $(this).attr("data-autocomplete-sharefilter-bound", "true")
                .attr("autocomplete", "off");
        var maxResults = $this.attr("data-max") || 10,
                alignment = $this.attr("data-alignment") || "left",
                dropDownTarget = $this.attr("data-dropdown-target"),
                dropDownPosition = null;

        if (dropDownTarget) {
            dropDownPosition = $(dropDownTarget);
        }
        else {
            dropDownPosition = $("<div></div>");
            $this.after(dropDownPosition);
        }
        dropDownPosition.addClass("aui-dd-parent autocomplete");

        $this.quicksearch(AJS.REST.getBaseUrl() + "search/user-or-group.json",
                function () {
                    $this.trigger("open.autocomplete-sharefilter");
                }, {
            makeParams: function (val) {
                return {
                    "max-results": maxResults,
                    query: val.replace("{|}", "") // remove curly braces as jersey doesn't like them
                };
            },
            dropdownPlacement: function (dd) {
                dropDownPosition.append(dd);
            },
            makeRestMatrixFromData: makeRestMatrixFromData,
            addDropdownData: function (matrix) {
                var trimmedEmail = $.trim($this.val());
                if (emailExpression.test(trimmedEmail)) {

                    matrix.push([{
                            name: trimmedEmail,
                            email: trimmedEmail,
                            href: "#",
                            icon: Confluence.getContextPath() + "/download/resources/Addteq.Excellentable:share-filter-resources/envelope.png"
                        }]);
                }

                if (!matrix.length) {
                    var noResults = $this.attr("data-none-message");
                    if (noResults) {
                        matrix.push([{
                                name: noResults,
                                className: "no-results",
                                href: "#"
                            }]);
                    }
                }

                return matrix;
            },
            ajsDropDownOptions: {
                alignment: alignment,
                displayHandler: function (obj) {
                    if (obj.restObj && obj.restObj.username) {
                        return obj.name + " (" + obj.restObj.username + ")";
                    }
                    return obj.name;
                },
                selectionHandler: function (e, selection) {

                    if (selection.find(".search-for").length) {
                        $this.trigger("selected.autocomplete-sharefilter", {searchFor: $this.val()});
                        return;
                    }
                    if (selection.find(".no-results").length) {
                        this.hide();
                        e.preventDefault();
                        return;
                    }

                    var contentProps = $("span:eq(0)", selection).data("properties");

                    if (!contentProps.email) {
                        contentProps = contentProps.restObj;
                    }

                    $this.trigger("selected.autocomplete-sharefilter", {content: contentProps});
                    this.hide();
                    e.preventDefault();
                }
            }
        });
    });

};

(function ($) {
    var dialogOptions = {
        hideCallback: hideCallback,
        width: 250,
        hideDelay: 36e5,
        // keep this in here until https://ecosystem.atlassian.net/browse/AUI-1323 is resolved and integrated with
        // Confluence
        calculatePositions: function (popup, targetPosition, mousePosition, opts) {
            var popupLeft;    //position of the left edge of popup box from the left of the screen
            var popupRight;   //position of the right edge of the popup box fron the right edge of the screen
            var popupTop;   //position of the top edge of popup box
            var arrowOffsetY = -7;    //the offsets of the arrow from the top edge of the popup, default is the height of the arrow above the popup
            var arrowOffsetX;
            var displayAbove;   //determines if popup should be displayed above the the trigger or not

            var targetOffset = targetPosition.target.offset();
            var triggerWidth = targetPosition.target.outerWidth(); //The total width of the trigger (including padding)
            var middleOfTrigger = targetOffset.left + triggerWidth / 2;    //The absolute x position of the middle of the Trigger
            var bottomOfViewablePage = (window.pageYOffset || document.documentElement.scrollTop) + $(window).height();


            //CONSTANTS
            var SCREEN_PADDING = 10; //determines how close to the edge the dialog needs to be before it is considered offscreen

            popupTop = targetOffset.top + targetPosition.target.outerHeight() + opts.offsetY;
            popupLeft = targetOffset.left + opts.offsetX;

            var enoughRoomAbove = targetOffset.top > popup.height();
            var enoughRoomBelow = (popupTop + popup.height()) < bottomOfViewablePage;

            //Check if the popup should be displayed above the trigger or not (if the user has set opts.onTop to true and if theres enough room)
            displayAbove = (!enoughRoomBelow && enoughRoomAbove) || (opts.onTop && enoughRoomAbove);

            //calculate if the popup will be offscreen
            var diff = $(window).width() - (popupLeft + opts.width + SCREEN_PADDING);

            //check if the popup should be displayed above or below the trigger
            if (displayAbove) {
                popupTop = targetOffset.top - popup.height() - 8; //calculate the flipped position of the popup (the 8 allows for room for the arrow)

                var shadowHeight = opts.displayShadow ?
                        (AJS.$.browser.msie ? 10 : 9) :
                        0;

                arrowOffsetY = popup.height() - shadowHeight;
            }
            arrowOffsetX = middleOfTrigger - popupLeft + opts.arrowOffsetX;

            //check if the popup should show up relative to the mouse
            if (opts.isRelativeToMouse) {
                if (diff < 0) {
                    popupRight = SCREEN_PADDING;
                    popupLeft = "auto";
                    arrowOffsetX = mousePosition.x - ($(window).width() - opts.width);
                } else {
                    popupLeft = mousePosition.x - 20;
                    popupRight = "auto";
                    arrowOffsetX = mousePosition.x - popupLeft;
                }
            } else {
                if (diff < 0) {
                    popupRight = SCREEN_PADDING;
                    popupLeft = "auto";
                    arrowOffsetX = middleOfTrigger - ($(window).width() - popup.outerWidth());
                } else if (opts.width <= triggerWidth / 2) {
                    arrowOffsetX = opts.width / 2;
                    popupLeft = middleOfTrigger - opts.width / 2;
                }
            }

            return {
                displayAbove: displayAbove,
                popupCss: {
                    left: popupLeft,
                    right: popupRight,
                    top: popupTop
                },
                arrowCss: {
                    position: "absolute",
                    left: arrowOffsetX,
                    right: "auto",
                    top: arrowOffsetY
                }
            }
        }
    };

    var hideCallback = function () {
        $(".dashboard-actions .explanation").hide();
    };

    var generatePopup = function ($contents, trigger, doShowPopup) {
        if ($contents.find("input").length) {
            doShowPopup();
            $contents.find(".autocomplete-sharefilter").val("");
            $contents.find('.aui-dropdown').remove();
            return
        }

        // load template
        var path = AJS.Meta.get("static-resource-url-prefix") + "/download/resources/Addteq.Excellentable:spreadJSResourcesV1/shareFilterDialog.html";
        $contents.append(Confluence.Templates.Excellentable.ShareFilter());
        AJS.Excellentable.ShareFilter.autocompleteUser();



            // hide popup hooks
            var doHidePopup = function (reset) {
                AJS.Excellentable.ShareFilter.current.hide();
                if (reset) {
                    // hiding the dialog is animated. we want to wait until the animation is over before
                    // we clear the content of the dialog
                    setTimeout(function () {
                        $contents.empty();
                    }, 300)
                }
                return false;
            };

            $(document).keyup(function (e) {
                if (e.keyCode == 27) {
                    doHidePopup(true);
                    $(document).unbind("keyup", arguments.callee);
                    return false;
                }
                return true;
            });

            $contents.find(".eui-close-dialog").click(function () {
                doHidePopup(true)
                return false;
            });

            $contents.find("#euiNote").elastic();

            $contents.find("form").submit(function () {

                var users = [];
                $contents.find(".eui-recipients li").each(function (index, item) {
                    users.push($(item).attr("data-userKey"))
                });

                if (users.length <= 0)
                    return false;

                $("button,input,textarea", this).attr("disabled", "disabled");

                $contents.find(".eui-progress-messages-icon").removeClass("error");
                $contents.find(".eui-progress-messages").text("Sending");
                $contents.find(".eui-progress-messages").attr("title", "Sending");
                var progressSpinner = Raphael.spinner($contents.find(".eui-progress-messages-icon")[0], 7, "#666");
                $contents.find(".eui-progress-messages-icon").css("position", "absolute").css("left", "0").css("margin-top", "3px");
                $contents.find(".eui-progress-messages").css("padding-left", $contents.find(".eui-progress-messages-icon").innerWidth() + 20);

                var users = [];
                $contents.find(".eui-recipients li[data-userKey]").each(function (index, item) {
                    users.push($(item).attr("data-userKey"))
                });

                var emails = [];
                $contents.find(".eui-recipients li[data-email]").each(function (index, item) {
                    emails.push($(item).attr("data-email"))
                });

                var groups = [];
                $contents.find(".eui-recipients li[data-group]").each(function (index, item) {
                    groups.push($(item).attr("data-group"))
                });

                var $note = $contents.find("#euiNote");
                
                 try{
                    var excellentableId = $contents.closest(".aui-inline-dialog").attr("excellentable-id");
                    var $div = jQuery(".eui-exc-container[excellentable-id="+excellentableId+"]");
                    spread= $div.find(".eui-view-spread").wijspread("spread"), sheet = spread.getActiveSheet();
                    var spreadJSON = spread.toJSON();            
                    var filterJSON = {};
                    filterJSON["filterVersion"] = 3; //Added support for share search with multisheet in third version of filter Refer:EXC-2513
                    filterJSON["activeSheetIndex"] = spread.getActiveSheetIndex();
                    var sheetCount = spread.getSheetCount();
                    for(var i= sheetCount-1 ; i >=0; i--){
                        var currentSheet = spread.getSheet(i),
                            sheetName = currentSheet.getName(),
                            sheetTag = currentSheet.tag(),
                            searchString = sheetTag && sheetTag.searchString || null,
                            tempJSON = {};
                        if(searchString != null){ /* Global Search is a Live search throughout the sheet*/
                            tempJSON["globalSearch"] = searchString;
                        }
                        
                        tempJSON["globalFilter"] = spreadJSON.sheets[sheetName].rowFilter; /* Global FIlter is a filter which is applied via Data Tab*/
                        
                        var sheetTables = currentSheet.getTables(), tableFilter = []; //For filter in the table designs.
                        if (sheetTables.length > 0){
                            tempJSON["tableFilter"] = {};
                            for (var j = 0; j < sheetTables.length; j++){
                                var currTable = sheetTables[j];
                                tempJSON["tableFilter"][currTable.name()] = currTable.rowFilter();
                            }
                        }
                        filterJSON[sheetName] = tempJSON;
                    }
                    filterJSON = JSON.stringify(filterJSON);
                    
                }catch(err){
                    console.log("Error"+err);
                    return false;
                }
                
                var request = {
                    users: users,
                    emails: emails,
                    groups: groups,
                    note: $note.hasClass("placeholded") ? "" : $note.val(),
                    contentEntityId: AJS.params.pageId,
                    contentType: AJS.params.contentType,
                    spaceKey : AJS.params.spaceKey,
                    id: excellentableId,
                    filterString: filterJSON
                };

                $.ajax({
                    type: "POST",
                    contentType: "application/json; charset=utf-8",
                    url: Confluence.getContextPath() + "/rest/excellentable/1.0/share/filter",
                    data: JSON.stringify(request),
                    dataType: "text",
                    success: function () {
                        // remove spinner
                        setTimeout(function () {
                            progressSpinner();

                            $contents.find(".eui-progress-messages-icon").addClass("aui-icon aui-icon-success");
                            $contents.find(".eui-progress-messages").text("Sent");
                            $contents.find(".eui-progress-messages").attr("title", "Sent");
                            setTimeout(function () {
                                $contents.find(".eui-progress-messages").text("");
                                $contents.find(".eui-progress-messages-icon").removeClass("aui-icon aui-icon-success");
                                $contents.find("#euiNote").val("");
                                $("button,input,textarea", $contents).removeAttr("disabled");
                                doHidePopup(false);
                            }, 1000);
                        }, 500);
                    },
                    error: function (data, status) {
                        progressSpinner();

                        $contents.find(".eui-progress-messages-icon").addClass("aui-icon aui-icon-error");
                        $contents.find(".eui-progress-messages").text("Error sending");
                        $contents.find(".eui-progress-messages").attr("title", "Error sending" + ": " + status);

                        $("button,input,textarea", $contents).removeAttr("disabled");
                    }
                });

                return false;
            });

            var $input = $contents.find("#euiUsers");
            var $shareButton = $contents.find("input.submit");

            $input.bind("selected.autocomplete-sharefilter", function (e, data) {
                var addItem = function (type, template, data) {
                    var $recipients = $contents.find(".eui-recipients"),
                            recipientSelector,
                            $item;

                    recipientSelector = "li[data-" + type + "=\"" + data.content[type] + "\"]";
                    if ($recipients.find(recipientSelector).length > 0) {
                        $recipients.find(recipientSelector).hide();
                    } else {
                        $recipients.append(template(data.content));
                    }
                    $item = $recipients.find(recipientSelector);
                    $item.find(".remove-recipient").click(function () {
                        $item.remove();
                        if ($recipients.find("li").length == 0) {
                            $shareButton.attr("disabled", "true");
                        }
                        AJS.Excellentable.ShareFilter.current.refresh();
                        $input.focus();
                        return false;
                    });
                    $item.fadeIn(200);
                };

                if (data.content.email) {
                    addItem("email", Excellentable.Templates.ShareFilter.Dialog.recipientEmail, data);
                } else if (data.content.type == "group") {
                    addItem("group", Excellentable.Templates.ShareFilter.Dialog.recipientGroup, data);
                } else {
                    addItem("userKey", Excellentable.Templates.ShareFilter.Dialog.recipientUser, data);
                }

                AJS.Excellentable.ShareFilter.current.refresh();
                $shareButton.removeAttr("disabled");
                $input.val("");
                $input.focus();

                return false;
            });

            $input.bind("open.autocomplete-sharefilter", function (e, data) {
                if ($("a:not(.no-results)", AJS.dropDown.current.links).length > 0)
                    AJS.dropDown.current.moveDown();
            });

            $input.keypress(function (e) {
                return e.keyCode != 13;
            });

            $(document).bind("showLayer", function (e, type, dialog) {
                if (type == "inlineDialog" && dialog.popup == AJS.Excellentable.ShareFilter.current) {
                    dialog.popup.find("#euiUsers").focus();
                }
            });

            doShowPopup();
    };

    /*
     * CONF-28333: The documentation theme's content container is positioned absolute, causing the dialog to behave like
     * being fixed if attached to the body. Thus the dialog needs to be a child of that container in order to not behave
     * like being in a fixed position if the content is scrolled.
     */
    var attachToContentContainerIfInDocumentationTheme = function (dialogOptions) {
        var documentationThemeContentContainer = $("#splitter-content");
        if (documentationThemeContentContainer.length !== 0) {
            dialogOptions.container = documentationThemeContentContainer;
            dialogOptions.offsetY = AJS.InlineDialog.opts.offsetY - documentationThemeContentContainer.offset().top;
        }
        return dialogOptions;
    };

    AJS.Excellentable.ShareFilter.initDialog = function (selector, identifier, options) {
        if (selector.length) {
            var opts = $.extend(false, attachToContentContainerIfInDocumentationTheme(dialogOptions), options);
            AJS.Excellentable.ShareFilter.current = AJS.InlineDialog(selector, identifier, generatePopup, opts);
        }
    };


})(AJS.$);

(function($){
	jQuery.fn.extend({
		elastic: function() {

			//	We will create a div clone of the textarea
			//	by copying these attributes from the textarea to the div.
			var mimics = [
				'paddingTop',
				'paddingRight',
				'paddingBottom',
				'paddingLeft',
				'fontSize',
				'lineHeight',
				'fontFamily',
				'width',
				'fontWeight',
				'border-top-width',
				'border-right-width',
				'border-bottom-width',
				'border-left-width',
				'borderTopStyle',
				'borderTopColor',
				'borderRightStyle',
				'borderRightColor',
				'borderBottomStyle',
				'borderBottomColor',
				'borderLeftStyle',
				'borderLeftColor'
				];

			return this.each( function() {

				// Elastic only works on textareas
				if ( this.type !== 'textarea' ) {
					return false;
				}

			var $textarea	= jQuery(this),
				$twin		= jQuery('<div />').css({
					'position'		: 'absolute',
					'display'		: 'none',
					'word-wrap'		: 'break-word',
					'white-space'	:'pre-wrap'
				}),
				lineHeight	= parseInt($textarea.css('line-height'),10) || parseInt($textarea.css('font-size'),'10'),
				minheight	= parseInt($textarea.css('height'),10) || lineHeight*3,
				maxheight	= parseInt($textarea.css('max-height'),10) || Number.MAX_VALUE,
				goalheight	= 0;

				// Opera returns max-height of -1 if not set
				if (maxheight < 0) { maxheight = Number.MAX_VALUE; }

				// Append the twin to the DOM
				// We are going to measure the height of this, not the textarea.
				$twin.appendTo($textarea.parent());

				// Copy the essential styles (mimics) from the textarea to the twin
				var i = mimics.length;
				while(i--){
					$twin.css(mimics[i].toString(),$textarea.css(mimics[i].toString()));
				}

				// Updates the width of the twin. (solution for textareas with widths in percent)
				function setTwinWidth(){
					var curatedWidth = Math.floor(parseInt($textarea.width(),10));
					if($twin.width() !== curatedWidth){
						$twin.css({'width': curatedWidth + 'px'});

						// Update height of textarea
						update(true);
					}
				}

				// Sets a given height and overflow state on the textarea
				function setHeightAndOverflow(height, overflow){

					var curratedHeight = Math.floor(parseInt(height,10));
					if($textarea.height() !== curratedHeight){
						$textarea.css({'height': curratedHeight + 'px','overflow':overflow});
					}
				}

				// This function will update the height of the textarea if necessary
				function update(forced) {

					// Get curated content from the textarea.
					var textareaContent = $textarea.val().replace(/&/g,'&amp;').replace(/ {2}/g, '&nbsp;').replace(/<|>/g, '&gt;').replace(/\n/g, '<br />');

					// Compare curated content with curated twin.
					var twinContent = $twin.html().replace(/<br>/ig,'<br />');

					if(forced || textareaContent+'&nbsp;' !== twinContent){

						// Add an extra white space so new rows are added when you are at the end of a row.
						$twin.html(textareaContent+'&nbsp;');

						// Change textarea height if twin plus the height of one line differs more than 3 pixel from textarea height
						if(Math.abs($twin.height() + lineHeight - $textarea.height()) > 3){

							var goalheight = $twin.height()+lineHeight;
							if(goalheight >= maxheight) {
								setHeightAndOverflow(maxheight,'auto');
							} else if(goalheight <= minheight) {
								setHeightAndOverflow(minheight,'hidden');
							} else {
								setHeightAndOverflow(goalheight,'hidden');
							}
						}
					}
				}

				// Hide scrollbars
				$textarea.css({'overflow':'hidden'});

				// Update textarea size on keyup, change, cut and paste
				$textarea.bind('keyup change cut paste', function(){
					update();
				});

				// Update width of twin if browser or textarea is resized (solution for textareas with widths in percent)
				$(window).bind('resize', setTwinWidth);
				$textarea.bind('resize', setTwinWidth);
				$textarea.bind('update', update);

				// Compact textarea on blur
				/* JML (Atlassian): Commented out because on most browers this causes an extra
				 * refresh on the container and problems with form submissions as the button
				 * has to be clicked twice.
				$textarea.bind('blur',function(){
					if($twin.height() < maxheight){
						if($twin.height() > minheight) {
							$textarea.height($twin.height());
						} else {
							$textarea.height(minheight);
						}
					}
				});
				*/
				// And this line is to catch the browser paste event
				$textarea.bind('input paste',function(e){ setTimeout( update, 250); });

				// Run update once when elastic is initialized
				update();

			});

        }
    });
})(AJS.$);
