
(function ($) {

    $.fn.ExcInsertHyperlinkEvents = function (options, currentSpread) {
        var spread, activeSheet, $self = this;
        this.currentSpread = currentSpread;
        $.fn.ExcInsertHyperlinkEvents.defaults = {
            excellentableId: ".eui-edit-spread",
            linkColor:"#0000FF"
        };
        this.init = function () {
            $self = this;
            options = $.extend(true, $.fn.ExcInsertHyperlinkEvents.defaults, options);
            spread = $self.currentSpread || $self.find(options.excellentableId).wijspread("spread"),
                    activeSheet = spread.getActiveSheet();
            $self.editableHyperlinkCellType();
            $self.bindEvent();
        };
        this.removeHyprelinkFromCell = function () {
            if (typeof spread === "undefined") {
                spread = $self.currentSpread || $self.find(options.excellentableId).wijspread("spread");
            }
            activeSheet.isPaintSuspended(true);
            var selections = activeSheet.getSelections(); //get selected cells
            for (var length = 0; length < selections.length; length++) {
                var sel = getActualCellRange(selections[length], activeSheet.getRowCount(), activeSheet.getColumnCount());
                for (var rowCount = 0; rowCount < sel.rowCount; rowCount++) {
                    for (var colCount = 0; colCount < sel.colCount; colCount++) {
                        var currentCell = activeSheet.getCell(rowCount + sel.row, colCount + sel.col, $.wijmo.wijspread.SheetArea.viewport);
                        var currentCellType = currentCell.cellType();
                        var textCellType = new GcSpread.Sheets.TextCellType();
                        if (currentCellType instanceof GcSpread.Sheets.HyperLinkCellType) {
                            currentCell.text("").cellType(textCellType);
                        }
                    }
                }
            }
            activeSheet.isPaintSuspended(false);
        };
        this.editableHyperlinkCellType = function () {
            if (typeof spread === "undefined") {
                spread = $self.currentSpread || $self.find(options.excellentableId).wijspread("spread");
            }
            activeSheet = spread.getActiveSheet();
            var rowCount = activeSheet.getRowCount();
            var colCount = activeSheet.getColumnCount();
            for (var row = 0; row < rowCount; row++) {
                for (var col = 0; col < colCount; col++) {
                    var cell = activeSheet.getCell(row, col);
                    var cellType = cell.cellType();
                    if (cellType instanceof GcSpread.Sheets.HyperLinkCellType) {
                        var text = cellType._text;
                        var linkvalue = cell.text();
                        var hyperLink = new EditableHyperLinkCellType();
                        var cellForeColor = cell.foreColor();
                        if (cellForeColor !== undefined) {
                            hyperLink.linkColor(cellForeColor).visitedLinkColor(cellForeColor).text(text).linkToolTip(linkvalue);
                        } else {
                            hyperLink.linkColor(options.linkColor).visitedLinkColor(options.linkColor).text(text).linkToolTip(linkvalue);
                        }
                        cell.cellType(hyperLink);
                    }
                }
            }
        };
        this.convertTextToHyperlink = function (e, cellInfo) {//when user edit the hyperlink cell
            if (typeof spread === "undefined") {
                spread = $self.currentSpread || $self.find(options.excellentableId).wijspread("spread");
            }
            activeSheet = spread.getActiveSheet();
            if (cellInfo.editingText !== null) {
                var hyperLink = new EditableHyperLinkCellType();
                var textValue = cellInfo.editingText; //get entered text value
                var activeCell = activeSheet.getCell(cellInfo.row, cellInfo.col);
                var cellType = activeCell.cellType();
                var textCellType = new GcSpread.Sheets.TextCellType();
                var cellForeColor = activeCell.foreColor();
                if (cellType instanceof GcSpread.Sheets.HyperLinkCellType) {
                    var text = cellType._text;
                    var linkvalue = activeCell.text();
                    if (cellForeColor !== undefined) {
                        hyperLink.linkColor(cellForeColor).visitedLinkColor(cellForeColor).text(text).linkToolTip(linkvalue);
                    } else {
                        hyperLink.linkColor(options.linkColor).visitedLinkColor(options.linkColor).text(text).linkToolTip(linkvalue);
                    }
                    activeCell.cellType(hyperLink);
                }
                else {
                    if (textValue !== "" && !(cellType instanceof GcSpread.Sheets.TextCellType)) {
                        // cell having substing as with dot operation com
                        if (textValue.indexOf("http://") !== 0 && textValue.indexOf("https://") !== 0) {
                            if (textValue.indexOf(".") > 0 && textValue.indexOf("..") < 0) {
                                var regularExpression = /^(https:\/\/|http:\/\/|ftp:\/\/|)([a-zA-Z0-9.-]+(:[a-zA-Z0-9.&%$-]+)*)*((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9][0-9]?)(\.(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])){3}|([a-zA-Z0-9-]+\.)*[a-zA-Z0-9-]+\.([a-zA-Z]+))(:[0-9]+)*(\/($|[a-zA-Z0-9.,?'\\+&%$#=~_-]+))*$/;
                                if (regularExpression.test(textValue)) {
                                    if (cellForeColor !== undefined) {
                                        hyperLink.linkColor(cellForeColor).visitedLinkColor(cellForeColor).linkToolTip("http://" + textValue).text(textValue);
                                    }else{
                                        hyperLink.linkColor(options.linkColor).visitedLinkColor(options.linkColor).linkToolTip("http://" + textValue).text(textValue);
                                    }
                                    setTimeout(function () {
                                        activeSheet.getCell(cellInfo.row, cellInfo.col).cellType(hyperLink).value("http://"+textValue);
                                    }, 10);
                                }
                            }
                        } else if (textValue.indexOf("http://") === 0 && textValue.indexOf("..") < 0 || textValue.indexOf(".") > 0) {
                            //to update the link
                            var regularExpression1 = /^[a-zA-Z0-9.:\/\/]*$/, splitTextValue = textValue.split("."),
                                    lastTextElement = splitTextValue[splitTextValue.length - 1];

                            if (regularExpression1.test(textValue) && lastTextElement !== "" && textValue !== "http://") {
                                if (cellForeColor !== undefined) {
                                    hyperLink.linkColor(cellForeColor).visitedLinkColor(cellForeColor).linkToolTip(textValue);
                                }else{
                                    hyperLink.linkColor(options.linkColor).visitedLinkColor(options.linkColor).linkToolTip(textValue);
                                }
                                setTimeout(function () {
                                    activeSheet.getCell(cellInfo.row, cellInfo.col).cellType(hyperLink).text(textValue);
                                }, 10);
                            }
                        } else {
                            // if cell having number , string or formula then it removes hyperlink cell type
                            activeCell.cellType(textCellType);
                        }
                    }
                }

            }
        };
        this.bindEvent = function () {
            if (typeof spread === "undefined") {
                spread = $self.currentSpread || $self.find(options.excellentableId).wijspread("spread");
            }
            jQuery(options.excellentableId).keyup(function (e) {
                activeSheet = spread.getActiveSheet();
                if (e.keyCode === 46) {
                    // on deleted the hyperlink cells on delete keypress
                    $self.removeHyprelinkFromCell();
                } else if (e.keyCode === 8) { // on backspace removing hyperlink cell type
                    var activeCell = activeSheet.getCell(activeSheet.getActiveRowIndex(), activeSheet.getActiveColumnIndex());
                    var currentCellType = activeCell.cellType();
                    var textCellType = new GcSpread.Sheets.TextCellType();
                    if (currentCellType instanceof GcSpread.Sheets.HyperLinkCellType) {
                        activeCell.text("").cellType(textCellType);
                    }
                }
            });
            //Enable ok button when user type something in link input box
            jQuery("#euiLinkHere").on("input", function() {
                if (jQuery(this).val() !== "") {
                    jQuery("#euiInsertlinkOkButton").button("enable");
                } else {
                    jQuery("#euiInsertlinkOkButton").button("disable");
                }
            });
            // On Enter key press triggering ok button
            jQuery("#euiAddHyperlinkDialog").keypress(function (event) {
                if (event.keyCode === 13) {
                    jQuery("#euiInsertlinkOkButton").trigger("click");
                }
            });
            //on edit end of the cell
            spread.bind($.wijmo.wijspread.Events.EditEnd, function (e, cellInfo) {
                if (typeof spread === "undefined") {
                    spread = $self.currentSpread || $self.find(options.excellentableId).wijspread("spread");
                }
                activeSheet = spread.getActiveSheet();
                $self.convertTextToHyperlink(e, cellInfo);
            });
            //paste hyperlink from outside to excellentable
            jQuery(document).on("paste", options.excellentableId, function (e) {
                if (typeof spread === "undefined") {
                    spread = $self.currentSpread || $self.find(options.excellentableId).wijspread("spread");
                }
                activeSheet = spread.getActiveSheet();
                var cellEditor = jQuery(activeSheet._editor).find("textarea"), //Cell Editor.
                    cellEditingText = cellEditor.length != 0 ? cellEditor.val().trim() : ""; 
                if (cellEditingText == "" && e.originalEvent.clipboardData && e.originalEvent.clipboardData.getData) {
                    var activeCell = activeSheet.getCell(activeSheet.getActiveRowIndex(), activeSheet.getActiveColumnIndex());
                    var regularExpression = /^(https:\/\/|http:\/\/|ftp:\/\/|)([a-zA-Z0-9.-]+(:[a-zA-Z0-9.&%$-]+)*)*((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9][0-9]?)(\.(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])){3}|([a-zA-Z0-9-]+\.)*[a-zA-Z0-9-]+\.(com|edu|gov|int|mil|net|org|biz|arpa|info|name|pro|aero|coop|museum|[a-zA-Z]{2}))(:[0-9]+)*(\/($|[a-zA-Z0-9.,?'\\+&%$#=~_-]+))*$/;
                    var clipText = e.originalEvent.clipboardData.getData('text/plain');
                    var hyperLink = new EditableHyperLinkCellType();
                    var cellType = activeCell.cellType();
                    if (!(cellType instanceof GcSpread.Sheets.TextCellType)) {
                        if (clipText.indexOf("http://") === 0 || clipText.indexOf("https://") === 0) {
                            var hyperLink = new EditableHyperLinkCellType();
                            hyperLink.linkColor(options.linkColor).visitedLinkColor(options.linkColor).linkToolTip(clipText);
                            activeCell.cellType(hyperLink).value(clipText);
                        } else {
                            if (regularExpression.test(clipText)) {
                                hyperLink.linkColor("blue").visitedLinkColor("blue").linkToolTip("http://" + clipText).text(clipText);
                                activeCell.cellType(hyperLink).value("http://" + clipText);
                            }
                        }
                    }
                }
            });
        };
        this.init();
        return this;
    };
})(jQuery);



