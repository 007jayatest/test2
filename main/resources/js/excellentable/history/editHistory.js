(function ($) {

    $.fn.ExcellentableEditHistoryDialog = function (options) {
        $.fn.ExcellentableEditHistoryDialog.defaults = {
            template: '<li class="eui-version-menu-item" history-id={{historyId}}>\
                                <div class="eui-version-item-link">\
                                    <img src={{imgSrc}} class="eui-version-author-avatar" style="border-radius:50%;">\
                                    <div class="eui-version-author-name">{{authorName}}</div>\
                                    <span class="eui-restore-version">\
                                    '+AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.version.restore")+'\
                                    </span>\
                                    <div class="eui-version-date">{{date}}</div>\
                                </div>\
                            </li>',
            protectionOption: {
                allowFilter: false,
                allowSort: false,
                allowResizeRows: false,
                allowResizeColumns: false,
                allowEditObjects: false
            },
            versionSpreadId: "#euiVersionSpread",
            pageNumber: 1,
            limit: 10
        };

        options = $.extend({}, $.fn.ExcellentableEditHistoryDialog.defaults, options);
        var $self = this;
        this.init = function () {
            var pushRight = new Menu({
                wrapper: '#euiWrapper', // The content wrapper
                type: 'push-right', // The menu type
                menuOpenerClass: '.eui-show-version-button', // The menu opener class names (i.e. the buttons)
                maskId: '#euiVersionMask'
            });
            $self.off(".excVersion");
            jQuery(".eui-show-version-button,#euiDropdownHistory").on('click.excellentable', function (e) {
                e.preventDefault;
                pushRight.open();
                jQuery(".eui-version-topBar").find(".eui-restore-version").remove();
                jQuery(".eui-version-menu-items").empty();
                options.pageNumber = 1;
                $self.getHistoryOverview();
            });
            $self.on("click.excVersion", ".eui-version-menu-item:not('.active')", function () {
                var thisHistory = this;
                jQuery(".eui-version-menu-item").removeClass("active");
                jQuery(this).addClass("active");
                $.fn.Excellentable.BouncyBallSpinner().show();//Version History
                options.historyId = jQuery(this).attr("history-id");

                var versionAjax = $self.ExcellentableDBOperations({
                    operation: "retrieveHistoryById",
                    "ID": options.excellentableId,
                    "historyId": options.historyId
                });
                versionAjax.success(function (data) {
                    $self.showHistory(data);
                    $.fn.Excellentable.BouncyBallSpinner().hide();
                });
            });
            $self.on("click.excVersion", ".eui-view-more", function(){
                options.pageNumber = options.pageNumber + 1;
                $self.getHistoryOverview();

            });
            $self.on("click.excVersion", ".eui-restore-version", function () {
                if(options.liveEditingStatus){
                    jQuery("#euiDropdownSave").trigger("click");
                    options.historyId = jQuery(this).closest(".eui-version-menu-item").attr("history-id");
                    // No need to update options.historyId here. It has been stored in this method $self.on("click", ".eui-version-menu-item"...
                    setTimeout(function () {
                        $self.restoreToThisVersion();
                    }, 1000);
                }else{
                    options.historyId = jQuery(this).closest(".eui-version-menu-item").attr("history-id");
                    // No need to update options.historyId here. It has been stored in this method $self.on("click", ".eui-version-menu-item"...
                    $self.restoreToThisVersion();
                }

            });
            //To get standardise tooltip of excellentable for version dialog close button
            jQuery(".eui-version-topBar").find(".eui-version-menu-close").tooltip();
        };
        this.showHistory = function (data) {
            var metaData = jQuery.parseJSON(data.metaData);
            var $excVersionSpread = jQuery(options.versionSpreadId);
            if ($excVersionSpread.wijspread("spread") == undefined) {
                $excVersionSpread.wijspread();
            }
            var versionSpread = $excVersionSpread.wijspread("spread");
            versionSpread.fromJSON(metaData);
            var versionActiveSheet = versionSpread.getActiveSheet();
            versionActiveSheet.isPaintSuspended(true);
            if(metaData == null || metaData == ""){ //If metaData is blank. Usualy when user selects first version of Excellentable.
                versionSpread.clearSheets();
                versionSpread.addSheet(0,new $.wijmo.wijspread.Sheet("Sheet1"));
                versionActiveSheet = versionSpread.getActiveSheet();
                versionActiveSheet.isPaintSuspended(true);
                versionActiveSheet.selectionBorderColor(options.selectionBorderColor);
                versionActiveSheet.setRowCount(options.defaultRowCount);
                versionActiveSheet.setColumnCount(options.defaultColumnCount);
                versionActiveSheet.defaults.rowHeight = options.defaultRowHeight;
                versionActiveSheet.defaults.colWidth = options.defaultColWidth;
                versionSpread.scrollbarMaxAlign(true);
                versionSpread.scrollbarShowMax(true);
                versionSpread.showVerticalScrollbar(false);
            }
            versionSpread.tabStripVisible(true);
            versionSpread.newTabVisible(false);
            versionSpread.tabEditable(false);
            versionActiveSheet.getColumn(0, $.wijmo.wijspread.SheetArea.rowHeader).font(options.defaultFontSize+options.defaultFontFamily);
            versionActiveSheet.getRow(0, $.wijmo.wijspread.SheetArea.colHeader).font(options.defaultFontSize+options.defaultFontFamily);
            versionSpread._font = options.defaultFontSize+options.defaultFontFamily;
            versionSpread._tab._font = options.defaultTabFontSize+options.defaultFontFamily;
            versionSpread.repaint();
            var sheetsCount = versionSpread.getSheetCount();
            for (var i = 0; i < sheetsCount; i++) {
                var versionCurrentSheet = versionSpread.getSheet(i);
                versionCurrentSheet.protectionOption(options.protectionOption);
                versionCurrentSheet.setIsProtected(true);
                if (versionCurrentSheet.getRowCount() < options.defaultRowCount) {
                    versionCurrentSheet.addRows(versionCurrentSheet.getRowCount(), options.defaultRowCount - versionCurrentSheet.getRowCount());
                }
                if (versionCurrentSheet.getColumnCount() < options.defaultColumnCount) {
                    versionCurrentSheet.addColumns(versionCurrentSheet.getColumnCount(), options.defaultColumnCount - versionCurrentSheet.getColumnCount());
                }
            }

            //Show and hide note handled for version history
            showCommentsview(versionSpread);
            versionActiveSheet.isPaintSuspended(false);
        };
        this.getHistoryOverview = function () {
            var historyOverviewAjax = $self.ExcellentableDBOperations({
                operation: "retrieveAllHistory",
                "ID": options.excellentableId,
                "pageNumber": options.pageNumber,
                "limit": options.limit
            });
            historyOverviewAjax.success(function (data) {
                if (data.size === 0) {
                    $self.ExcellentableNotification({title: 'No history found'}).showWarningMsg();
                } else {
                    var temp = "";
                    $.each(data.results, function (index, obj) {
                        temp = temp + options.template.replace(/{{historyId}}/ig, obj.historyID).replace(/{{imgSrc}}/ig, obj.profilePicPath)
                                .replace(/{{authorName}}/ig, obj.creatorFullName).replace(/{{date}}/ig, obj.createdDate);
                    });

                    jQuery(".eui-version-menu-items").append(temp);
                    var historyId=jQuery(".eui-version-menu-items li:not(:first-child).active").attr("history-id");
                    jQuery(".eui-view-more").remove();

                    if(data._links.next !== "") {
                        jQuery(".eui-version-menu-items").append("<li class='eui-view-more'><span class='eui-view-more-version-history'>"+AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.version.view.more")+"</span></li>");
                    }
                    var latestVersion = jQuery(".eui-version-menu-item:first");
                    latestVersion.attr("restore", false);
                    // on page load getting history-id as undefine, so added class active and get the id of latest version
                    if (historyId === undefined) {
                        historyId = latestVersion.addClass("active").attr("history-id");
                    }
                    if(historyId != undefined){
                        $.fn.Excellentable.BouncyBallSpinner().show();//Version History inner
                        var versionAjax = $self.ExcellentableDBOperations({
                            operation: "retrieveHistoryById",
                            "ID": options.excellentableId,
                            "historyId": historyId
                        });
                        versionAjax.success(function (data) {
                            $self.showHistory(data);
                            $.fn.Excellentable.BouncyBallSpinner().hide();
                        });
                    }

                    // Set liveediting params for restoring
                    $self.initLiveEditingForRestore();
                }
            });
        };
        this.restoreToThisVersion = function() {
            var restoreToAjax = $self.ExcellentableDBOperations({
                operation: "restoreTo",
                "ID": options.excellentableId,
                "historyId": options.historyId
            });
            restoreToAjax.success(function(data){
                $self.lastRestoredMetaData = data.metaData;
                options.reloadOnClose = true; //Reload Excellentable on Page View Mode on Exit.
                var temp = options.template.replace(/{{historyId}}/ig, data.historyID).replace(/{{imgSrc}}/ig, data.profilePicPath)
                                .replace(/{{authorName}}/ig, data.creatorFullName).replace(/{{date}}/ig, data.createdDate);
                jQuery(".eui-version-menu-items").prepend(temp);
                jQuery(".eui-version-menu-item").removeClass("active").first().addClass("active");
                jQuery(".eui-edit-spread").empty().append('<div id="euiPrintArea"></div>');
                options.type= "edit";
                options.updateAttachmentOnLoad = true;
                options.isEditHistoryAlreadyInitialized = true;
                $self.Excellentable(options);
                $self.faviconSetter(options.confFavicon);
                $self.find(".eui-version-menu-close").trigger("click");

                $self.ExcellentableNotification({"title":AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.version.restore.success")}).showSuccessMsg();
            });
        };
        this.initLiveEditingForRestore = function(){
            // If live editing is enabled , then force restore enable
            // restoreVersion is used in live editing to push restore content to other users
            if(options.liveEditingStatus){
              options.restoreVersion = 1;

              // Allow users to restore to current version to discard changes made thru draft
              var latestVersion = jQuery(".eui-version-menu-item:first");
              latestVersion.attr("restore", true);
              latestVersion.find('.eui-restore-version').text('Discard Draft');
              latestVersion.hover(function() {
                  $(this).find('.eui-restore-version').css("display","inline-block");
              },function(){
                  $(this).find('.eui-restore-version').css("display","none");
              });
            }else{
              options.restoreVersion = 0;
            }
        };
        this.init();
    };
})(jQuery);
