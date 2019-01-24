"use strict";
AJS.toInit(function () {
    // Show Excellentable icon on all the attachments under attachment screen
    if (window.location.href.indexOf("viewpageattachments.action") > -1) {
        // view attachment screen
        var attachments1 = jQuery("#view-attachments td>span.icon-file-excel, #view-attachments td>span.content-type-attachment-excel");
        attachments1.parent().append('<button title="Open using Excellentable" class="openExcellentable aui-icon aui-icon-small aui-iconfont-search">Open using Excellentable</button>');
        attachments1.removeClass().addClass("aui-icon eui-attachment-icon").prop('title', "Open using Excellentable");
        attachments1.wrap('<a href="#" class="eui-attachment-preview"></a>');

    }
    // Show Excellentable icon on attachments under attachment macro
    setTimeout(function () {
        // attachment macro page screen for Confluence 6.7.x and above
        var attachments2 = jQuery("div.plugin_attachments_container span.content-type-attachment-excel");
        attachments2.parent().append('<button title="Open using Excellentable" class="openExcellentable aui-icon aui-icon-small aui-iconfont-search">Open using Excellentable</button>');
        attachments2.removeClass().addClass("aui-icon eui-attachment-icon hide-icons").prop('title', "Open using Excellentable");
        attachments2.wrap('<a href="#" class="eui-attachment-preview"></a>');

        // attachment macro page screen for Confluence below 6.7
        var attachments3 = jQuery("div.plugin_attachments_table_container td.filename-column > span.icon-file-excel");
        attachments3.parent().append('<button title="Open using Excellentable" class="openExcellentable aui-icon aui-icon-small aui-iconfont-search">Open using Excellentable</button>');
        attachments3.removeClass().addClass("aui-icon eui-attachment-icon hide-icons").prop('title', "Open using Excellentable");
        attachments3.wrap('<a href="#" class="eui-attachment-preview"></a>');
    }, 500);

    var Excellentable = window.Excellentable || {};
    /**
     * Constructor : Retrieves dialog from templates
     * @type {Function}
     */
    var AttachmentPreview = Excellentable.AttachmentPreview = Excellentable.AttachmentPreview || function (options) {
        var defaults = {
            protectionOption: {
                allowFilter: true,
                allowSort: true,
                allowResizeRows: true,
                allowResizeColumns: true,
                allowEditObjects: false
            },
            attachmentPreviewElement: "euiAttachmentSpread",
            contentEntityId: AJS.params.pageId
        },
            width = window.outerWidth,
            height = window.outerHeight -130;
        this.options = $.extend({}, defaults, options);
        var excellentableDialogHtml = Confluence.Templates.Excellentable.AttachmentPreview({
                excellentableDialogId: this.options.attachmentPreviewElement,
                contentEntityId: this.options.contentEntityId,
                width: width,
                height: height
            });
        this.dialog = jQuery(excellentableDialogHtml);
        var $spread = this.dialog;
        if (typeof $spread.wijspread("spread") === "undefined") {
            $spread.wijspread();
        }
        this.spread = $spread.wijspread("spread");
    };
    /**
     * Binds all the events related to view excellentable dialog
     */
    AttachmentPreview.prototype.bindEvents = function () {
        var $self = this;
        
        AJS.dialog2("#" + $self.options.attachmentPreviewElement).on("show", function() {
        	$("#" + $self.options.attachmentPreviewElement).addClass("disable-attachment-uploader");
        });
        
        AJS.dialog2("#" + $self.options.attachmentPreviewElement).on("hide", function() {
        	$("#" + $self.options.attachmentPreviewElement).removeClass("disable-attachment-uploader");
        });
  
        //Click on Excellentable Image to open dialog event
        jQuery("body").on("click", "button.openExcellentable", function (e) {
            //stop the attachment row from expanding and collapsing on click magnifying icon
            var attachmentId = this.parentElement.parentElement.getAttribute('data-attachment-id');
            jQuery('.attachment-summary[data-attachment-id="'+attachmentId+'"]').toggleClass("hidden");
            $.fn.Excellentable.BouncyBallSpinner().show("body", false,
                AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spinner.import"));
            var width = window.outerWidth,
                height = window.outerHeight -130;
            $self.dialog.css({
                width: width,
                height: height
            });
            var tr = jQuery(this).closest("tr");
            attachmentId = tr.attr("id").split("-")[1];
            var fileName = tr.attr("data-attachment-filename") || jQuery(this).siblings('a.filename').attr('data-filename');
            $self.dialog.find("#euiDownload").attr("href",jQuery(this).siblings('a.filename').attr('href'));
            console.log("attachmentId: " + attachmentId);
            var settings = {
                "url": AJS.params.baseUrl + "/rest/excellentable/1.0/attachment/render/" + attachmentId,
                "type": "GET",
                "processData": false,
                contentType: "application/json",
                beforeSend: function (xhr) {
                    xhr.setRequestHeader("X-Atlassian-Token", "no-check");
                }
            };
            jQuery.ajax(settings).done(function (response) {
                if (typeof response === "undefined" || response == null) {
                    //Import Failed
                    return;
                }
                AJS.dialog2("#" + $self.options.attachmentPreviewElement).show();
                var viewErrorImportDialog = false,
                    overWrite = true;
                var import1 = Excellentable.import1 = new Excellentable.Import({name:fileName}, overWrite,$self.spread, viewErrorImportDialog);
                import1.importData(response);
                $self._setSpreadProperties();
            }).fail(function(err) {
                $.fn.Excellentable.BouncyBallSpinner().hide();
                jQuery("body").ExcellentableNotification({
                    title: AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.import.formatType.error.title"),
                }).showErrorMsg();
            });
        });
        //Click on close button in dialog
        this.dialog.find(".eui-close").on('click', function () {
            AJS.dialog2("#" + $self.options.attachmentPreviewElement).hide();
        });
        //Click on create new page button
        this.dialog.find("#euiCreateNewPage").on('click', function () {
            jQuery("body").ExcellentableNotification({title:"Coming Soon..."}).showGenericMsg();
            // 0 is the identification number in google sheets
            if (typeof $self._createNewCall === "undefined") {
                $self._restCall("https://hooks.zapier.com/hooks/catch/28910/zpvsyn/", "POST", {"id": 0});
                $self._createNewCall = true;
            }

        });
        //Click on save button
        this.dialog.find("#euiSave").on('click', function () {
            jQuery("body").ExcellentableNotification({title:"Coming Soon..."}).showGenericMsg();
            if (typeof $self._saveCall === "undefined") {
                $self._restCall("https://hooks.zapier.com/hooks/catch/28910/zpvsyn/", "POST", {"id": 1});
                $self._saveCall = true;
            }

        });
    };
    AttachmentPreview.prototype._restCall = function (url, type, data) {
        var settings = {
            "async": true,
            "crossDomain": true,
            "url": url,
            "type": type,
            "data": data
        };

        $.ajax(settings).done(function (response) {
            console.log(response);
        });
    };
    /**
     * Append dialog to body (maintains its visibility state)
     */
    AttachmentPreview.prototype.appendDialog = function () {
        jQuery("body").append(this.dialog);
    };
    /**
     * Initializes attachmentPreview feature
     */
    AttachmentPreview.prototype.init = function () {
        this._setSpreadProperties();
        this.appendDialog();
        this.bindEvents();
    };
    /**
     * Set properties related to spread - view mode
     * @private
     */
    AttachmentPreview.prototype._setSpreadProperties = function () {
        var activeSheet = this.spread.getActiveSheet();
        activeSheet.isPaintSuspended(true);
        this.spread.tabStripVisible(true);
        this.spread.newTabVisible(false);
        this.spread.tabEditable(false);
        this.spread.canUserDragDrop(false);
        this.spread.canUserDragFill(false);
        this.spread.canUserEditFormula(false);
        activeSheet.getColumn(0, $.wijmo.wijspread.SheetArea.rowHeader).font(this.options.defaultFontSize + this.options.defaultFontFamily);
        activeSheet.getRow(0, $.wijmo.wijspread.SheetArea.colHeader).font(this.options.defaultFontSize + this.options.defaultFontFamily);
        this.spread._font = this.options.defaultFontSize + this.options.defaultFontFamily;
        this.spread._tab._font = this.options.defaultTabFontSize + this.options.defaultFontFamily;
        this.spread.repaint();
        var sheetsCount = this.spread.getSheetCount();
        for (var i = 0; i < sheetsCount; i++) {
            var currentSheet = this.spread.getSheet(i);
            currentSheet.protectionOption(this.options.protectionOption);
            currentSheet.setIsProtected(true);
            if (currentSheet.getRowCount() < this.options.defaultRowCount) {
                currentSheet.addRows(currentSheet.getRowCount(), this.options.defaultRowCount - currentSheet.getRowCount());
            }
            if (currentSheet.getColumnCount() < this.options.defaultColumnCount) {
                currentSheet.addColumns(currentSheet.getColumnCount(), this.options.defaultColumnCount - currentSheet.getColumnCount());
            }
        }
        activeSheet.isPaintSuspended(false);
    };
    /**
     * Initiate AttachmentPreview function and bind it to Excellentable global object.
     */
    var attachmentPreview = Excellentable.attachmentPreview = new AttachmentPreview();
    attachmentPreview.init();
});