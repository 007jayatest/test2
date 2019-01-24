(function ($) {
    var Excellentable = window.Excellentable || {};
    /**
     * Extends excellentable to consist of export object which is used to export
     * @type {Function}
     */
    var ExportRequest = Excellentable.ExportRequest = Excellentable.ExportRequest || function (type, data, name){
            this.type = type;
            this.data = data;
            this.name = name;
            this.acceptType = this.getAcceptType();
        }
    /**
     * Parse the error input blob and notifies user + prints error in console log
     * @param result : error in blob format
     * @private
     */
    ExportRequest.prototype._onError = function (result) {
        var fr = new FileReader;
        fr.onload = function (ev) {
            var error = JSON.parse(result);
            jQuery("body").ExcellentableNotification({title:error.userMessage}).showErrorMsg();
            console.log(error.systemMessage);
        }
        fr.readAsText(this.response);
    };
    /**
     * Makes export call which passes the "type", "data", "name" as multipart/form-data and retrieves the data as blob,
     * if error message then converts blob to string
     */
    ExportRequest.prototype.makeCall = function () {
        var $self = this;

        if(this.type === "csv") {
            this._generateBlob(this.data, this.acceptType, this.type, this.name);
            return;
        }
        var url = "/rest/excellentable/1.0/export";
        var myFormData = new FormData();
        myFormData.append("type",this.type); //Export type xlsx/pdf/json
        myFormData.append("data",this.data);
        myFormData.append("name",this.name); //Export file name
        //Ajax is not able to handle binay data thus XHR
        var xhr = new XMLHttpRequest();
        xhr.open('POST', AJS.contextPath() + url);
        xhr.responseType = 'blob';
        xhr.setRequestHeader("X-Atlassian-Token", "no-check");
        xhr.setRequestHeader("Accept", this.acceptType);
        xhr.onload = function(e) {
            if (this.status === 200) {
                $self._generateBlob(this.response, $self.acceptType, $self.type, $self.name);
            } else if (this.status === 400){
                $self._onError(this.result);
            } else if (this.status === 401){
                var msg = AJS.I18n
                    .getText("com.addteq.confluence.plugin.excellentable.logout.errorMessage", AJS.params.baseUrl + '/login.action');
                jQuery('body').ExcellentableNotification({ body: msg, fadeout: false }).showErrorMsg();
            } else {
                //More error handling code here
            }
        };
        xhr.send(myFormData);
    }
    /**
     * Creates a temporary link which allows user to download file returned from rest
     * @param data : blob (content to be exported)
     * @param contentType : "content type of data"
     * @param extension : example xlsx, csv
     * @param fileName : name of the file to be exported
     * @private
     */
    ExportRequest.prototype._generateBlob = function(data, contentType, extension, fileName) {

        var blob = new Blob([data], {type: contentType});

        // Internet Explorer does not presently support the Download attribute on A tags. So here is the work around
        if (window.navigator.msSaveOrOpenBlob) { // for IE and Edge
            window.navigator.msSaveBlob(blob, fileName + '.' + extension);
        } else {
            // for modern browsers
            //Create a link element, hide it, direct it towards the blob, and then 'click' it programatically
            var a = document.createElement("a");
            a.style.display = 'none';
            document.body.appendChild(a);
            //Create a DOMString representing the blob and point the link element towards it
            var url = window.URL.createObjectURL(blob);
            a.href = url;
            a.download = fileName + '.' + extension;
            a.click();
            //release the reference to the file by revoking the Object URL
            window.URL.revokeObjectURL(url);
        }
    }
    /**
     * Get the content type which is to be sent in request header accept key.
     * @param extension : extension selected by user
     * @returns {string} : returns string or contentType
     */
    ExportRequest.prototype.getAcceptType = function () {
        var extension = this.type.toUpperCase();
        switch (extension) {
            case "XLSX":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "PDF":
                return "application/pdf";
            case "CSV":
                return 'text/csv';
            case "JSON":
                return "application/json";
            default:
                return "application/json";
        }
    }

    $.fn.Excellentable = function (options) {

        if (typeof AJS.params !== "undefined") { // Default params for atlassian plugin
            options.pageId = AJS.params.pageId;
        } else { // Default params for Qunit
            options.pageId = "";
        }

        if (typeof AJS.params.pageId !== "undefined") { // For template
            options.convertToImage = true;
        } else {

            options.convertToImage = false;
        }

        $.fn.Excellentable.defaults = {
            excellentableId: null, // Unique id of excellentable e.g 2 , 55 etc.
            excellentableIdAttr: "excellentable-id", //Name of the html element attr which holds excellentable unique Id.
            loadingDivId: ".loadingDiv", //loading image div Id
            globalFilterDivId: "#euiFilterString", //Id of global filter div on "Page View" mode
            menubarId: ".eui-menu-bar", //Id of Menu Bar div on "Page View" mode
            type: "view", //Excellentable type edit/view.
            defaultRowCount: 60,
            defaultColumnCount: 20,
            defaultRowHeight:25,
            defaultColWidth:150,
            viewportMaxHeight : 700,
            defaultFontSize : "11pt ",
            defaultTabFontSize : "10pt ",
            defaultFontFamily : AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.font.verdana"),
            selectionBorderColor: "#52b052",
            reloadOnClose : false,
            excFavicon: {icon : AJS.contextPath() + "/download/resources/Addteq.Excellentable:spreadJSResourcesV1/images/exc-sq-icon.png",
            			short : AJS.contextPath() + "/download/resources/Addteq.Excellentable:spreadJSResourcesV1/images/exc-sq-icon.png"
            			},
            confFavicon: "",
            hiddenRowColHeaderBackColor : "crimson",
            fullscreen:false,
            searchTerm:'',
            edit: {//Excellentable edit mode properties
                divId: ".eui-edit-spread", //div Id on which to apply excellentable
                columnFilter: false,
                allowFilter:true,
                allowSort:true,
                allowEdit: true,
                resizableColumn: true,
                resizableRow: true,
                showHideOnMouserHover: false,
                globalFilter: false,
                showMinimalData: false,
                multisheet: false,
                scrollTip:"Both",
                keyboardShortcut: true,
                allowAddNewSheet: true,
                tabEditable : true,
                confluenceKeyboardShortcut: false,
                TabNavigationButton: true
            },
            view: {//Excellentable view mode properties
                mainDivId: ".eui-exc-container[excellentable-id]",
                divId: ".eui-view-spread",
                columnFilter: true,
                allowFilter:true,
                allowSort:true,
                allowEdit: false,
                resizableColumn: true,
                resizableRow: true,
                showHideOnMouserHover: false,
                globalFilter: true,
                showMinimalData: true,
                multisheet: false,
                scrollTip:"Both",
                keyboardShortcut: false,
                allowAddNewSheet : false,
                tabEditable: false,
                confluenceKeyboardShortcut: true,
                TabNavigationButton: false
            }
        };

        // variable declaration
        var $self = this;
        var spread;
        var activeSheet;
        var $spreadView;
        var tableMetaData;
        var excellentableId;
        var rowCount;
        var columnCount;
        var maxDataRow;
        var maxDataCol;
        var exportType="xls";
        var exportJsonData;
        var exportCsvData;
        var defaultStyle;
        var formulabox;
        var spinnerId;
        var initialData;
        var exportFileName;
        var historyID;
        var versionNumber;
        var excellentableLiveObject = {};
        var unsavedContentManager = {};
        var selectedPicture;
        var isScrolling;
        //tracks whether the user is currently editing a cell
        var isEditing = false;

        //Keeps track on wheter the user is in hovering in or out of a table in view mode
        var leftViewTable = false;
        //Keeps track of the last table the user has hovered on, in order to support multiple macros with notes in the same page
        var currentTableId = null;

        options = $.extend(true, $.fn.Excellentable.defaults, options), parser = new GcSpread.Sheets.Calc.Parser();

        this.init = function () {
            $self.css({"width":"auto"});
            excellentableId = options.excellentableId;
            $self.refreshBorderDialog();
            var isSpinnerForMacro = false;//The spinner being used is for macro or not.
            if(options.type == "view"){
                spinnerId= '.eui-exc-container[excellentable-id='+excellentableId+'] '+ options[options.type].divId;
                isSpinnerForMacro = true;
            }else{
                spinnerId = '#euiDialog .eui-dialog2-content';
            }
            // checking content type of page
            if (AJS.params.contentType == "blogpost") {
                exportFileName = AJS.params.latestPublishedPageTitle;
            } else {
                exportFileName = AJS.params.pageTitle;
            }
            /*
            * Ref: EXC-3024
            * Bouncy ball logo takes little time to load hence we need to wait to get logo loaded. So we have used Deferred here.
            */
            jQuery.when($.fn.Excellentable.BouncyBallSpinner().show(spinnerId, isSpinnerForMacro)).then(function(){ //View Mode
            //Based on the language selected on confluence, applied the same language to Spread Sheet.
            GcSpread.Sheets.Culture(typeof AJS.Meta !=="undefined"? AJS.Meta.get("user-locale").replace(/_/g,"-") : "en");
            $spreadView = $self.find(options[options.type].divId);
            if (typeof excellentableId !== "undefined") {
                var response = $self.loadExcellentableFromDB();
                response.success(function (data) {
                        if (data.metaData != "" && data.isGZipped) {
                            var decodeBase64 = window.atob(data.metaData);
                            var unzippedResponse = pako.ungzip(decodeBase64, { to: 'string' });
                            data.metaData = unzippedResponse;
                        }
                    tableMetaData = data.metaData.trim();
                    /* If the filter with version prior 3 is shared */
                    if(data.filterVersion == undefined || data.filterVersion < 3){
                        //For share functionality. If global search is shared with any user prior filterVersion 2.
                        if (data.globalSearchString !== undefined && data.globalSearchString.trim() !== "") {
                            var globalSearchString = data.globalSearchString.split(" ");
                            $self.find(".eui-live-search").val(globalSearchString);
                        }
                    }

                    //live editing initializing
                    $self.initCollaborativeOptions(data)

                    $self.convertToSpread(data);
                    initialData = data.metaData.trim();
                    activeSheet = spread.getActiveSheet();
                    rowCount = activeSheet.getRowCount();
                    columnCount = activeSheet.getColumnCount();
                    (options[options.type].showMinimalData === true && !options.fullscreen) ? $self.getMinimalCount(tableMetaData) : "";
                    options.confFavicon = $self.getConfluenceFavicon();
                    options.type === "edit" ? $self.applyEditMode() : $self.applyViewMode();
                    options[options.type].showHideOnMouserHover == true ? $self.applyShowHideOnMouserHover() : $self.removeShowHideOnMouserHover();
                    options[options.type].globalFilter == true ? $self.applyGlobalFilter(options.fullscreen, options.searchTerm) : $self.removeGlobalFilter();
                    options[options.type].multisheet == true ? $self.applyMultiSheet() : $self.removeMultiSheet();
                    options[options.type].keyboardShortcut == true ? $self.ExcellentableKeyboardShortcuts() : "";
                    options[options.type].confluenceKeyboardShortcut == true ? $self.enableConfluenceKeyboardShortcuts() : $self.disableConfluenceKeyboardShortcuts();
                    options[options.type].allowAddNewSheet == true ? spread.newTabVisible(true) : spread.newTabVisible(false);
                    options[options.type].tabEditable == true ? spread.tabEditable(true) : spread.tabEditable(false);
                    options[options.type].TabNavigationButton == true ? spread.tabNavigationVisible(true) : spread.tabNavigationVisible(false);
                    typeof options[options.type].scrollTip !== undefined ? $self.scrollTip(options[options.type].scrollTip) : "";
                    $self.bindEvents();
                    $.fn.ExcOverwriteOriginalClipboardCopyFunction();
                    $.fn.ExcOverrideOriginalFloatingObjectFunction();
                    $.fn.ExcOverwriteOriginalEndSheetTabEditingNameValidation();
                    activeSheet.isPaintSuspended(true);
                    var sheetName = activeSheet.getName();
                    tableMetaData = spread.toJSON();
                    refreshProtectedCellCount(tableMetaData);
                    //Get default row height and column width from JSON data of sheet
                    if (typeof tableMetaData.sheets[sheetName].defaults !== "undefined") {
                        activeSheet.defaults.rowHeight = tableMetaData.sheets[sheetName].defaults.rowHeight;
                        activeSheet.defaults.colWidth = tableMetaData.sheets[sheetName].defaults.colWidth;
                    } else { //If default row height and column width is not present in JSON data of sheet, set as the default values of excellentable
                        activeSheet.defaults.rowHeight = options.defaultRowHeight;
                        activeSheet.defaults.colWidth = options.defaultColWidth;

                    }
                    activeSheet.isPaintSuspended(false);
                    var selectedMenuTab = jQuery("#euiMenuBar .active-tab").find("a").attr("href") || "#euiFormatHome";
                    $self.editSpreadHeight(selectedMenuTab);
                    //enable share filter

                    var dialogObj = AJS.Excellentable.ShareFilter.initDialog($self.find(".eui-share-filter"), "shareFilter",
                        {
                        initCallback: function (popup) {
                            jQuery(this.popup).attr("excellentable-id", excellentableId);
                        }
                    });

                    //Get JSON array of all the selectors which has tipsy along with gravity. Synonym:tooltip
                    var tipsyElements = [
                        {
                            "element":jQuery("#euiMenuBar").find("button.eui-left-most-button[data-gravity='s']"),
                            "gravity":"sw"
                        },
                        {
                            "element":jQuery("#euiMenuBar").find("button.eui-left-most-button"),
                            "gravity":"nw"
                        },
                        {
                            "element":jQuery("#euiMenuBar,"+options[options.type].mainDivId).find("button:not(.eui-left-most-button):not([data-gravity='s'])"),
                            "gravity":"n"
                        },
                        {
                            "element": jQuery("#euiMenuBar," + options[options.type].mainDivId).find("button[data-gravity='s']"),
                            "gravity": "s"
                        },
                        {
                            "element":jQuery("#euiMenuBar,"+options[options.type].mainDivId).find("a:not([data-gravity])"),
                            "gravity":"n"
                        },
                        {
                            "element":jQuery("#euiMenuBar,"+options[options.type].mainDivId).find("a[data-gravity='s']"),
                            "gravity":"s"
                        },
                        {
                            "element":jQuery("#euiMenuBar,"+options[options.type].mainDivId).find("a[data-gravity='w']"),
                            "gravity":"w"
                        },
                        {
                            "element":jQuery("#euiMenuBar").find(".eui-show-version-button,#euiDialogCloseButton"),
                            "gravity":"n"
                        },
                        {
                            "element":jQuery(".eui-dropdownmenu").find("aui-item-link"),
                            "gravity":"w"
                        },
                        {
                            "element":jQuery(".eui-fullscreen-menubar").find("button"),
                            "gravity":"n"
                        },
                        {
                            "element":jQuery(".eui-fullscreen-menubar").find("a.aui-button"),
                            "gravity":"w"
                        },
                        {
                            "element":jQuery(".eui-fullscreen-menubar").find("a[data-gravity='w']"),
                            "gravity":"w"
                        }
                    ];
                    jQuery.each(tipsyElements, function (index, item) {
                        // set the trigger type and gravity
                        item.element.tooltip({trigger: 'hover', gravity: item.gravity, delayIn: 1000, delayOut: 1000});
                        // trigger show and hide on mouse enter and mouse leave
                        item.element.mouseenter(function () {
                            jQuery(this).tooltip("show");
                        }).mouseleave(function () {
                            jQuery(this).tooltip("hide");
                        });
                    });
                    /*
                     * Ref: EXC-2781
                     * Font files takes little time to load hence we need to repaint spread once all font files are loaded.
                     */
                    setTimeout(function () {
                        spread.repaint();
                    },1);

                    if(options.type === "edit" && options.liveEditingStatus ){
                      $self.initCollaborativeEditing(data);
                    }else{
                        $self.initializeAvatarManager(data);
                    }
                    if(options.type === "edit") {
                        $self.initUnsavedContentManager();
                    }


                    if(options.type === "edit" && options.liveEditingStatus === 0 ) {
                        //EXC-5010 Loading notification dialog ONLY if liveEdit was NEVER tried
                        options.liveEditTried = data.multieditConnectionInfo.tried;
                        if (!options.liveEditTried) {
                            var messages = {
                                0: AJS.I18n.getText("com.addteq.confluence.plugin.wnm.message2")
                            };
                            Excellentable.wnm = new Excellentable.WhatsNew("excellentableEdit", messages);
                            Excellentable.wnm.ready.done(function () {
                                Excellentable.wnm.updateAppendTimeout(5000);
                                Excellentable.wnm.updateDialogZIndex(3100)
                                Excellentable.wnm.appendDefaultDialog();
                            })
                        }
                    }

                });
                response.error(function (data) {
                    $.fn.Excellentable.BouncyBallSpinner().hide(spinnerId);
                    $self.errorUnableToLoadExcellentable();
                });
            }
            });
        };
        /**
         * Workaround method to deal with bug on "AJS.dialog2()c:wpick .show" where it adds additional elements for uknown reasons.
         * The bug occurs on the border dialog after the user leaves the table for the first time -
         * from then on, every time the user tries to reopen it, the AJS.dialog2() will add another a "shadow" border dialog -
         * causing a few bugs when the code executes.
         * @returns nothing
        */
        this.refreshBorderDialog = function() {
            var borderDialogs = AJS.$('[id="euiBorderFormat"]');
            if (borderDialogs.length > 1) {
                AJS.$(borderDialogs.get(1)).remove();
            }
        }

        /**
          * Display error message instead of Excelletable when unable to load excellentable content from REST GET call
          * @param Error message template to display
          * @returns nothing
        */
        this.errorUnableToLoadExcellentable = function(){
            var messageDiv = jQuery(this);
            messageDiv.children().remove();
            messageDiv.css('padding-top','30px');

            var loginURL = AJS.params.baseUrl + '/login.action'
            var errorLogoutMessage = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.logout.errorMessage",loginURL);

            // if the error message is on dialog. Provide option to close dialog
            var closeableValue = messageDiv.attr('role') === 'dialog' ? true : false;

            AJS.messages.error(messageDiv, {
                 title:"Unable to retrieve Excellentable",
                 body: '<p>'+errorLogoutMessage+'</p>',
                 closeable: closeableValue
            });

            // Close edit dialog in view mode when user closes the message
            messageDiv.find('div.aui-message span.icon-close').click(function(){
                messageDiv.remove()
            });
        }

        this.initUnsavedContentManager = function () {
            var excellentableOptions = {
                spreadObject: spread
            }
            unsavedContentManager = new UnsavedContentManager(excellentableOptions);
            unsavedContentManager.init();
        }

        this.initializeAvatarManager = function (data) {
            if(options.type === "edit"){
                historyID = data.historyID;
                versionNumber = data.versionNumber;
                //Trigger - Add collaborator avatar code (undefined is passed so method will take default values from properties file)
                var msg = {
                    excellentableId: excellentableId,
                    versionNumber: versionNumber,
                    repeatTime: undefined,
                    htmlElement: "#euiUserImageArea",
                    maxWidthPercentage: 30
                };
                $.event.trigger({
                    type: "initializeAvatarManager",
                    msg: msg
                });
            }
        };

        this.initCollaborativeOptions = function(data){
          if(options.type === "edit" && data.multieditConnectionInfo.status ){
            if( data.multieditConnectionInfo.hasError ){
              options.liveEditingStatus = 0;
              ExcellentableApp.CollaborativeEditing.setStatus(false);

              var msgObj = AJS.messages.error({
                  title: 'Error Initializing Collaborative editing. Now editing in Single user mode.',
                  body: "<p>"+ data.multieditConnectionInfo.message +"</p>",
                  fadeout: false
              });
              msgObj.addClass("eui-aui-msg").appendTo("#main-header");
            }else{
              options.liveEditingStatus = data.multieditConnectionInfo.status;

              if(options.liveEditingStatus === 0) {
                  ExcellentableApp.CollaborativeEditing.setStatus(false);
              } else {
                  ExcellentableApp.CollaborativeEditing.setStatus(true);
              }

              //user session
              if(! options.sessionId ){
                options.sessionId = new Date().getTime();
              }
            }
          }else{
            options.liveEditingStatus = 0;
            ExcellentableApp.CollaborativeEditing.setStatus(false);
          }
        }

        this.initCollaborativeEditing = function(data){

          var excellentableOptions = {
            spreadObject : spread,
            excellentableId : data.id,
            sessionId : options.sessionId,
            fbContextPath : data.multieditConnectionInfo.firebaseContext,
            userId :   AJS.Meta.get("remote-user") || data.updater || data.creator + Math.floor(Math.random(2000)*1000) ,
            userName : AJS.params.currentUserFullname || AJS.Meta.get("remote-user") || data.updater || data.creator,
            restoreOptions : {
              restoreFlag : options.restoreVersion,
              restoreContent : data.metaData
            },
            userAvatarUrl : AJS.params.currentUserAvatarUrl
          }

          // firebase options to be provided by excelletnable plugin
          // this information must be retrieved as part of admin page
          var firebaseOptions = {
            apiKey: data.multieditConnectionInfo.apiKey,
            databaseURL: data.multieditConnectionInfo.firebaseUrl,
            firebaseToken : data.multieditConnectionInfo.token
          };

          excellentableLiveObject = new ExcellentLiveOperations(excellentableOptions,$self);
          excellentableLiveObject.firebaseInit(firebaseOptions).then(function(){

            // Validate if firebase has init properly
            if(excellentableLiveObject.getState()){
              excellentableLiveObject.setUpLiveContext();
              $self.replaceSaveWithPublish();
            }else{
              options.liveEditingStatus = 0;
              console.log("Collaborative editing initialization failed");
            }
          }).catch(function(){
            $self.initializeAvatarManager(data);
            options.liveEditingStatus = 0;
            console.log("Collaborative editing database init failed");
          });
        }

        this.replaceSaveWithPublish = function () {
            //Changing save to publish
            var element = jQuery("aui-item-link#euiDropdownSave").find("a")
            var position = element.text().indexOf("(");
            var text = element.text().substring(1,position);
            var html = element.html();
            var publishText = AJS.I18n.getText('com.addteq.confluence.plugin.excellentable.publish');
            element.html(html.replace(text, publishText));
        }

        this.cellReferenceFormat = function (currentObj, activeRow, activeColumn) {
            var currentFormatter = null;
            // External cell reference
            if (currentObj instanceof GcSpread.Sheets.Calc.Expressions.ExternalCellExpression) {
                var sheetReference = currentObj.source.getSheet();
                // Relative external cell reference
                if (currentObj.rowRelative && currentObj.columnRelative)
                    currentFormatter = sheetReference.getFormatter(currentObj.row + activeRow, currentObj.column + activeColumn);
                // Absolute external cell reference
                else
                    currentFormatter = sheetReference.getFormatter(currentObj.row, currentObj.column);
                // Cell reference
            } else {
                // Relative cell reference
                if (currentObj.rowRelative && currentObj.columnRelative)
                    currentFormatter = activeSheet.getFormatter(currentObj.row + activeRow, currentObj.column + activeColumn);
                // Absolute cell reference
                else
                    currentFormatter = activeSheet.getFormatter(currentObj.row, currentObj.column);
            }
            var existingFormatter = activeSheet.getCell(activeRow, activeColumn).formatter();
            //If formatter does not exist on current cell & cells being referenced has some formatter applied on it.
            if (currentFormatter != null && typeof existingFormatter == "undefined"){
                activeSheet.getCell(activeRow, activeColumn).formatter(currentFormatter);
            }
        };
        this.rangeReferenceFormat = function (currentObj, activeRow, activeColumn) {
            var currentFormatter = null;
            // External range reference
            if (currentObj instanceof GcSpread.Sheets.Calc.Expressions.ExternalRangeExpression) {
                var sheetReference = currentObj.source.getSheet();
                // Relative external range reference
                if (currentObj.startRowRelative && currentObj.endRowRelative && currentObj.startColumnRelative && currentObj.endColumnRelative) {
                    for (var r = currentObj.startRow + activeRow; r <= currentObj.endRow + activeRow; r++) {
                        for (var c = currentObj.startColumn + activeColumn; c <= currentObj.endColumn + activeColumn; c++) {
                            currentFormatter = sheetReference.getFormatter(r, c);
                            if (currentFormatter != null) {
                                activeSheet.getCell(activeRow, activeColumn).formatter(currentFormatter);
                            }
                        }
                    }
                    // Absolute external range reference
                } else {
                    for (var r = currentObj.startRow; r <= currentObj.endRow; r++) {
                        for (var c = currentObj.startColumn; c <= currentObj.endColumn; c++) {
                            currentFormatter = sheetReference.getFormatter(r, c);
                            if (currentFormatter != null) {
                                activeSheet.getCell(activeRow, activeColumn).formatter(currentFormatter);
                            }
                        }
                    }
                }
            } else {
                // Relative range reference
                if (currentObj.startRowRelative && currentObj.endRowRelative && currentObj.startColumnRelative && currentObj.endColumnRelative) {
                    for (var r = currentObj.startRow + activeRow; r <= currentObj.endRow + activeRow; r++) {
                        for (var c = currentObj.startColumn + activeColumn; c <= currentObj.endColumn + activeColumn; c++) {
                            currentFormatter = activeSheet.getFormatter(r, c);
                            if (currentFormatter != null) {
                                activeSheet.getCell(activeRow, activeColumn).formatter(currentFormatter);
                            }
                        }
                    }
                    // Absolute range reference
                } else {
                    for (var r = currentObj.startRow; r <= currentObj.endRow; r++) {
                        for (var c = currentObj.startColumn; c <= currentObj.endColumn; c++) {
                            currentFormatter = activeSheet.getFormatter(r, c);
                            if (currentFormatter != null) {
                                activeSheet.getCell(activeRow, activeColumn).formatter(currentFormatter);
                            }
                        }
                    }
                }
            }
        };
        this.nameReferenceFormat = function (currentObj, activeRow, activeColumn, parserContext) {
            var currentFormatter = null;
            var customName = null;
            var expression = null;
            // External name reference
            if (currentObj instanceof GcSpread.Sheets.Calc.Expressions.ExternalNameExpression) {
                //alert("External name in expression.");
            } else {
                customName = activeSheet.getCustomName(currentObj.name);
                if (customName instanceof GcSpread.Sheets.NameInfo) {
                    expression = customName.getExpression();
                    if (expression instanceof GcSpread.Sheets.Calc.Expressions.CellExpression ||
                            expression instanceof GcSpread.Sheets.Calc.Expressions.BangCellExpression ||
                            expression instanceof GcSpread.Sheets.Calc.Expressions.ExternalCellExpression) {
                        $self.cellReferenceFormat(expression, activeRow, activeColumn);
                    } else if (expression instanceof GcSpread.Sheets.Calc.Expressions.RangeExpression ||
                            expression instanceof GcSpread.Sheets.Calc.Expressions.BangRangeExpression ||
                            expression instanceof GcSpread.Sheets.Calc.Expressions.ExternalRangeExpression) {
                        $self.rangeReferenceFormat(expression, activeRow, activeColumn);
                    } else if (expression instanceof GcSpread.Sheets.Calc.Expressions.NameExpression ||
                            expression instanceof GcSpread.Sheets.Calc.Expressions.BangNameExpression ||
                            expression instanceof GcSpread.Sheets.Calc.Expressions.ExternalNameExpression) {
                        $self.nameReferenceFormat(expression, actieRow, activeColumn);
                    } else {
                        $self.eachRecursive(expression, parserContext);
                    }
                }
            }
        };
        this.eachRecursive = function (obj, parserContext)
        {
            var activeRow = activeSheet.getActiveRowIndex();
            var activeColumn = activeSheet.getActiveColumnIndex();
            // Expression is made of a single reference (name or cell)
            if (obj instanceof GcSpread.Sheets.Calc.Expressions.CellExpression ||
                    obj instanceof GcSpread.Sheets.Calc.Expressions.NameExpression ||
                    obj instanceof GcSpread.Sheets.Calc.Expressions.BangCellExpression ||
                    obj instanceof GcSpread.Sheets.Calc.Expressions.BangNameExpression ||
                    obj instanceof GcSpread.Sheets.Calc.Expressions.ExternalCellExpression ||
                    obj instanceof GcSpread.Sheets.Calc.Expressions.ExternalNameExpression) {
                if (obj instanceof GcSpread.Sheets.Calc.Expressions.CellExpression ||
                        obj instanceof GcSpread.Sheets.Calc.Expressions.BangCellExpression ||
                        obj instanceof GcSpread.Sheets.Calc.Expressions.ExternalCellExpression) {
                    $self.cellReferenceFormat(obj, activeRow, activeColumn);
                } else {
                    $self.nameReferenceFormat(obj, activeRow, activeColumn, parserContext);
                }
            }
            for (property in obj) {
                if (obj.hasOwnProperty(property)) {
                    var currentObj = obj[property];
                    if (currentObj instanceof GcSpread.Sheets.Calc.Expressions.CellExpression ||
                            currentObj instanceof GcSpread.Sheets.Calc.Expressions.NameExpression ||
                            currentObj instanceof GcSpread.Sheets.Calc.Expressions.RangeExpression ||
                            currentObj instanceof GcSpread.Sheets.Calc.Expressions.BangCellExpression ||
                            currentObj instanceof GcSpread.Sheets.Calc.Expressions.BangNameExpression ||
                            currentObj instanceof GcSpread.Sheets.Calc.Expressions.BangRangeExpression ||
                            currentObj instanceof GcSpread.Sheets.Calc.Expressions.ExternalCellExpression ||
                            currentObj instanceof GcSpread.Sheets.Calc.Expressions.ExternalNameExpression ||
                            currentObj instanceof GcSpread.Sheets.Calc.Expressions.ExternalRangeExpression) {
                        //var reference = parser.unparse(currentObj, parserContext);
                        // Cell References
                        if (currentObj instanceof GcSpread.Sheets.Calc.Expressions.BangCellExpression ||
                                currentObj instanceof GcSpread.Sheets.Calc.Expressions.CellExpression ||
                                currentObj instanceof GcSpread.Sheets.Calc.Expressions.ExternalCellExpression) {
                            $self.cellReferenceFormat(currentObj, activeRow, activeColumn);
                            // Range references
                        } else if (currentObj instanceof GcSpread.Sheets.Calc.Expressions.BangRangeExpression ||
                                currentObj instanceof GcSpread.Sheets.Calc.Expressions.RangeExpression ||
                                currentObj instanceof GcSpread.Sheets.Calc.Expressions.ExternalRangeExpression) {
                            $self.rangeReferenceFormat(currentObj, activeRow, activeColumn);
                            // Name references
                        } else if (currentObj instanceof GcSpread.Sheets.Calc.Expressions.BangNameExpression ||
                                currentObj instanceof GcSpread.Sheets.Calc.Expressions.NameExpression ||
                                currentObj instanceof GcSpread.Sheets.Calc.Expressions.ExternalNameExpression) {
                            $self.nameReferenceFormat(currentObj, activeRow, activeColumn, parserContext);
                        }
                        $self.eachRecursive(obj.parent, parserContext);
                    }
                    if (property == "args") {
                        $self.eachRecursive(currentObj, parserContext);
                    }
                    if (property == "left") {
                        $self.eachRecursive(currentObj, parserContext);
                    }
                    if (property == "right") {
                        $self.eachRecursive(currentObj, parserContext);
                    }
                }
            }
        };
        /**
         * Hide row and column header in view mode
         */
        this.showHideHeaders = function () {
            var sheetCount = spread.getSheetCount();
            for (var i = sheetCount - 1; i >= 0; i--) {
                var currentSheet = spread.getSheet(i);
                var rowCount = currentSheet.getRowCount(),
                        columnCount = currentSheet.getColumnCount(),
                        topBorderRange = new $.wijmo.wijspread.Range(0, 0, 0, columnCount),
                        leftBorderRange = new $.wijmo.wijspread.Range(0, 0, rowCount, 0);
                if (currentSheet.getColumn(0, $.wijmo.wijspread.SheetArea.rowHeader).backColor() === "crimson") {
                    currentSheet.setRowHeaderVisible(false);
                    currentSheet.setBorder(leftBorderRange, new $.wijmo.wijspread.LineBorder("#dae6f4", $.wijmo.wijspread.LineStyle.thick), {left: true}, 3);
                }
                if (currentSheet.getRow(0, $.wijmo.wijspread.SheetArea.colHeader).backColor() === "crimson") {
                    currentSheet.setColumnHeaderVisible(false);
                    currentSheet.setBorder(topBorderRange, new $.wijmo.wijspread.LineBorder("#dae6f4", $.wijmo.wijspread.LineStyle.thick), {top: true}, 3);
                }
            }
        };
        this.disableConfluenceKeyboardShortcuts = function () { //To disable confluence keyboard shortcuts
            AJS.trigger("remove-bindings.keyboardshortcuts");
            AJS.popup.current = AJS.dialog2("#"+options.excellentableDialogId); //For those shortcucts which are added by AJS.whenIType event
        };
        this.enableConfluenceKeyboardShortcuts = function () { //To enable confluence keyboard shortcuts
            AJS.trigger("add-bindings.keyboardshortcuts");
            AJS.popup.current = null; //For those shortcucts which are added by AJS.whenIType event
        };
        this.formulaToolTip = function () { //Shows applied formula in the tooltip
            $self.find('.eui-view-spread').mousemove(function (event) {
                var leftOffset = event.pageX;
                var topOffset = event.pageY;
                var hitInfo = activeSheet.hitTest(leftOffset - jQuery(this).offset().left, topOffset - jQuery(this).offset().top);
                var content = activeSheet.getFormula(hitInfo.row, hitInfo.col, $.wijmo.wijspread.SheetArea.viewport);

                if (content) {
                    jQuery(this).closest(options[options.type].mainDivId).find('.eui-tool-tip-div').text(content)
                            .css("top", event.clientY + 15)
                            .css("left", event.clientX + 5).show();
                }
                else {
                    jQuery(this).closest(options[options.type].mainDivId).find('.eui-tool-tip-div').hide();
                }
            });

            $self.find('.eui-view-spread').mouseleave(function () {
                jQuery(this).closest(options[options.type].mainDivId).find('.eui-tool-tip-div').hide();
            });
        };
        this.autoAdjustColWidth = function (e, cellInfo) {
            activeSheet.isPaintSuspended(true);
            var row, selStartColumn = cellInfo.colList[0], selColLength = cellInfo.colList.length;
            var selEndcolumn = selColLength + selStartColumn - 1;
            var totalRowCount = activeSheet.getRowCount();
            for (row = 0; row < totalRowCount; row++) {
                for (var col = selStartColumn; col <= selEndcolumn; col++) {
                    var cellWordWrap = activeSheet.getCell(row, col, $.wijmo.wijspread.SheetArea.viewport).wordWrap();
                    if (cellWordWrap == true) {
                        activeSheet.autoFitRow(row);
                        var rowHeight = activeSheet.getRow(row).height(), extraHeight = 20;
                        activeSheet.getRow(row).height(rowHeight + extraHeight);
                        break;
                    }
                }
            }
            activeSheet.isPaintSuspended(false);
        };
        // GcSpread.Sheets.ShowScrollTip.Horizontal, Vertical, Both, None
        this.scrollTip = function(whichScroll) {
            if(whichScroll=="None")
                spread.showScrollTip(GcSpread.Sheets.ShowScrollTip.None);
            else if(whichScroll=="Both")
                spread.showScrollTip(GcSpread.Sheets.ShowScrollTip.Both);
            else if(whichScroll=="Vertical")
                spread.showScrollTip(GcSpread.Sheets.ShowScrollTip.Vertical);
            else if(whichScroll=="Horizontal")
                spread.showScrollTip(GcSpread.Sheets.ShowScrollTip.Horizontal);
        }
        this.applyMultiSheet = function () {
            spread.newTabVisible(true);
        };
        this.removeMultiSheet = function () {
            spread.newTabVisible(false);
        };
        this.faviconSetter = function(favicon){

        	$('head link[rel="icon"]').attr("href",favicon.icon);
        	$('head link[rel="shortcut icon"]').attr("href",favicon.short);
        };

        // store favicon url
        this.getConfluenceFavicon = function(){

        	var icons = {
		        			icon  : jQuery("link[rel='icon']").attr("href"),
		        			short : jQuery("link[rel='shortcut icon']").attr("href")
        				};

        	return icons;
        };

        this.applyViewMode = function () { //open excellentable in View Mode.
            var protectionOption = {
                allowFilter: options[options.type].allowFilter,
                allowSort: options[options.type].allowSort,
                allowResizeRows: options[options.type].resizableRow,
                allowResizeColumns: options[options.type].resizableRow,
                allowEditObjects: options[options.type].allowEdit
            };
            var sheetCount = spread.getSheetCount();
            for (var i = sheetCount - 1; i >= 0; i--) {
                var currentSheet = spread.getSheet(i);
                currentSheet.protectionOption(protectionOption);
                currentSheet.setIsProtected(true);
                currentSheet.clearSelection();  // Clear all selections when on View page
            }
            spread.bind($.wijmo.wijspread.Events.CellDoubleClick, function (sender, args) {
                var currentSheet = spread.getActiveSheet();
                currentSheet.isPaintSuspended(true);
                if (args.sheetArea === $.wijmo.wijspread.SheetArea.colHeader) {
                    currentSheet.setColumnWidth(args.col, currentSheet.defaults.colWidth);
                }

                if (args.sheetArea === $.wijmo.wijspread.SheetArea.rowHeader) {
                    currentSheet.setRowHeight(args.row, currentSheet.defaults.rowHeight);
                }
                if(!options.fullscreen){
                    $self.adjustViewPortSize(true);
                }               
                currentSheet.isPaintSuspended(false);
            });
            //Event call when Range Group is applied on cells
            spread.bind($.wijmo.wijspread.Events.RangeGroupStateChanged, function () {
                $self.adjustViewPortSize(true);
            });
            spread.setTabStripRatio(0.5, true);
            $self.formulaToolTip();
            $self.showHideHeaders();
            //show comments/notes in view mode
            showCommentsview(spread);
            $.fn.Excellentable.BouncyBallSpinner().hide(spinnerId);
            $self.faviconSetter(options.confFavicon);
            //Remove focus from excellentable i.e to fix edit page with keyboard shortcut issue(EXC-887)
            document.activeElement.blur();

            if(options.fullscreen){
                activeSheet = spread.getActiveSheet();
                activeSheet.setRowCount(options.defaultRowCount);
                activeSheet.setColumnCount(options.defaultColumnCount);
                spread.showVerticalScrollbar(true);
            }
        };
        this.applyEditMode = function () { //open Excellentable in edit mode.
            //checking devMode in the edit mode of the table
            spread.showVerticalScrollbar(true);
            if (AJS.params.isDevMode) {
                var generateDataText = AJS.I18n.getText('com.addteq.confluence.plugin.excellentable.generate.data');
                jQuery(".eui-export-json").removeClass("hidden");
                jQuery("div#dataHome li:last-child").after("<li><span><button name='generatedata' title=" +generateDataText
                       +" class='aui-button aui-button-subtle big-button'>"+generateDataText+"</button></span></li>");
                jQuery(options[options.type].mainDivId).css("min-width", "507px");
            }
            spread.setTabStripRatio(0, true);
            //calling events related to insert hyperlink
            $self.ExcInsertHyperlinkEvents();
            $self.ExcellentableTabStrip(options, spread);
            formulabox = new $.wijmo.wijspread.FormulaTextBox(document.getElementById('euiFormulaBox'));
            formulabox.spread(spread);
            //Set default font style as verdana to row/col header
            activeSheet.getColumn(0, $.wijmo.wijspread.SheetArea.rowHeader).font(options.defaultFontSize+options.defaultFontFamily);
            activeSheet.getRow(0, $.wijmo.wijspread.SheetArea.colHeader).font(options.defaultFontSize+options.defaultFontFamily);
            //set focus to the sheet
            setTimeout(function () {
                spread.focus();
                /*Trigger enterCell event when sheet enters edit mode and focus is on the first cell,
                to fetch all the formatting from the first cell of the sheet and populate it in the menu bar*/
                activeSheet.triggerEnterCell({
                    sheet: activeSheet, row: activeSheet.getActiveRowIndex(), col: activeSheet.getActiveColumnIndex()
                });
            }, 10);
            if ($.browser.msie && parseInt($.browser.version, 10) < 9) {
                //run for ie7/8
                spread.bind("SpreadsheetObjectLoaded", function () {
                    initSpread(spread);
                });
            } else {
                initSpread(spread);
            }

            spread.bind($.wijmo.wijspread.Events.CellDoubleClick, function (sender, args) {
                var currentSheet = spread.getActiveSheet();
                var isProtected = protectedCheck(currentSheet);
                var isSelectionProtected = protectedSelectionCheck(currentSheet, currentSheet.getSelections().pop());

                if (!isProtected && !isSelectionProtected) {
                    currentSheet.isPaintSuspended(true);
                    if (args.sheetArea === $.wijmo.wijspread.SheetArea.colHeader) {
                        currentSheet.setColumnWidth(args.col, currentSheet.defaults.colWidth);
                    }

                    if (args.sheetArea === $.wijmo.wijspread.SheetArea.rowHeader) {
                        currentSheet.setRowHeight(args.row, currentSheet.defaults.rowHeight);
                    }
                    currentSheet.isPaintSuspended(false);
                } else if (isSelectionProtected) {
                   args.cancel = true;
                   setTimeout(function() { currentSheet.endEdit() }, 0);
                   return false;
                }
            });

            jQuery(document).on("keyup", function (e) {
                if(e.which === 8 && selectedPicture){
                    var activeSheet = spread.getActiveSheet();
                    activeSheet.removePicture(selectedPicture._name);
                    selectedPicture = undefined;
                }
            });

            spread.bind($.wijmo.wijspread.Events.PictureSelectionChanged, function (sender, args) {
                if(args.picture.isSelected()){
                    selectedPicture = args.picture;
                    args.picture.allowResize(false);
                    args.picture.isSelected(true);
                    jQuery('div.floatingobject-selected').closest('div.floatingobject-container')
                        .append('<button class="eui eui-picture-control aui-button aui-dropdown2-trigger aui-dropdown2-trigger-arrowless ' +
                            'eui-dropdown eui-corner-all aui-icon aui-icon-small aui-iconfont-more" ' +
                            'aria-controls="euipictureDropdown"></button>');
                    $self.appendCustomPictureResizeIndicators(args.picture, args.picture._location.width, args.picture._location.height);
                }else {
                    selectedPicture = undefined;
                    jQuery('div.floatingobject-container').find('button.eui-picture-control').remove();
                    jQuery('.eui-picture-resize-indicator').remove();
                }
            });
            
            spread.bind($.wijmo.wijspread.Events.LeftColumnChanged, function(sender, args) {
                $self.hidePictureUI();
                if(selectedPicture){
                    $self.pictureScrollTimeout(selectedPicture.startColumn(), args.newLeftCol); 
                }
                     
            });
       
            spread.bind($.wijmo.wijspread.Events.TopRowChanged, function(sender, args) {
                $self.hidePictureUI();
                if(selectedPicture){
                    $self.pictureScrollTimeout(selectedPicture.startRow(), args.newTopRow); 
                }
                
            });

            spread.bind($.wijmo.wijspread.Events.EditStarting, function (sender, args) {
                isEditing = true;
                var currentSheet = spread.getActiveSheet(),
                    isSelectionProtected = AJS.$("#eui-comment-dialog").is(":hidden") && protectedSelectionCheck(currentSheet, currentSheet.getSelections().pop());

                if (isSelectionProtected) {
                   args.cancel = true;
                   setTimeout(function() { currentSheet.endEdit() }, 0);
                   return false;
                }
            });

            spread.bind($.wijmo.wijspread.Events.EditEnding, function (sender, args) {
               isEditing = false; 
            });

            jQuery(document).on("keydown", function (e) {
                var initialNewlineHeightIncrease = 10;
                var newlineHeightIncrease = 16;
                var textareaHeight = 32;
                var enterKeycode = 13;
                if(e.which === enterKeycode && e.metaKey && isEditing){
                    //force textarea to go to newline
                    e.target.value = e.target.value  + '\n';
                    //modify the height of the textarea to mimic behavior of CTRL + ENTER
                    var height = parseInt(jQuery(e.target).css('height'), 10);
                    var height = (height >= textareaHeight) ? height + newlineHeightIncrease : height + initialNewlineHeightIncrease;
                    jQuery(e.target).css('height', height);
                }
            });

            $self.initializeColorPickers();
            $self.adjustMinimumRowsAndColumnsAtEditMode();
            
            spread.bind($.wijmo.wijspread.Events.EditChange, function () {
                jQuery('#euiSpreadContentChanged').val("true");
            });
            $self.adjustEditPortSize();
            $self.dragEffect();
            jQuery('.eui-menu-dialog').parent().addClass("eui-formatting-dialog");
            jQuery('#euiTextArea').css("visibility","visible");
            $.fn.Excellentable.BouncyBallSpinner().hide(spinnerId);
            $self.faviconSetter(options.excFavicon);

            // Delete all the selected cells on "Backspace" key press
            activeSheet.addKeyMap(GcSpread.Sheets.Key.backspace, false, false, false, false, GcSpread.Sheets.SpreadActions.clear);
            if(!options.isEditHistoryAlreadyInitialized) {
                $self.ExcellentableEditHistoryDialog(options);
            }
            $self.showEvaluationMsg();
        };
        this.initializeColorPickers = function() {
            var defaultColor = '#52b052';
            var spectrumObject = {
                showPaletteOnly: true,
                togglePaletteOnly: true,
                clickoutFiresChange: true,
                togglePaletteMoreText: 'more',
                togglePaletteLessText: 'less',
                hideAfterPaletteSelect:true,
                color: defaultColor,
                palette: [
                    ["#000","#444","#666","#999","#ccc","#eee","#f3f3f3","#fff"],
                    ["#f00","#f90","#ff0","#0f0","#0ff","#00f","#90f","#f0f"],
                    ["#f4cccc","#fce5cd","#fff2cc","#d9ead3","#d0e0e3","#cfe2f3","#d9d2e9","#ead1dc"],
                    ["#ea9999","#f9cb9c","#ffe599","#b6d7a8","#a2c4c9","#9fc5e8","#b4a7d6","#d5a6bd"],
                    ["#e06666","#f6b26b","#ffd966","#93c47d","#76a5af","#6fa8dc","#8e7cc3","#c27ba0"],
                    ["#c00","#e69138","#f1c232","#6aa84f","#45818e","#3d85c6","#674ea7","#a64d79"],
                    ["#900","#b45f06","#bf9000","#38761d","#134f5c","#0b5394","#351c75","#741b47"],
                    ["#600","#783f04","#7f6000","#274e13","#0c343d","#073763","#20124d","#4c1130"]
                ],
                change: function(color) {
                    var cmd = {
                        color: color.toHexString(),
                        commandName: $(this).attr('name')
                    };
                    executeCommand(spread, cmd);
                }
            };

            colorPickerButtons = $('#euiFormatHome button[name="backcolor"], #euiFormatHome button[name="fontcolor"]');

            $(colorPickerButtons).spectrum(spectrumObject);
        }
        this.pictureScrollTimeout = function(picturePos, screenPos) {
            window.clearTimeout( isScrolling );
            isScrolling = setTimeout(function() {
                if(picturePos > screenPos){
                    $self.showPictureUI();
                }
                
            }, 750); 
        }
        this.hidePictureUI = function(){
            jQuery('div.floatingobject-container').find('button.eui-picture-control').hide();
            jQuery('.eui-picture-resize-indicator').hide();
        }
        this.showPictureUI = function() {
            jQuery('div.floatingobject-container').find('button.eui-picture-control').show();
            jQuery('.eui-picture-resize-indicator').show(); 
        }
        this.appendCustomPictureResizeIndicators = function(picture, width, height) {
            //https://bugs.jqueryui.com/ticket/4186 this code is needed as there is a bug in jqueryui that 
            // even though aspectRatio can be set after initialization there is no effect
            var oldSetOption = $.ui.resizable.prototype._setOption;
            $.ui.resizable.prototype._setOption = function(key, value) {
                oldSetOption.apply(this, arguments);
                if (key === "aspectRatio") {
                    this._aspectRatio = !!value;
                }
            };

            var resizeIndicatorPadding = 10;
            //Remove the resize indicators provided by Spread JS floating object container and
            //any other resize indicators that have been previously appended
            jQuery(".floatingobject-resize-indicator").remove();
            jQuery(".eui-picture-resize-indicator").remove();

            var pictureContainer = jQuery('.floatingobject-content-container.floatingobject-selected').closest('.floatingobject-container');
            pictureContainer.append("<div class='eui-picture-resize-indicator'></div>");
            var pictureResizeIndicator = jQuery(".eui-picture-resize-indicator");

            pictureResizeIndicator.width(width);
            pictureResizeIndicator.height(height);

            pictureResizeIndicator.addClass('no-user-select');
            pictureResizeIndicator.addClass('ui-resizable');

            pictureResizeIndicator.resizable({
                handles: "s, se, e",
                minHeight: 25,
                minWidth: 25,
                alsoResize:'.floatingobject-content-container.floatingobject-selected',
                aspectRatio: true
            });

            pictureResizeIndicator.find('.ui-resizable-handle').on('mouseenter', function(ev){
                picture.allowMove(false);
                //resize direction is determined and aspect ratio is toggled depending on which handle is used
                var srcEl = ev.originalEvent.srcElement.className;
                var direction = srcEl.replace('ui-resizable-handle ui-resizable-', '');
                if(direction === 'e' || direction === 's'){
                    pictureResizeIndicator.resizable('option', 'aspectRatio', false);
                }else {
                    pictureResizeIndicator.resizable('option', 'aspectRatio', true);
                }

            });

            pictureResizeIndicator.on('resize', function(event, ui) {                
                picture.width(ui.size.width);
                picture.height(ui.size.height);         
            });

            pictureResizeIndicator.find('.ui-resizable-handle').on('mouseleave', function(){
                picture.allowMove(true);
            });
       
        }
        this.showEvaluationMsg = function(){
            var response = $self.ExcellentableDBOperations({"operation": "isLicenseEval"});
            response.success(function (data) {
                if(data){
                    var evalMsg = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.evaluation.license",AJS.params.baseUrl);
                    jQuery("#"+options.excellentableDialogId).find("footer").append('<span class="eui-license-eval">'+evalMsg+'</span>');
                }
            });
        }
        this.loadExcellentableFromDB = function () { //Set data to excellentable
            var url= window.location.href, hash = window.location.hash;
            url =url.replace(hash,"");
            var filterHash = $self.ExcellentableCustom({URL: url, param: "eFilter"}).getUrlParameter() ;
            if(typeof filterHash != "undefined" && options.type === "view"){ //show shared filter only on view mode
                var response = $self.ExcellentableDBOperations({"operation": "retrieveSharedTable", ID: excellentableId, secretKey: filterHash });
                response.success(function (data) {
                    if (data.secretKey != "") { //If the secret key is valid
                        location.hash = '#exc-' + excellentableId; //Set the focus on shared excellentable.
                        if ($self.find(".eui-clear-filter").length == 0 && data.filterApplied) { //If filter is not blank
                            var clearFilterText = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.filter.clear");
                            $self.addClass("mediumDiv").find(".eui-help").after('<button name="clear-filter" title="' + clearFilterText + '" class="aui-button aui-button-subtle eui-clear-filter">\
                                                                                         <span class="icon-icomoon icon-Filter-icon-copy"></span>\
                                                                                     </button>');
                            jQuery(document).on("click", ".eui-clear-filter", function () {
                                $self.removeClass("mediumDiv").find(".eui-menu-bar *:not(.eui-export-view-mode)").show();
                                $self.find(".eui-aui-msg-container").remove();
                                jQuery(this).remove();
                                $self.find(".eui-live-search").val("");
                                var pageLink = jQuery('link[rel = "canonical"]').attr("href");
                                history.pushState("", "", pageLink, "");
                                $self.refresh();
                            });
                        }
                    }
                });
                response.error(function (data) {
                    $self.find(".eui-aui-msg-container").css({"padding-top":"4%"});
                    $self.find(".eui-menu-bar *:not(.eui-clear-filter)").hide().closest("#euiFilterString").show();
                });
                return response;
            }else{
                /**
                 * "mode" denotes Operation type i.e view/edit. If you open a Confluence page which
                 * has Excellentable in it then it the type is view when you click on edit button of
                 * Excellentable then it opens the app in edit mode where it passes the mode = edit.
                 */
                var response = $self.ExcellentableDBOperations({"operation": "retrieve", ID: excellentableId, "mode": options.type});
                return response;
            }
        };
        this.convertToSpread = function(responseText){
            tableMetaData = responseText.metaData.trim();
            exportJsonData=tableMetaData;
            $spreadView.wijspread(); // create wijspread control
            spread = $spreadView.wijspread("spread"); // get instance of wijspread control
            activeSheet = spread.getActiveSheet();
            if (tableMetaData !== "") {
                activeSheet.isPaintSuspended(true);
                spread.fromJSON(jQuery.parseJSON(tableMetaData));
                activeSheet = spread.getActiveSheet();
                activeSheet.isPaintSuspended(false);

            } else {
                $self.initBlankSheet();
            }
            spread.tabStripVisible(true);
            spread._font = options.defaultFontSize+options.defaultFontFamily;
            spread._tab._font = options.defaultTabFontSize+options.defaultFontFamily;
            spread.grayAreaBackColor("Transparent");

            /*
             * If the table is retrieved via ShareFilterFeature(i.e via shared link in email)
             */
            var globalFilterApplied = responseText.filterApplied;
            if (globalFilterApplied !== undefined && globalFilterApplied == true) { //If global filter is shared then hide the table on load & show it after applying filter
                $self.find(".eui-view-spread").find("table").addClass("invisible");

                /* If the filter with version >= 3 is shared */
                if (responseText.filterVersion >= 3) {
                    setTimeout(function () {
                        $self.showSearchResultOfActiveSheet(spread.getActiveSheet()); //Show global search result of active sheet.
                    }, 100);
                }
            }
        };
        this.initBlankSheet = function () {
            spread.showVerticalScrollbar(true);
            activeSheet = spread.getActiveSheet();
            activeSheet.isPaintSuspended(true);
            activeSheet.setRowCount(options.defaultRowCount);
            activeSheet.setColumnCount(options.defaultColumnCount);
            activeSheet.selectionBackColor("transparent");
            activeSheet.defaults.rowHeight = options.defaultRowHeight;
            activeSheet.defaults.colWidth = options.defaultColWidth;
            activeSheet.allowCellOverflow(true);
            activeSheet.getColumns(0, activeSheet.getColumnCount()).textIndent(1).vAlign($.wijmo.wijspread.VerticalAlign.center);
            activeSheet.setRowHeight(0, activeSheet.defaults.rowHeight, $.wijmo.wijspread.SheetArea.colHeader);
            activeSheet.getColumn(0, $.wijmo.wijspread.SheetArea.rowHeader).font(options.defaultFontSize+options.defaultFontFamily);
            activeSheet.getRow(0, $.wijmo.wijspread.SheetArea.colHeader).font(options.defaultFontSize+options.defaultFontFamily);
            //set default font style as Verdana
            defaultStyle =  new $.wijmo.wijspread.Style();
            defaultStyle.font = options.defaultTabFontSize+options.defaultFontFamily;
            activeSheet.setDefaultStyle(defaultStyle, $.wijmo.wijspread.SheetArea.viewport);
            activeSheet.isPaintSuspended(false);
        };
        this.getMinimalCount = function (response) { //Get maximum row & column count which has data in it.
            var index = 0;
            var rowArr = new Array();
            var colArr = new Array();
            response = jQuery.parseJSON(response);
            spread.isPaintSuspended(true);
            if (response != null) {
                for (var key in response.sheets) {
                    maxDataRow = 0, maxDataCol = 0;
                    var innerObject = response.sheets[key];
                    var currentSheet = spread.getSheetFromName(key);

                    /*
                     * Find out MaxDataRow(Last utilized row) & MaxDataCol(Last utilized column) based on floating objects.
                     */
                    if (innerObject.hasOwnProperty('floatingObjects')) {  //For floating objects
                        for (var floatingObjKey in innerObject.floatingObjects) {
                            var floatingObject = currentSheet.findPicture(innerObject.floatingObjects[floatingObjKey].name);
                            if (maxDataRow <= floatingObject._endRow) {
                                maxDataRow = floatingObject._endRow;
                            }
                            if (maxDataCol <= floatingObject._endColumn) {
                                maxDataCol = floatingObject._endColumn;
                            }
                            index++;
                        }
                    }

                    /*
                     * Find Out MaxDataRow(Last utilized row) & MaxDataCol(Last utilized column) based on cell data.
                     */
                    if (innerObject.hasOwnProperty('data')) { //For normal text data
                        var obj = innerObject.data.dataTable;
                        if (obj !== undefined) { //If current sheet is not blank.
                            var arr1 = Object.keys(obj);
                            $.grep(arr1, function (value, key) {
                                var arr2 = Object.keys(obj[value]);
                                $.grep(arr2, function (inner_value, inner_key) {
                                    if (maxDataRow < parseInt(value)) {
                                        maxDataRow = value;
                                    }
                                    if (maxDataCol < parseInt(inner_value)) {
                                        maxDataCol = inner_value;
                                    }
                                });
                            });
                        }
                    }

                    /*
                     * Find Out MaxDataRow(Last utilized Row) based on Row Range.
                     */
                    if (innerObject.hasOwnProperty('rowRangeGroup')) { //For row range group
                        var obj = innerObject.rowRangeGroup.itemsData;
                        var arr1 = Object.keys(obj);
                        $.grep(arr1, function (value, key) {
                            var index = obj[key].index, count = obj[key].count;
                            if (obj[key].info.level >= 0 && index + count > maxDataRow) {
                                maxDataRow = index + count;
                            }
                        });
                    }

                    /*
                     * Find Out MaxDataCol(Last utilized Column) based on Column Range.
                     */
                    if (innerObject.hasOwnProperty('colRangeGroup')) { //For column range group
                        var obj = innerObject.colRangeGroup.itemsData;
                        var arr1 = Object.keys(obj);
                        $.grep(arr1, function (value, key) {
                            var index = obj[key].index, count = obj[key].count;
                            if (obj[key].info.level >= 0 && index + count > maxDataCol) {
                                maxDataCol = index + count;
                            }
                        });
                    }

                    if (innerObject.hasOwnProperty('spans')) {//for spans(merge cells)
                        var spansObj = innerObject.spans;
                        var spansArray = Object.keys(spansObj);
                        $.each(spansArray, function (key, value) {
                            var spanObjKey = spansObj[key];
                            var spanRowCount = spanObjKey.rowCount;
                            var spanColCount = spanObjKey.colCount;
                            var spanRow = spanObjKey.row;
                            var spanCol = spanObjKey.col;
                            if (maxDataRow < spanRowCount + spanRow) {
                                maxDataRow = spanRowCount + spanRow;
                            }
                            if (maxDataCol < spanColCount + spanCol) {
                                maxDataCol = spanColCount + spanCol;
                            }
                        });
                    }

                    if (innerObject.hasOwnProperty('comments')) {//for Notes
                        var commentsObj = innerObject.comments;
                        var commentsArray = Object.keys(commentsObj);
                        $.each(commentsArray, function (key, value) {
                            var commentObjKey = commentsObj[key];
                            var commentRowIndex = commentObjKey.rowIndex;
                            var commentColIndex = commentObjKey.colIndex;
                            if (maxDataRow < commentRowIndex) {
                                maxDataRow = commentRowIndex;
                            }
                            if (maxDataCol < commentColIndex) {
                                maxDataCol = commentColIndex;
                            }
                        });
                    }

                    if (innerObject.hasOwnProperty('tables')) {//for Tables
                        var tablesObj = innerObject.tables;
                        var tablesArray = Object.keys(tablesObj);
                        $.each(tablesArray, function (key, value) {
                            var tableObjKey = tablesObj[key];
                            var tableStartRow = tableObjKey.row,
                                tableStartCol = tableObjKey.col,
                                /*
                                 * Subtracted 1 as tableEndRow/tableEndCol initial index count starts with 1
                                 * and maxDataRow/maxDataCol initial index count starts with 0
                                 * which leads to wrong condition check and assigning wrong value to maxDataRow/maxDataCol
                                 */
                                tableEndRow = (tableStartRow + tableObjKey.rowCount)-1,
                                tableEndCol = (tableStartCol + tableObjKey.colCount)-1;
                            if (maxDataRow < tableEndRow) {
                                maxDataRow = tableEndRow;
                            }
                            if (maxDataCol < tableEndCol) {
                                maxDataCol = tableEndCol;
                            }
                        });
                    }

                    ++maxDataCol;
                    ++maxDataRow;
                    colArr.push(maxDataCol);
                    rowArr.push(maxDataRow);

                    if (options.type == "view" || ( options.type == "edit" && !options.liveEditingStatus) ) {
                      spread.getSheetFromName(key).setColumnCount(maxDataCol);
                      spread.getSheetFromName(key).setRowCount(maxDataRow);
                    }else{
                      // to do fix so that unwanted data is removed
                      // console.log("col count " + maxDataCol);
                      // console.log("row count " + maxDataRow);
                    }
                }
            }
            if (colArr.length > 0) { //When table has data then show max col and row among all the sheets
                maxDataCol = Math.max.apply(null, colArr);
                maxDataRow = Math.max.apply(null, rowArr);
            }else{ // When table is empty then show single cell by default.
               maxDataCol = 1;
               maxDataRow = 1;
            }
            if (options.type == "view") {
                this.adjustViewPortSize(true);
                this.bindScrollbarEvent();
            }
            spread.isPaintSuspended(false);
        };
        this.bindScrollbarEvent = function () {
            spread.bind($.wijmo.wijspread.Events.ColumnWidthChanged, function (event, data) {
                $self.adjustViewPortSize(true);
                $self.hideScrollbarIfNotRequired();
            });
            spread.bind($.wijmo.wijspread.Events.RowHeightChanging, function (event, data) {
                $self.adjustViewPortSize(true);
                $self.hideScrollbarIfNotRequired();
            });
        };
        this.adjustViewPortSize = function (addDummyRows) {  //Auto adjust of Excellentable's height and width based on data filled in it.
            var sheetCount = spread.getSheetCount(), maxSpreadHeight = 0, maxSpreadWidth = 0;
            var verticalScrollbarWidth = $self.find(".scrollbar-vertical").closest(".scroll-container").width();
            var sheetRowHeightsArray = {};
            $self.find(".eui-view-spread-container,"+options[options.type].divId).removeAttr("style");
            for (var sheetCnt = sheetCount - 1; sheetCnt >= 0; sheetCnt--) {
                activeSheet = spread.getSheet(sheetCnt);

                var rowRangeGroupWidth = activeSheet.rowRangeGroup.getMaxLevel() !== null ? 20 : 0;
                var rowLevelCountRangeGroup = activeSheet.rowRangeGroup.getMaxLevel();
                if (rowLevelCountRangeGroup > -1) {
                    rowRangeGroupWidth = rowRangeGroupWidth + 19 * (rowLevelCountRangeGroup + 2);
                }

                var colRangeGroupHeight = activeSheet.colRangeGroup.getMaxLevel() !== null ? 28 : 0;
                var colLevelCountRangeGroup = activeSheet.colRangeGroup.getMaxLevel();
                if (colLevelCountRangeGroup > -1) {
                    colRangeGroupHeight = colRangeGroupHeight + 19 * (colLevelCountRangeGroup + 2);
                }

                var curwidth = rowRangeGroupWidth - verticalScrollbarWidth,curheight = colRangeGroupHeight;
                if (activeSheet.getRowHeaderVisible(true)) {
                    curwidth = curwidth + activeSheet.getColumnWidth(0, $.wijmo.wijspread.SheetArea.rowHeader);
                }
                if (activeSheet.getColumnHeaderVisible(true)) {
                    curheight = curheight + activeSheet.getRowHeight(0, $.wijmo.wijspread.SheetArea.colHeader);
                }

                for (var c = 0; c <= maxDataCol || c <= maxDataRow; c++) {
                    if (c < maxDataCol) {
                        curwidth = curwidth + activeSheet.getColumnWidth(c);
                    }
                    if (c < maxDataRow) {
                        curheight = curheight + activeSheet.getRowHeight(c);
                    }
                }
                sheetRowHeightsArray[activeSheet.getName()] = { "height": curheight, "width": curwidth };
                if (maxSpreadHeight < curheight) {
                    maxSpreadHeight = curheight;
                }
                if (maxSpreadWidth < curwidth) {
                    maxSpreadWidth = curwidth;
                }
            }
            if(maxSpreadWidth < $self.width()){
                $self.find(".eui-view-spread-container").css({"width": maxSpreadWidth});
            }

            if(maxSpreadHeight < options.viewportMaxHeight){
                $self.find(options[options.type].divId).css({"height": maxSpreadHeight});
                spread.showVerticalScrollbar(false);
            }else{
                $self.find(options[options.type].divId).css({"height": options.viewportMaxHeight});
                spread.showVerticalScrollbar(true);
            }

            maxSpreadHeight = maxSpreadHeight > options.viewportMaxHeight ? options.viewportMaxHeight : maxSpreadHeight;
            if(addDummyRows){
                $self.addDummyRowsColumns(sheetRowHeightsArray, maxSpreadHeight);
            }
            $self.hideScrollbarIfNotRequired();
        };
        /*
         * Ref: EXC-2607
         * This method is used to add extra rows & columns on view Mode of Excellentable to cover the Viewport area.
         */
        this.addDummyRowsColumns = function(sheetSizeArray,spreadHeight){
            var sheetCount = spread.getSheetCount();
            var spreadWidth = $self.find(".eui-view-spread-container").width();
            for (var sheetCnt = sheetCount - 1; sheetCnt >= 0; sheetCnt--) {
                var sheet = spread.getSheet(sheetCnt);

                var needToIncreaseHeightBy = spreadHeight - sheetSizeArray[sheet.getName()].height;
                if(needToIncreaseHeightBy > 0){
                    var rowFactor = needToIncreaseHeightBy / sheet.defaults.rowHeight;
                    var rowsNeeded = parseInt(rowFactor);
                    var remainingBlankSpace = ((rowFactor-rowsNeeded) * sheet.defaults.rowHeight)
                    sheet.addRows(sheet.getRowCount(),rowsNeeded);
                    var lastRowNo = sheet.getRowCount()-1;
                    var lastRowHeight = sheet.getRowHeight(lastRowNo);
                    sheet.setRowHeight(lastRowNo , lastRowHeight+remainingBlankSpace);
                }

                var needToIncreaseWidhtBy = spreadWidth - sheetSizeArray[sheet.getName()].width;
                if(needToIncreaseWidhtBy > 0){
                    var number = needToIncreaseWidhtBy / sheet.defaults.colWidth;
                    var noOfColumnsNeeded = parseInt(number);
                    var remainingBlankSpace = ((number-noOfColumnsNeeded)*sheet.defaults.colWidth);
                    sheet.addColumns(sheet.getColumnCount(),noOfColumnsNeeded);
                    var lastColumnNo = sheet.getColumnCount()-1;
                    var lastColumnWidth = sheet.getColumnWidth(lastColumnNo);
                    sheet.setColumnWidth( lastColumnNo , lastColumnWidth+remainingBlankSpace);
                }
                spread.scrollbarMaxAlign(true);
                spread.scrollbarShowMax(true);
                spread.refresh();
            }
        };
        this.autoAdjustRowHeightViewMode = function () {
            if (maxDataCol === 1) { // if only one column is present in the view mode
                var row, totalRowCount = activeSheet.getRowCount();
                for (row = 0; row < totalRowCount; row++) {
                    var cellWordWrap = activeSheet.getCell(row, 0, $.wijmo.wijspread.SheetArea.viewport).wordWrap();
                    if (cellWordWrap === true) {
                        activeSheet.autoFitRow(row);
                    }
                }
            }
        };
        this.adjustEditPortSize = function () {
            var curwidth = 0, ColumnCount = activeSheet.getColumnCount(), defaultRowHeaderColWidth = activeSheet._defaultRowHeaderColWidth;
            var canvasWidth = $self.find("#vp").width() - defaultRowHeaderColWidth;
            for (var c = 0; c < ColumnCount; c++) {
                curwidth = curwidth + activeSheet.getColumnWidth(c);

            }
            if (curwidth < canvasWidth) {
                var differenceInWidth = (canvasWidth - curwidth), tempWidth = differenceInWidth / ColumnCount;
                var tempColCount = activeSheet.getColumnCount();
                for (var i = 0; i < tempColCount; i++) {
                    var ColWidth = activeSheet.getColumnWidth(i);
                    var newwidth = ColWidth + tempWidth;
                    activeSheet.setColumnWidth(i, newwidth);
                }

            }
            spread.scrollbarMaxAlign(true);
            spread.scrollbarShowMax(true);
            spread.refresh();
        }
          this.hideScrollbarIfNotRequired = function () {
            spread.showHorizontalScrollbar(true);
            var $horizontalScrollBar = $self.find('.scrollbar-horizontal');
            if ($horizontalScrollBar.closest(".scroll-bar").width() + 15 > $horizontalScrollBar.closest(".scrollbar-wrapper").width()) {
                spread.showHorizontalScrollbar(false);
            }else{
                spread.showHorizontalScrollbar(true);
            }
            spread.refresh();
        };
        this.applyShowHideOnMouserHover = function () {
            hide();
            $self.find(options[options.type].divId + " table , " + options[options.type].divId + " canvas , " + options[options.type].divId + " .floatingobject-content-container").on("mouseover.applyshowHideOnMouserHover", function (e) {
                show();
            }).on("mouseout.applyshowHideOnMouserHover", function (e) {
                if (jQuery('.wijspread-popup:visible').length == 0)
                    hide();
            });

            function show() {
                spread.showHorizontalScrollbar(true).showVerticalScrollbar(true);
                activeSheet = spread.getActiveSheet();
                activeSheet.setColumnHeaderVisible(true);
                activeSheet.setRowHeaderVisible(true);
            }
            function hide() {
                spread.showHorizontalScrollbar(false).showVerticalScrollbar(false);
                activeSheet.clearSelection();
                activeSheet.setColumnHeaderVisible(false);
                activeSheet.setRowHeaderVisible(false);
            }
        };
        this.removeShowHideOnMouserHover = function () {
            jQuery(options.mainDivId).unbind("mouseover.applyshowHideOnMouserHover mouseout.applyshowHideOnMouserHover");
        };
        this.adjustMinimumRowsAndColumnsAtEditMode = function () {
            var sheetsCount = spread.getSheetCount();
            for (var i = 0; i < sheetsCount; i++) {
                var currentSheet = spread.getSheet(i);
                currentSheet.isPaintSuspended(true);
                if (currentSheet.getRowCount() < options.defaultRowCount) {
                    currentSheet.addRows(currentSheet.getRowCount(), options.defaultRowCount - currentSheet.getRowCount())
                }
                if (currentSheet.getColumnCount() < options.defaultColumnCount) {
                    currentSheet.addColumns(currentSheet.getColumnCount(), options.defaultColumnCount - currentSheet.getColumnCount());
                }
                currentSheet.isPaintSuspended(false);
            }
        };

        this.onCancel = function (excellentableDialogId) { //on dialog cancel
            window.closingExcellentableFlag = true;
        	$self.faviconSetter(options.confFavicon);
        	$self.enableConfluenceKeyboardShortcuts();
            if (options.reloadOnClose || options.liveEditingStatus) {
                $self.refresh();
            }

            jQuery('#page').removeAttr('style');
            jQuery("#" + options.excellentableDialogId).addClass('hidden');
            jQuery('.eui-menu-dialog').remove();
            jQuery(excellentableDialogId).remove();
            jQuery('.aui-blanket').remove();
            jQuery('.gcStringWidthSpanStyle').remove();

            if(options.liveEditingStatus){
              excellentableLiveObject.closeExcellentableConnection();
            }

            //Closing any open comment (note) dialogs
            if (clicknote) {
                clicknote.close();
            }

        };

        this.save = function () {
            var announceSavingMessage = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.save.announce.message");
            var announceSavingLongerDurationMessage = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.save.longer.announce.message");
            // Show message while saving and show another message indicating that it takes more than 3 seconds to save.
            $.announce.info({"message": [announceSavingMessage, announceSavingLongerDurationMessage]});

            //EXC-4490 Check if the Spread object has any base64 images. If yes, those need to be converted to confluence attachments with URLS
            //AGAIN, before converting to confluence attachments we need to check if the pageID is existing or not!
            $self.shrinkBase64ImagesToAttachmentURLs();

            activeSheet = spread.getActiveSheet();
            activeSheet.endEdit();
            /*As a resolution to the problem:After saving table the cellTextAlignment() gets called
             which takes few mili seconds. becuase of that if user saves and close the ET app
             it complains the data has been changed in the table, wrote functionality within setTimeout.
             (refer:EXC-982)*/
            setTimeout(function () {
                var metaData = spread.toJSON();

                $self.getMinimalCount(JSON.stringify(metaData));
                metaData = spread.toJSON();
                initialData = JSON.stringify(metaData);
                activeSheet.isPaintSuspended(true);
                $self.adjustMinimumRowsAndColumnsAtEditMode(); //Add extra Rows & columns if required.
                activeSheet.isPaintSuspended(false);
                // GZip the content
                var gZippedInitialData = pako.gzip(initialData);
                var gzippedInitialBlob = new Blob([gZippedInitialData], {type: "application/json; charset=x-user-defined-binary"});

                var response = $self.ExcellentableDBOperations({operation: "saveGZip", ID: options.excellentableId, metaData: gzippedInitialBlob, errorMsgFade: false});
                response.success(function (data) {
                    $self.ExcellentableNotification({title: AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.save.success.message")}).showSuccessMsg();

                    if (!options.liveEditingStatus) {
                        $.event.trigger({
                            type: "updateVersionNumber",
                            msg: {"versionNumber": data.versionNumber}
                        });
                    }else{
                        //last saved spread json for live editing set
                        excellentableLiveObject.setLastSavedSpreadJSON(true);
                        excellentableLiveObject.annouceSaveOperation();
                    }
                    $.announce.hide();
                    unsavedContentManager.clearUnsavedContentFlag();
                });
                jQuery('#euiSpreadContentChanged').val("false");
                options.reloadOnClose = true;
            }, 10);


        };
        this.shrinkBase64ImagesToAttachmentURLs = function () {
            //Only convert images to attachments if the pageId exists
            if (AJS.params.pageId != 0) {
                for (var i = 0 ; i < spread.sheets.length ; i++) {
                    var sheet = spread.getSheet(i);
                    var pictures = sheet.getPictures();
                    for (var j = 0 ; j < pictures.length; j++) {
                        var src = pictures[j]["src"]();
                        if (src.startsWith("data:image")) {
                            var file = dataURLtoFile(src, sheet.getName()+"_"+pictures[j]._name);
                            var formData = new FormData();
                            formData.append("file",file);
                            var pictureUrl = attachToConfluencePage(formData);
                            if (pictureUrl) {
                                pictures[j].src(pictureUrl);
                            }
                        }
                    }
                }
            }
        };
        this.refresh = function () { //on excellentable save on "view page" mode, reflect the same changes on respected excellentable view.
            var $tempDiv = jQuery(options["view"].mainDivId + "[" + options.excellentableIdAttr + "=" + options.excellentableId + "]");
            if ($tempDiv.length > 0) {
                $tempDiv.find(options["view"].divId).empty();
                this.unbindEvents($tempDiv);
                $tempDiv.Excellentable({excellentableId: options.excellentableId});
            }
            options.reloadOnClose = false;
        };
        this.applyGlobalFilter = function (isFullscreen, searchTerm) {
                $self.ExcellentableSearch({'spread': spread, fullscreen: isFullscreen, searchTerm: searchTerm});
        };
        this.removeGlobalFilter = function () {
            $self.find(options.globalFilterDivId).hide();
        };

        var waitForFinalEvent = (function () {

        	var timers = {};

        	return function (callback, ms, uniqueId) {
			    if (!uniqueId) {
			      uniqueId = "Don't call this twice without a uniqueId";
			    }
			    if (timers[uniqueId]) {
			      clearTimeout (timers[uniqueId]);
			    }
			    timers[uniqueId] = setTimeout(callback, ms);
		  };
		})();

        this.editSpreadHeight = function(t){
            var windowHeight = jQuery(window).height(),
            scrollContainerHeight = 12,
            newSpreadHeight,
            menubarHeight = 30,
            formulabar = jQuery("#euiFormulaBar").height()
            menubarList = jQuery(t).height();

            newSpreadHeight = windowHeight - (menubarHeight + menubarList + formulabar + scrollContainerHeight);
            
            jQuery("#euiControlPanel").css({"height": newSpreadHeight});
            jQuery(".eui-edit-spread").height(newSpreadHeight);
            if(options.fullscreen){
               jQuery(".eui-fullscreen .eui-view-spread").height(newSpreadHeight); 
            }
            
            spread.refresh();
        };
        this.checkDiff = function () {  //Check for any changes in the table and return true or false value accordingly
            return unsavedContentManager.hasUnsavedContentFlag();
        };

         //Function to remove the unwanted diff elements of json.
        this.removeDiffElements = function (jsonDataString, arrayOfKeysToSkip) {
            return JSON.parse(jsonDataString, function (key, value) {
                if (jQuery.inArray(key, arrayOfKeysToSkip) == -1) {
                    return value;
                }
            });
        };
        this.bindEvents = function () { //bindEvents of Excellentable with custom namespace.
            this.off(".excellentable"); //Unbind existing events.
            //EXC-2955 :Updated this method in this way as previous way was not recognizing euiDropdownSave selector properly
            jQuery("#euiDropdownSave").on("click.excellentable", function () {
                //Ref : EXC-4698, We now allow the user to save as many times as needed, without checking any differences.
                // We will need to fine tune this later on and add the deleted code back again after making sure its able
                //to check differences properly. Also, we are ensuring the fix done in EXC-4321 related this code block works
                // in accordance with this new behavior.
                if(!$('#euiVersionMask').hasClass('is-active')) {
                    $self.save();
                }

            });

            jQuery('body').on('mouseover.excellentable',function() {
                if (hovernote && !leftViewTable){
                    hovernote.close();
                    leftViewTable = true;
                }
            });

            jQuery('.eui-exc-container #vp_vp').on('mouseover.excellentable', function(e) {
                if (leftViewTable) {
                    var tableId = $(e.target).closest('.eui-exc-container').attr('id');

                    if (tableId === currentTableId) {
                      hovernote.close();
                      spread.repaint();
                      e.stopPropagation();
                      leftViewTable = false;
                    }

                    currentTableId = tableId;
                }
            });

            this.on("click.excellentable", "#euiEditExcellentable", function () {
                $self.ExcellentableDialog({excellentableId: options.excellentableId,contentEntityId:$self.attr('content-entity-id')});
            });

            this.on("click.excellentable", "#euiFullscreenExcellentable", function () {
                var searchTerm = $('.eui-live-search').text();
                $self.ExcellentableDialog({excellentableId: options.excellentableId, contentEntityId:$self.attr('content-entity-id'), type:'view', fullscreen:true, searchTerm: searchTerm});
            });

            this.on("click.excellentable", "#euiDialog .eui-close", function () {
                $self.onCancel($self.selector);
            });

            this.on("click.excellentable", "#euiDialog #euiFullscreenEditExcellentable", function () {
                $self.onCancel($self.selector);
                $('#euiEditExcellentable').trigger('click.excellentable');
            });

            jQuery("#euiDialogCloseButton,#euiDropdownExit").bind('click.excellentable', function () {
                $self.ExcellentableNotification().removeMsg();
                var hasChanges = $self.checkDiff({ignoreActiveSheetChange : true});
                if (hasChanges) {
                    var options = {title: AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.title.confirmation"),
                        msg: AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.message.unsave.changes"),
                        okText: AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.exit"),
                        ID: "euiUnsavedChangesDialog"};

                    var popup = jQuery().getDialog(options);
                    popup.show();
                    popup.setOkAction(function () {
                        popup.hide();
                        $self.onCancel($self.selector);
                    });
                } else {
                    $self.onCancel($self.selector);
                }
            });

            jQuery(document).unbind(".excellentable");
            $self.on("click.excellentable", ".exportToExcel", function (e, exportType) {
                $self.export(e,exportType);
            });

            jQuery(document).on("click.excellentable", ".eui-export-view-mode li", function (e,exportType) {
                /* In view mode, we need to ensure that the spread value is set to the correct object that we want to export.
                   This is really important, particularly when there are multiple excellentable macros on a single confluence page
                   Ref : EXC-4347
                */
                spread = jQuery(this).parents(".eui-exc-container")
                                            .find('.eui-view-spread-container')
                                            .find('.eui-view-spread')
                                            .wijspread("spread");
                
            	$self.export(e, $(this).data("value"));
            });

            jQuery(document).on("click.excellentable", '#confirmDialog #euiSaveChangesBeforeExportButton', function (e) {
                var data, exportType =$self.find('#euiExportType').val();
                var newSpread = jQuery(""+options["edit"].divId).wijspread('spread');
                var activeSheet = newSpread.getActiveSheet();
                exportJsonData = newSpread.toJSON();
                exportCsvData = activeSheet.getCsv(0,0,activeSheet.getRowCount(),activeSheet.getColumnCount(),"\n",",");
                $self.ExcellentableDBOperations({operation: "update", ID: options.excellentableId, metaData: exportJsonData});
                $self.find('#euiSpreadContentChanged').val("false");
                if (exportType === "xlsx") {
                    data = JSON.stringify(exportJsonData);
                } else if (exportType === "csv") {
                    data = exportCsvData;
                } else if (exportType === "json") {
                    data = JSON.stringify(exportJsonData);
                }
                var exportReq = new window.Excellentable.ExportRequest(exportType, data, exportFileName);
                exportReq.makeCall();
            });
            jQuery(document).on("click.excellentable", '#confirmDialog #euiUnsaveChangesBeforeExportButton', function (e) {
                var data, exportType =$self.find('#euiExportType').val();
                exportJsonData = spread.toJSON();
                if (exportType === "xlsx") {
                    data = JSON.stringify(exportJsonData);
                } else if (exportType === "csv") {
                    data = exportCsvData;
                } else if (exportType === "json") {
                    data = JSON.stringify(exportJsonData);
                }
                var exportReq = new window.Excellentable.ExportRequest(exportType, data, exportFileName);
                exportReq.makeCall();
            });
            jQuery(document).on("paste.excellentable", function (e) {
                activeSheet = spread.getActiveSheet();
                if (e.originalEvent.clipboardData && e.originalEvent.clipboardData.getData) {
                    var clipText = e.originalEvent.clipboardData.getData('text/plain');
                    var rc = clipText.split("\n"), copiedRowCount = rc.length - 1, cc = rc[0].split("\t"), copiedColCount = cc.length;
                    var spread_rowcount = activeSheet.getRowCount($.wijmo.wijspread.SheetArea.viewport), spread_colcount = activeSheet.getColumnCount($.wijmo.wijspread.SheetArea.viewport);
                    var activeRowIndex = activeSheet.getActiveRowIndex(), activecolIndex = activeSheet.getActiveColumnIndex();
                    var leftRowcount = spread_rowcount - activeRowIndex, leftColcount = spread_colcount - activecolIndex;
                    if (copiedRowCount > leftRowcount && copiedColCount > leftColcount) {
                        activeSheet.addRows(spread_rowcount, copiedRowCount - leftRowcount);
                        activeSheet.addColumns(spread_colcount, copiedColCount - leftColcount);
                    } else if (copiedColCount > leftColcount) {
                        activeSheet.addColumns(spread_colcount, copiedColCount - leftColcount);
                    } else if (copiedRowCount > leftRowcount) {
                        activeSheet.addRows(spread_rowcount, copiedRowCount - leftRowcount);
                    }
                }
            });
            spread.bind($.wijmo.wijspread.Events.SheetTabClick, function (sender, args) {
                //On Add New Sheet Button CLicked
                if (args.sheet === null && args.sheetName === null) {
                    setTimeout(function(){
                      $self.initBlankSheet();
                      ExcellentableTriggerCustomValueChangeEvent(spread,'AddNewSheet');
                    },1);
                }
            });

            spread.bind($.wijmo.wijspread.Events.DragFillBlock, function (sender, args) {
                var sheet = args.sheet,
                    selection = args.fillRange;

                args.cancel = protectedSelectionCheck(sheet, selection);
                sheet.endEdit();

                return !args.cancel;
            });

            spread.bind($.wijmo.wijspread.Events.DragDropBlock, function (sender, args) {
                var sheet = args.sheet;

                args.cancel = protectedSelectionCheck(sheet, selection);
                sheet.endEdit();

                return !args.cancel;
            });

            spread.bind($.wijmo.wijspread.Events.ActiveSheetChanged, function (sender, args) {
                /*Trigger enterCell event when we change the sheet to fetch all the formatting from the selected cell of the sheet
                  and populate it in the menu bar*/
                activeSheet = args.newSheet;
                args.newSheet.triggerEnterCell({
                    sheet: activeSheet, row: activeSheet.getActiveRowIndex(), col: activeSheet.getActiveColumnIndex()
                });
            });
            //Auto adjust the row height while changing the column width,on cell leave and enter the cell
            spread.bind($.wijmo.wijspread.Events.ColumnWidthChanged, function (e, cellInfo) {
                $self.autoAdjustColWidth(e, cellInfo);
            });
            spread.bind(GcSpread.Sheets.Events.EditEnded, function (sender, args) {
                activeSheet = spread.getActiveSheet();
                var formula = activeSheet.getFormula(activeSheet.getActiveRowIndex(), activeSheet.getActiveColumnIndex());
                if (formula != null) {
                    var parserContext = new GcSpread.Sheets.Calc.ParserContext(new GcSpread.Sheets.Calc.CalcSource(activeSheet.getCalcService()), false, activeSheet.getActiveRowIndex(), activeSheet.getActiveColumnIndex());
                    var expression = parser.parse(formula, parserContext);
                    if (expression instanceof GcSpread.Sheets.Calc.Expressions.Expression) {
                        $self.eachRecursive(expression, parserContext);
                    }
                }

                //checks if user entered a newline character and modifies the wordwrap to make it display correctly
                if(args.editingText !== null){
                    var newline = args.editingText.search(/\n/);
                    if(newline !== -1){
                        activeSheet.isPaintSuspended(true);
                        var activeCell = activeSheet.getCell(args.row, args.col);
                        activeCell.wordWrap(true);
                        activeSheet.autoFitRow(args.row);
                        activeSheet.isPaintSuspended(false);
                    }
                }
                
                
            });

            spread.bind(GcSpread.Sheets.Events.CellClick, function (sender, args) {
                var sheet = spread.getActiveSheet();

                //If sheet is not protected and comment is already present, then show the Add comment dialog with the
                // ability to edit, else exit out!
                if (sheet.getIsProtected()) {
                  return false;
                }
                
                var cmd = {};
                cmd.commandName = "comment";
                if (clicknote){
                  clicknote.close();
                  clicknote = null;
                }
                if (hovernote){
                    hovernote.close();
                }
                //Show the edit comment dialog ONLY if comment is already present
                var comment = sheet.getComment(sheet.getActiveRowIndex(), sheet.getActiveColumnIndex());
                if (comment) {
                    noteFlag.body = AJS.$('#eui-comment-note').html().replace(/\r\n|\r|\n/g,"<br />");
                    clicknote = AJS.flag(noteFlag);
                    var commentDialogFlagNoteButton = AJS.$("#edit-note-thru-flag");
                    commentDialogFlagNoteButton.on('click', function(e) {
                        e.preventDefault();
                        if (clicknote){
                            clicknote.close();
                            clicknote = null;
                        }
                    });
 
                    // Resolve the note by clicking on the resolve button shown by clicking on the cell having note
                    var commentDialogFlagResolveButton = AJS.$("#dialog-resolve-button");
                    commentDialogFlagResolveButton.on('click', function(e) {
                        e.preventDefault();
                        var spread = jQuery(spreadId).wijspread("spread");
                        var sheet = spread.getActiveSheet();
                        sheet.isPaintSuspended(true);
                        var sel = sheet.getSelections()[0];
                        if (sel) {
                            var cr = getActualRange(sel, sheet.getRowCount(), sheet.getColumnCount());
                            sheet.setComment(cr.row, cr.col, null);
                        }
                        sheet.isPaintSuspended(false);
                        clicknote.close();
                    });
                }
            });
            /* Some of the Date/Time related formulas in SpreadJS are not showing result same as google spreadsheet,
             * below event is added to show the result of date/time formula in a proper format.
             * Ref: EXC-219,EXC-860
             */
            spread.bind($.wijmo.wijspread.Events.EditEnd, function (e, cellInfo) {
                //time and date formula
                var sheet = spread.getActiveSheet();
                var edittext = cellInfo.editingText;
                if (edittext !== null) {
                    var splitEditText = edittext.split("("), formulaName = splitEditText[0].substring(1).toLowerCase();
                    var activeCell = sheet.getCell(cellInfo.row, cellInfo.col), cellFormatter = activeCell.formatter();
                    var dtRegex = new RegExp(/^(?!=^[a-zA-Z]+$)(\d{1,2})\/(\d{1,2})\/(\d{2,4})/);
                    var abbDtRegex = new RegExp(/^(?!=^[a-zA-Z]+$)((Jan(uary)?|Feb(ruary)?|Mar(ch)?|Apr(il)?|May|Jun(e)?|Jul(y)?|Aug(ust)?|Sep(tember)?|Oct(ober)?|Nov(ember)?|Dec(ember)?))\/(\d{1,2})\/(\d{2,4})/);

                    //To check horizontal align is already set to activeCell Ref: EXC-2327
                    var alignActiveCell = function(position) {
                       if (activeCell.hAlign() != undefined) {
                         return;
                      }

                      return activeCell.hAlign($.wijmo.wijspread.HorizontalAlign[position]);
                    };

                    sheet.isPaintSuspended(true);
                    if (formulaName === "time") {
                      activeCell.formatter("hh:mm:ss AM/PM");
                      alignActiveCell('right');
                    }  else if (formulaName === "date" || formulaName === "edate" || formulaName === "today" || dtRegex.test(edittext) || abbDtRegex.test(edittext)) {
                      activeCell.formatter("M/d/yyyy");
                      alignActiveCell('right');
                    } else if (formulaName === "datedif" || formulaName === "datevalue") {
                        setTimeout(function () {
                            activeCell.formatter("");
                            alignActiveCell('right');
                        }, 0.5);
                    } else {
                        if (cellFormatter === "M/d/yyyy" || cellFormatter === "hh:mm:ss AM/PM") {
                            activeCell.formatter("");
                            setTimeout(function () {
                                $self.cellTextAlignment(cellInfo, sheet);
                            }, 1);
                        } else {
                            setTimeout(function () {
                                $self.cellTextAlignment(cellInfo, sheet);
                            }, 1);
                        }
                    }
                    sheet.isPaintSuspended(false);
                }
                setTimeout(function () {
                    try {
                      $self.autoAdjustRowHeightOnCellLeave(sheet, cellInfo);
                    } catch (e) {
                        /*
                            Workaround:
                            When the borders are updated by another user on live editing mode, the "getDataModel" error gets thrown -
                            it happens because, apparently, excellentable doesn't know how to perform certain alignment operations when dealing with ranges.
                            There's not really any way of preventing this error which doesn't involve altering a good portion of how the live editing feature was done or modifying spreadjs.
                            The error should be ignored since it doesn't block or affect the application in any way.
                            And, also, because border operations do not change the contents of the selected cells, they don't need to be realigned in the first place. */
                            return;
                    }
                }, 10);
            });
            //Auto adjust the height of the Insert table dialog
            jQuery("#euiTableStyles").on('click', function () {
                var tableDialogHeight = jQuery("#euiTableDialog").height(), dropDownHeight = jQuery("#euiTableStyles_child").height();
                var totalDialogHeight = tableDialogHeight + dropDownHeight;
                if (tableDialogHeight < totalDialogHeight) {
                    jQuery("#euiTableDialog").css({"height": 546});
                }
            }).blur(function(){
                jQuery("#euiTableDialog").css({"height": 'auto'});
            });
            jQuery("select#euiTableStyles").change(function () {
                jQuery("#euiTableDialog").css({"height": 'auto'});
            });
            this.on("click.excellentable", ".eui-help", function () {
                window.open("https://nebula.addteq.com/display/EXC/Excellentable", '_blank');
            });

            jQuery(document).on("click.excellentable", options[options.type].mainDivId+" .eui-export-json", function (e) {
               $self.export(e, "json");
            });
            jQuery("#euiMenuBar .tabs-menu .menu-item").click(function () {
            	var t = jQuery(this).find("a").attr("href");
            	waitForFinalEvent($self.editSpreadHeight(t), 1000, new Date().getTime());

            });

            var resizeTimer;

            $(window).on('resize', function(e) {

              clearTimeout(resizeTimer);
              resizeTimer = setTimeout(function() {

            	  var t = jQuery("#euiMenuBar .tabs-menu .menu-item.active-tab").find("a").attr("href");
            	  $self.editSpreadHeight(t)
              }, 500);

            });

            //set focus to the sheet when any format button is pressed in the menubar
            jQuery(".eui-formatting-dialog").on("dialogclose", function () {
                spread.focus();
            });
            jQuery(document).on("click", "#euiMenuBar button:not(.addlink,.comment),.eui-dropdown a,#euiSpreadContextMenu aui-item-link", function () {
                spread.focus();
            });
            // Fix the Confluence alert while saving Excellentable in the new UI. Ref: EXC-3171
            jQuery(document).on("click", '#euiMenuBar a[href="#"]', function (e) {
                e.preventDefault();
            });

            jQuery(document).on('contextmenu', '.floatingobject-container', function(e){
                e.preventDefault();
            });
        };
        this.unbindEvents = function ($tempDiv) { //Unbind all events which are binded in above bindEvent method.
            $tempDiv.off('.excellentable');
        };
        this.cellTextAlignment = function (cellInfo, sheet) {
            var activeCell = sheet.getCell(cellInfo.row, cellInfo.col), celltext = activeCell.text();
            if (!jQuery.isNumeric(celltext) && !(activeCell.formatter() === "M/d/yyyy")) {
                if (activeCell.formula() || jQuery.isNumeric(cellInfo.editingText)) {
                    activeCell.hAlign($.wijmo.wijspread.HorizontalAlign.right);
                } else {
                    activeCell.hAlign($.wijmo.wijspread.HorizontalAlign.left);
                }
            } else {
                activeCell.hAlign($.wijmo.wijspread.HorizontalAlign.right);
            }
        };
        this.autoAdjustRowHeightOnCellLeave = function (sheet, cellInfo) {
            sheet.isPaintSuspended(true);
            var activeRowIndex = cellInfo.row;
            var activeCell = sheet.getCell(cellInfo.row, cellInfo.col);
            if (activeCell.wordWrap() === true) {
                sheet.autoFitRow(activeRowIndex);
                var rowHeight = sheet.getRow(activeRowIndex).height(), extraHeight = 20;
                sheet.getRow(activeRowIndex).height(rowHeight + extraHeight);
            }
            sheet.isPaintSuspended(false);
        };
        this.export = function (e,exportType) {
            e.preventDefault();
            if(spread == undefined){
                spread = $self.find('.eui-view-spread').wijspread("spread");
            }
            var data;
            exportType = (typeof exportType == "undefined") ?  "xlsx" : exportType;
            $self.find('#euiExportType').val(exportType);
            var somethingChanged = $self.find('#euiSpreadContentChanged').val();

            if (options.type == "edit" && somethingChanged == "true") {
                var excellentableSaveChangesDialogObj = jQuery('body').ExcellentableCustomDialog({"dialogId": "confirmDialog",
                                    "msg":AJS.I18n.getText('com.addteq.confluence.plugin.excellentable.export.dialog.save.changes.message')}).init();
                excellentableSaveChangesDialogObj.show();
                return false;
            } else {
                if (exportType == "xlsx") {
                    data = JSON.stringify(spread.toJSON());
                } else if (exportType == "csv") {
                    var activeSheet = spread.getActiveSheet();
                    data = activeSheet.getCsv(0, 0, activeSheet.getRowCount(), activeSheet.getColumnCount(), "\n", ",");
                } else if (exportType == "json" || exportType == "html" ) {
                    data = JSON.stringify(spread.toJSON());
                }
                var exportReq = new window.Excellentable.ExportRequest(exportType, data, exportFileName);
                exportReq.makeCall();
            }
        };

        this.getSpreadObject = function () {  //To get spreadObject for Qunit
            return spread;
        };
        this.getColumnCount = function () {
            return columnCount;
        };
        this.getRowCount = function () {
            return rowCount;
        };

        this.dragEffect = function () {
           jQuery("#"+options.excellentableDialogId).on("dragenter", function (event) {
                if(jQuery(".eui-menu-push-right.is-active").length > 0) return; //disable drag event on version history dialog.
                if (jQuery('.eui-dragndrop-overlay').length === 0) {
                    jQuery(this).append('<div class="eui-dragndrop-overlay"></div>');
                }
            }).on("dragleave", function (event) {
                var $target = jQuery(event.target);
                if ($target.attr("class") === "eui-dragndrop-overlay") {
                    jQuery(".eui-dragndrop-overlay").remove();
                }
            }).on('drop', function (event) {
                if(jQuery(".eui-menu-push-right.is-active").length > 0) return; //disable drag event on version history dialog.
                var file = event.originalEvent.dataTransfer.files[0];
                if(file == null){  jQuery(".eui-dragndrop-overlay").remove(); return;}
                var fileExtension = file.name.split(".").pop();
                if (fileExtension === "xls" || fileExtension === "xlsx" || fileExtension === "csv" || fileExtension === "ods" || fileExtension === "html") {
                    findNoOfSheets(file,fileExtension);
                } else if(fileExtension === 'png' || fileExtension === 'jpeg' || fileExtension === 'jpg' || fileExtension === 'gif' || fileExtension === 'svg'){
                    event.preventDefault();
                    event.stopPropagation();

                    var reader = new FileReader();
                    reader.addEventListener("load", function () {
                        var activeSheet = spread.getActiveSheet();
                        var selRow = activeSheet.getActiveRowIndex() || 0;
                        var selCol = activeSheet.getActiveColumnIndex() || 0;

                        activeSheet.addPicture(file.name, reader.result, selRow, selCol).borderStyle("solid").borderWidth(0).borderRadius(3);
                    }, false);
                    reader.readAsDataURL(file);

                }else {
                    $self.ExcellentableNotification({
                        title: AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.import.error.filetype")
                    }).showErrorMsg();
                }
                jQuery(".eui-dragndrop-overlay").remove();
            });
        };
        this.init();
        return this;
    };

    $.fn.ExcellentableDialog = function (options) {
        $.fn.ExcellentableDialog.defaults = {
            excellentableDialogId: "euiDialog",
            excellentableIdAttr: "excellentable-id",
            type: "edit",
            fullscreen:false,
            searchTerm: '',
            events : {
            	CANCEL : "excellentable-cancel"
            },

        };

        var $self = this;  
        var $excellentableDialogObj;
        var excellentablePluginObj;
        var excellentableDialogHtml;

        options = $.extend({}, $.fn.ExcellentableDialog.defaults, options);
        var path = AJS.Meta.get("static-resource-url-prefix") + "/download/resources/Addteq.Excellentable:spreadJSResourcesV1/spreadJs.html";

        this.init = function () { //create dialog object
            var width = AJS.$(window).width() + 5,height = AJS.$(window).height();
            if(options.type === 'view') {
                excellentableDialogHtml = Confluence.Templates.Excellentable.FullscreenView({
                    excellentableDialogId : options.excellentableDialogId,
                    excellentableId : options.excellentableId,
                    contentEntityId : options.contentEntityId,
                    width : width,
                    height : height,
                    hasEditPermission : true,
                    contextPath : AJS.contextPath()
                }); 
            }else {
                excellentableDialogHtml = Confluence.Templates.Excellentable.EditETDialog({
                    excellentableDialogId : options.excellentableDialogId,
                    contentEntityId : options.contentEntityId,
                    width : width,
                    height : height
                });
            }
            
            jQuery('body').append(excellentableDialogHtml);
            jQuery("#" + options.excellentableDialogId).removeClass('hidden');
            jQuery('#page').css( "overflow", "hidden" );
            jQuery('.aui-blanket').remove();

            if(options.type === 'view'){
                jQuery("#" + options.excellentableDialogId).attr('aria-hidden', false);
            }
            $excellentableDialogObj = jQuery('#' + options.excellentableDialogId);
            $excellentableDialogObj.attr(options.excellentableIdAttr, options.excellentableId);
            
            var isMac = navigator.platform.toUpperCase().indexOf('MAC')!==-1;
            var editSpreadDialog = Confluence.Templates.Excellentable.Spread({
                contextPath:AJS.contextPath(),
                isMac: isMac
            });
            $excellentableDialogObj.find('#euiTextArea').append(editSpreadDialog);

            excellentablePluginObj = $excellentableDialogObj.Excellentable(options);
            AJS.tabs.setup();
        };

        this.init();
    };

    $.fn.ExcellentableSearch = function (options) {
        var spread, sheet, $self = this;
        $.fn.ExcellentableSearch.defaults = {
            searchTextBoxId: ".eui-live-search",
            fullscreen: false,
            searchTerm: ''
        };

        this.init = function () {
            $self = this;
            //remove hash from the url
            var url=window.location.href,hash = location.hash;
            history.pushState("","",url.replace(hash,""));

            options = $.extend({}, $.fn.ExcellentableSearch.defaults, options),
                    spread = $self.find('.eui-view-spread').wijspread("spread");
            sheet = spread.getActiveSheet();
            $self.find(options.searchTextBoxId).auiSelect2('data', ""); //Clear Global Filter TextBox.
            $self.bindEvent();

            if(options.searchTerm.trim().length !== 0) {
                $self.addSearchTerms(options.searchTerm.trim());
            }
        };
        this.getSearchCondition = function (searchOptions) {
            $.fn.defaulSearchOptions = {
                rowStart: 0,
                columnStart: 0,
                rowEnd: sheet.getRowCount(),
                columnEnd: sheet.getColumnCount()
            };
            searchOptions = $.extend({}, $.fn.defaulSearchOptions, searchOptions);
            var searchCondition = new GcSpread.Sheets.SearchCondition();
            searchCondition.searchString = searchOptions.searchString;
            searchCondition.searchOrder = GcSpread.Sheets.SearchOrder.ZOrder;
            searchCondition.searchTarget = GcSpread.Sheets.SearchFoundFlags.CellText;
            searchCondition.rowStart = searchOptions.rowStart;
            searchCondition.columnStart = searchOptions.columnStart;
            searchCondition.rowEnd = searchOptions.rowEnd;
            searchCondition.columnEnd = searchOptions.columnEnd;
            searchCondition.startSheetIndex = spread.getActiveSheetIndex();
            searchCondition.endSheetIndex = spread.getActiveSheetIndex();
            searchCondition.searchFlags = GcSpread.Sheets.SearchFlags.IgnoreCase | GcSpread.Sheets.SearchFlags.UseWildCards;
            return searchCondition;
        };
        this.globalSearch = function (findWhat) {
            findWhat = findWhat.split(" ");
            findWhat = findWhat.filter(function (v) { //remove blank elements from array
                return v !== ''
            });
            var condition = $self.getSearchCondition({searchString: findWhat[0]});
            var activeSheet = spread.getActiveSheet();
            $self.SearchAll(activeSheet, condition, findWhat);
        };
        this.SearchAll = function (sheet, condition, findWhat) {
            var results = [];
            var searchResult = sheet.search(condition);
            while (searchResult.searchFoundFlag !== $.wijmo.wijspread.SearchFoundFlags.None) {
                var found = true;
                for (var sr = 1; sr < findWhat.length; sr++) {
                    var con = $self.getSearchCondition({searchString: findWhat[sr],
                        rowStart: searchResult.foundRowIndex,
                        rowEnd: searchResult.foundRowIndex,
                        columnStart: 0
                    });
                    var innerResult = sheet.search(con);
                    if (innerResult.searchFoundFlag == $.wijmo.wijspread.SearchFoundFlags.None) {
                        found = false;
                        break;
                    }
                }
                if (found == true) {
                    results.push(searchResult.foundRowIndex);
                }
                var con = $self.getSearchCondition({searchString: condition.searchString,
                    rowStart: ++searchResult.foundRowIndex,
                    columnStart: 0
                });
                searchResult = sheet.search(con);
            }
            $self.showSearchResult(sheet, results, condition.searchString.length);
        };

        this.bindEvent = function () {
            var timer;
            var value;
            $self.on("keyup", "" + options.searchTextBoxId, function (e) {
                var keyCode = e.keyCode || e.which;
                // backspace , delete and function keys from F1 to F12
                var inValidKeyCode = (keyCode == 8) || (keyCode == 46) ||(keyCode == 9)||(keyCode == 16)|| (keyCode > 36 && keyCode < 41) || (keyCode > 111 && keyCode < 124);
                clearTimeout(timer);
                var searchString = $self.find(options.searchTextBoxId).text() + $self.find(options.searchTextBoxId + " input").val();
                // replacing extra space between the string by single space
                var actualSearchString = searchString.trim().replace(/\s+/g, " ");
                // when space and enter keys are pressed then not calling search functionality and string is converted into closable label
                if (keyCode !== 32 && keyCode !== 13) {
                    //If search string has min 1 chars & prev search string does not matches with new one then only search.
                    if (actualSearchString.length > 0 && value != actualSearchString) {
                        timer = setTimeout(function () {
                            value = actualSearchString;
                            $self.globalSearch(actualSearchString);
                        }, 100);
                    } else if (!inValidKeyCode || actualSearchString == "") { //Show all rows
                        value ="";
                        $self.showAllRows();
                    }
                    $self.updateSearchTag(actualSearchString);
                }
            });
            // auiSelect2 is applied to the livesearch box
            $self.find(options.searchTextBoxId).auiSelect2({
                tags: true,
                tokenSeparators: [' '],
                minimumInputLength: 1,
                selectOnBlur:true,
                dropdownCssClass: "eui-select2-dropdown",
                /* if search string is empty and user pressed space key then getting error in the console as "text.toUpperCase is not a function"
                 * to resolve this issue using formatResult function which returns the search string.
                 * EXC-5286 Encoding for JS has been added due to retunring result.text was an XSS vulnerability
                 */
                formatResult: function (result) {
                    return '<%=Encoder.encodeForJS(result.text)%>';
                }
            }).off("select2-removed").on("select2-removed", function () { // closable label is deleted on click of the cross sign
                $self.searchResult();
            });
            //If the eui-live-search text box contain any value then trigger keyup
            var liveSearchBoxValue = $self.find(options.searchTextBoxId).text().trim().replace(/\s+/g, " ").split(" ");
            if (liveSearchBoxValue != "") {
                $self.find(options.searchTextBoxId).trigger('keyup');
            }
            jQuery('.eui-view-spread').on("click", '#filterOK', function () {
                $self.adjustViewPortSize(false);
            });
            /*
             * On Active Sheet changed Event fetch the SearchString from Sheet Tag
             * & put that data in GlobalFilter TextBox.
             */
            spread.bind(GcSpread.Sheets.Events.ActiveSheetChanged, function (event, args) {
                var sheet = args.newSheet;
                /*
                 * When user searches some string, it shows expected result. After that if user switches from that sheet to another
                 * and come back to the same sheet it shows the all table data first and then it shows searched result.
                 * To overcome this, added this line.
                 */
                $self.find(".eui-view-spread").find("table").addClass("invisible");
                $self.showSearchResultOfActiveSheet(sheet);
                $self.hideScrollbarIfNotRequired();
            });
        };
        this.showSearchResultOfActiveSheet = function(sheet){
            var sheetTag = sheet.tag(),
                searchString = sheetTag && sheetTag.searchString || null;
            if (searchString !== null && searchString.trim() !== "") {
                $self.addSearchTerms(searchString);
                setTimeout(function () {
                    $self.adjustViewPortSize(false);
                }, 1);
            } else {
                $self.find(options.searchTextBoxId).auiSelect2('data', "");
                $self.find('.eui-view-spread').find('table').removeClass("invisible");
            }
        };
        this.addSearchTerms = function(searchString){
            var searchStringArray = searchString.split(" ");
            var key = 1, dataArray = [];
            searchStringArray.forEach(function (value) {
                if(value.trim() !== ""){
                    dataArray.push({id: key, text: value});
                }
                key++;
            });
            $self.find(options.searchTextBoxId).auiSelect2('data', dataArray).trigger("keyup");
        }
        this.searchResult = function () {
            var searchString = $self.find(options.searchTextBoxId).text().trim();
            if ($self.find(options.searchTextBoxId + " div").length > 0 && searchString !== "") {
                $self.globalSearch(searchString);
            } else { //Show all rows
                $self.showAllRows();
            }
            $self.updateSearchTag(searchString);
        };
        this.showAllRows = function () {
            var activeSheet = spread.getActiveSheet();
            activeSheet.isPaintSuspended(true);
            activeSheet.getRows(0, activeSheet.getRowCount()).visible(true);
            activeSheet.showRow(0, GcSpread.Sheets.VerticalPosition.top); //Set first row as Top Most Row.
            activeSheet.isPaintSuspended(false);
            $self.find(options.searchTextBoxId).auiSelect2("close"); //Trigger select2-close event to remove focus from select2 input box.
            if(!options.fullscreen){
               $self.adjustViewPortSize(false); 
            }
        };
        this.showSearchResult = function (sheet, rowsFound, lengthFindWhat) {
            sheet.isPaintSuspended(true);
            sheet.getRows(0, sheet.getRowCount()).visible(false);
            if (lengthFindWhat == 0) {
                sheet.getRows(0, sheet.getRowCount()).visible(true);
            }
            else {
                for (var i = 0; i < rowsFound.length; i++) {
                    sheet.getRow(rowsFound[i]).visible(true);
                }
            }
            sheet.isPaintSuspended(false);
            //Show excellentable when loaded completely
            $self.find('.eui-view-spread').find('table').removeClass("invisible");


            if(!options.fullscreen){
               $self.adjustViewPortSize(false); 
            }
        };
        /* Store serachString as a tag to current sheet.
         * which can be fetched easily while switching sheets/while sharing the filter.
         */
        this.updateSearchTag = function(searchString){
            sheet = spread.getActiveSheet();
            var currentTag = sheet.tag() || {};
            if(searchString.trim() == ""){ //If search is removed then delete the searchString tag
                delete currentTag.searchString;
            }else{
                currentTag.searchString = searchString;
            }
            sheet.tag(currentTag);
        };
        this.init();
        return this;
    };

    $.fn.ExcellentableKeyboardShortcuts = function (options) {
        var spread, sheet, $self = this;
        $.fn.ExcellentableKeyboardShortcuts.defaults = {
            excellentableId: ".eui-edit-spread"
        };
        this.init = function () {
            $self = this;
            options = $.extend({}, $.fn.ExcellentableKeyboardShortcuts.defaults, options),
                    spread = $self.find(options.excellentableId).wijspread("spread"),
                    sheet = spread.getActiveSheet();
            $self.bindEvent();
        }
        this.bindEvent = function () {
            sheet.isPaintSuspended(true);
            $self.on("keyup", "" + options.excellentableId, function (e) {
                var cmd = {};
                if (e.altKey && e.which === 38) { //alt+upArrow or option+upArrow (insert row before)
                    cmd.commandName = "insertrow";
                    executeCommand(spread, cmd);
                    e.preventDefault();
                }
                else if (e.altKey && e.which === 40) { //alt+downArrow or option+downArrow (insert row after)
                    cmd.commandName = "insertrowbelow";
                    executeCommand(spread, cmd);
                    e.preventDefault();
                }
            });
            $self.on("keydown", "" + options.excellentableId, function (e) {
                var cmd = {};
                if ((e.ctrlKey || e.metaKey) && e.which === 66) { //ctrl+B or cmd+B (bold)
                    cmd.commandName = "bold";
                    e.preventDefault();
                }
                else if ((e.ctrlKey || e.metaKey) && e.which === 73) { //ctrl+I or cmd+I (italic)
                    cmd.commandName = "italic";
                    e.preventDefault();
                }
                else if ((e.ctrlKey || e.metaKey) && e.shiftKey && e.which === 83) { //ctrl+shift+S or cmd+shift+S (strikethrough)
                    cmd.commandName = "strikethrough";
                    e.preventDefault();
                }
                else if ((e.ctrlKey || e.metaKey) && e.which === 85) { //ctrl+U or cmd+U (underline)
                    cmd.commandName = "underline";
                    e.preventDefault();
                }
                else if (e.metaKey && e.which === 89) { //prevent default for ctrl+Y (Redo) for mac
                    e.preventDefault();
                }
                if ((e.ctrlKey || e.metaKey) && !e.shiftKey && e.which === 83) { // ctrl+S or cmd+S (save)
                    //Ref : EXC-4698, We now allow the user to save as many times as needed, without checking any differences.
                    // We will need to fine tune this later on and add the deleted code back again after making sure its able
                    //to check differences properly.
                    $self.save();
                    e.preventDefault();
                } else if (cmd.commandName !== undefined) {
                    executeCommand(spread, cmd);
                }
            });
            sheet.isPaintSuspended(false);
        };
        this.init();
        return this;
    };
    $.fn.ExcellentableFormulaFunction = function (options) {
        var spread, sheet, selectionrange, $self = this;
        $.fn.ExcellentableFormulaFunction.defaults = {
            excellentableId: ".eui-edit-spread"
        };
        this.init = function () {
            $self = this;
            options = $.extend({}, $.fn.ExcellentableFormulaFunction.defaults, options);
            spread = $self.find(options.excellentableId).wijspread("spread");
            sheet = spread.getActiveSheet();
            selectionrange = sheet.getSelections();
            $self.formulaInterface(sheet, selectionrange, options.type);
        };
        this.getTargetCell = function (selectionRange) { //get target cell where to populate the formula based on selection.
            var targetCell = new Object(),
                    row = selectionRange.row,
                    column = selectionRange.col,
                    rowCount = selectionRange.rowCount,
                    colCount = selectionRange.colCount;

            if (rowCount === 1) { //If single row is selected
                targetCell.row = row;
                targetCell.col = column + colCount;
            } else {
                targetCell.row = row + rowCount;
                targetCell.col = column;
            }

            //Handle boundary condition
            if (targetCell.row >= sheet.getRowCount()) { //If target row does not exist.
                targetCell.row = row;
            } else if (targetCell.col >= sheet.getColumnCount()) { // If target column does not exist.
                targetCell.row = targetCell.row + 1;
                targetCell.col = column;
                if (targetCell.row >= sheet.getRowCount()) { //If selection it at bottommost right corner.
                    targetCell.row = row;
                }
            }
            return targetCell;
        };
        this.getRangeSelection = function (selectionRange) { //e.g get A1:D20 as a string from selectionRange object
            var row = selectionRange.row,
                    column = selectionRange.col,
                    rowCount = selectionRange.rowCount,
                    colCount = selectionRange.colCount,
                    cellFormula = $self.getCellPositionString(row + 1, column + 1);
            if (rowCount > 1 || colCount > 1) {
                cellFormula = cellFormula + ":" + $self.getCellPositionString(row + rowCount, column + colCount);
            }
            return cellFormula;
        };
        this.getCellPositionString = function (row, column) { //get cell Reference from cellIndex e.g row:3 & col:4 will return D4.
            if (row < 1 || column < 1) {
                return null;
            }
            else {
                var letters = "";
                switch (spread.referenceStyle()) {
                    case $.wijmo.wijspread.ReferenceStyle.A1: // 0
                        while (column > 0) {
                            var num = column % 26;
                            if (num === 0) {
                                letters = "Z" + letters;
                                column--;
                            }
                            else {
                                letters = String.fromCharCode('A'.charCodeAt(0) + num - 1) + letters;
                            }
                            column = parseInt((column / 26).toString());
                        }
                        letters += row.toString();
                        break;
                    case $.wijmo.wijspread.ReferenceStyle.R1C1: // 1
                        letters = "R" + row.toString() + "C" + column.toString();
                        break;
                    default:
                        break;
                }
                return letters;
            }
        };
        this.formulaInterface = function (sheet, selectionRange, command) {
            for (var i = 0; i < selectionRange.length; i++) {
                var formula, targetCell = new Object();
                var row = selectionRange[i].row,
                        column = selectionRange[i].col,
                        rowCount = selectionRange[i].rowCount,
                        colCount = selectionRange[i].colCount;

                if (rowCount === 1 && colCount === 1) { //If single cell is selected
                    formula = "=" + command + "(";
                    targetCell.row = row;
                    targetCell.col = column;
                    sheet.getCell(row, column).formula("=" + command + "(0)");

                } else {
                    formula = $self.getRangeSelection(selectionRange[i]);
                    formula = "=" + command + "(" + formula + ")";
                    targetCell = $self.getTargetCell(selectionRange[i]);
                    sheet.getCell(targetCell.row, targetCell.col).formula(formula);
                }
                sheet.setActiveCell(targetCell.row, targetCell.col);
                sheet.startEdit(false, formula); //convert the activeCell in editable form.
            }
        };
        this.init();
        return this;
    };

    /*
     * Ref: EXC-2471
     * Override the original Clipboad Copy function to fix below issue:
     * When user selects range of rows and cut those rows and tries to paste those row in new sheet
     * then it overrides the row numbers & replaces that with the row numbers which user copied.
     */
    var originalClipBoardClipboardCopyFuntion = GcSpread.Sheets.Sheet.prototype._clipboardCopy;
    $.fn.ExcOverwriteOriginalClipboardCopyFunction = function () {
        init = function () {
            GcSpread.Sheets.Sheet.prototype._clipboardCopy = function (range, isCutting, ignoreClipboard) {
                if (range.row == -1) {
                    range.row = 0;
                }
                if (range.col == -1) {
                    range.col = 0;
                }
                originalClipBoardClipboardCopyFuntion.apply(this, arguments);
            };
        };
        init();
    };

    /* Ref : EXC-2463
     * Override the original Floating Object function to fix the below issue:
     * After exporting to xlsx file, the position of floating objects was not exact.
     * So, by adding 4 more properties of floating object and overriding it, we are
     * able to keep the floating objects at their exact positions.
     */
    $.fn.ExcOverrideOriginalFloatingObjectFunction = function(){
    init = function(){
        GcSpread.Sheets.FloatingObject.prototype.toJSON = function(){
           var self = this;
                    var dictData = {
                            name: self._name,
                            startRow : self._startRow,
                            startColumn : self._startColumn,
                            endRow : self._endRow,
                            endColumn : self._endColumn,
                            x: self._location.x,
                            y: self._location.y,
                            width: self._location.width,
                            height: self._location.height,
                            canPrint: self._canPrint,
                            isSelected: self._isSelected,
                            isLocked: self._isLocked,
                            isVisible: self._isVisible,
                            dynamicMove: self._dynamicMove,
                            dynamicSize: self._dynamicSize,
                            fixedPosition: self._fixedPosition,
                            allowMove: self._allowMove,
                            allowResize: self._allowResize
                        };
                    var jsData = {};
                    for (var item in dictData)
                    {
                        var value = dictData[item];
                        if (!self._isDefaultValue(item, value))
                        {
                            jsData[item] = value
                        }
                    }
                    return jsData;
        }
    }
    init();
    }

    /*
     * Ref- EXC-2621-sheetLength
     * Override the original endSheetTabEditing to fix below issue:
     * Sheet Name exceeding 31 characters was not exporting to XLSX format with more than 31 characters.
     * This function overrides sheetname and allows user to enter only 24 characters for sheet Name.
     */
    var originalEndSheetTabEditingValidationForSheetName = GcSpread.Sheets._GcTabBase.prototype.endSheetTabEditing;
    $.fn.ExcOverwriteOriginalEndSheetTabEditingNameValidation = function () {
            GcSpread.Sheets._GcTabBase.prototype.endSheetTabEditing = function (sheet, cancel) {
                var self = this;
                var newSheetName = self._tabNameEditor.value;
                if (newSheetName.length > 24) {
                    jQuery("body").ExcellentableNotification({
                        title: AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.sheetname.validation.warning")
                    }).showWarningMsg();
                    $(self._tabNameEditor).val(sheet.getName());
                }
                originalEndSheetTabEditingValidationForSheetName.apply(this, arguments);
            };
    };

}(jQuery));

/**
 * Check if the sheet is protected and render an error message if it is.
 * @param currentSheet
 * @returns {boolean}
 */
function protectedCheck(currentSheet) {
    var isprotected = currentSheet.getIsProtected();

    if (isprotected) {
        AJS.$(document).ExcellentableNotification(
            {
                title:AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.context.menu.protectedsheeterror"),
                fadeout: false
            }
        ).showErrorMsg();

        return true;
    } else {
        return false;
    }
}

/**
 * Check if the sheet is protected and render an error message if it is.
 * @param currentSheet
 * @param SpreadJS selection (optional)
 * @returns {boolean}
 */

function protectedSelectionCheck(currentSheet, selection) {
    /**
     * The -1 values indicate that either row or column headers are selected -
     * and in this cases we have to use different methods to check if the selection has any protected cells in it.
    */

    if(selection === undefined) {
        return false;   
    }else {
        var selection = selection || lodash.cloneDeep(currentSheet.getSelections().pop());
            selection.row = (selection.row === -1) ? 0 : selection.row;
            selection.col = (selection.col === -1) ? 0 : selection.col;
            selection.colCount = (selection.colCount === -1) ? currentSheet.getColumnCount() : selection.colCount;
            selection.rowCount = (selection.rowCount === -1) ? currentSheet.getRowCount() : selection.rowCount;


        var isSelectionProtected = selectionHasProtectedCell(currentSheet, selection);

        if (isSelectionProtected) {
            AJS.$(document).ExcellentableNotification(
                {
                    title:AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.context.menu.protect.cells.error"),
                    fadeout: false
                }
            ).showErrorMsg();
        }

        return isSelectionProtected;
    }

}

function getSelectedCells(sheet, selection) {
    return sheet.getCells(
            selection.row,
            selection.col,
            selection.rowCount - 1 + selection.row,
            selection.colCount - 1 + selection.col
        );
}

function dataURLtoFile(dataurl, filename) {
    var arr = dataurl.split(','), mime = arr[0].match(/:(.*?);/)[1],
        bstr = atob(arr[1]), n = bstr.length, u8arr = new Uint8Array(n);
    while(n--){
        u8arr[n] = bstr.charCodeAt(n);
    }
    return new File([u8arr], filename, {type:mime});
}

/**
 * Helper method to attach a file (image) to confluence page.
 * @param formData
 * @returns {string} the download URL of the image returned by confluence after attaching the image
 */
function attachToConfluencePage(formData) {
    url = AJS.params.baseUrl + "/rest/api/content/" + AJS.params.pageId + "/child/attachment?allowDuplicated=true";
    var options = {
        formUrl: url,
        type: "POST",
        postData: formData,
        contentType: false,
        processData: false,
        async: false

    };
    var data = jQuery("body").ExcellentableAjax(options);
    var pictureUrl = "";
    if (data && data.status === 200) {
        var response = JSON.parse(data.responseText);
        var result = response.results[0]; // This will always be zero because, you can attach only one image at a time
        var links = result._links;
        var downloadLink = links.download;
        //If the context path on confluence is present, append that to the download link provided by confluence,
        // and use that as picture URL, else use the download link itself as picture URL
        var contextPath = AJS.params.contextPath;
        //var pictureUrl = "";
        if (contextPath) {
            pictureUrl = contextPath + downloadLink;
        } else {
            pictureUrl = downloadLink;
        }
    }
    return pictureUrl;
}
