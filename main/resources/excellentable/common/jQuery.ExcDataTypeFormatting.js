(function ($) {
    $.fn.ETDataTypeFormatting = function (sheet, options) {
        var selections, selection, firstSelectedCell,cellsWithinSelection,
        $currencyButton = $('.icon-icomoon.icon-coin-dollar').parent('button'),
        $percentageButton = $('.icon-icomoon.icon-percent').parent('button'),
        $numberButton = jQuery('.icon-icomoon.icon-number123').parent('button');

        this.init = function () {
            selections = sheet.getSelections(); //get selected cells
            for (var n = 0; n < selections.length; n++) {
                if (selections[n].row === -1 && selections[n].col === -1) {    //If top left corner is selected (whole rows and columns).
                    selection = sheet.getColumns(0, selections[n].colCount - 1);
                    firstSelectedCell = sheet.getCell(0, 0);
                } else if (selections[n].row === -1) {    //If whole column is selected
                    selection = sheet.getColumns(selections[n].col, selections[n].col + selections[n].colCount - 1);
                    firstSelectedCell = sheet.getCell(0, selections[n].col);
                } else if (selections[n].col === -1) {   //If whole row is selected
                    selection = sheet.getRows(selections[n].row, selections[n].row + selections[n].rowCount - 1);
                    firstSelectedCell = sheet.getCell(selections[n].row, 0);
                }else {  //If range is selected
                    selection = sheet.getCells(selections[n].row, selections[n].col, selections[n].row + selections[n].rowCount - 1, selections[n].col + selections[n].colCount - 1);
                    firstSelectedCell = sheet.getCell(selections[n].row, selections[n].col);
                }

                /**Get object of all selected cells within a selection
                 * e.g if Sheet is having 100 rows & user has selected Column C then select cells from C1 to C100 **/
                cellsWithinSelection= sheet.getCells(selections[n].row , selections[n].col,
                                              selections[n].row + selections[n].rowCount -1 , selections[n].col + selections[n].colCount - 1);
                jQuery($currencyButton).add($percentageButton).add($numberButton).removeClass("active");

                switch (options.type) {
                    case "currency":
                        this.currencyFormatting();
                        break;
                    case "percent":
                        this.percentageFormatting();
                        break;
                    case "incrementDecimal":
                        this.decimal("increment");
                        break;
                    case "decrementDecimal":
                        this.decimal("decrement");
                        break;
                    case "number":
                        this.numberFormatting(options.format);
                        break;
                }
            }
        };
        this.currencyFormatting = function () {
            var currentFormatter = selection.formatter();
            if (currentFormatter !== undefined && currentFormatter.indexOf("$") >= 0) { // if already in currency format
                selection.formatter(undefined);
                $currencyButton.removeClass("active"); //remove highlight of currency button
            } else {
                cellsWithinSelection.formatter(undefined);
                selection.formatter("$#,##0.00");
                $currencyButton.addClass("active"); //highlight currency button
            }
        };
        this.percentageFormatting = function () {
            var currentFormatter = selection.formatter();
            if (currentFormatter !== undefined && currentFormatter.indexOf("%") >= 0) { //if already in percent format
                selection.formatter(undefined);
                $percentageButton.removeClass("active"); //remove highlight of percentage button
            } else {
                cellsWithinSelection.formatter(undefined);
                selection.formatter("0.00%");
                $percentageButton.addClass("active"); //highlight percentage button
            }
        };
        this.decimal = function (type) {
            var decimalCount = 0, newFormatter = "0";
            var currentFormatter = selection.formatter();
            if (currentFormatter) { //if any formatter is applied
                decimalCount = jQuery(this).ExcellentableCustom({value: currentFormatter.replace(/[%,$]/, "")}).countDecimals(); //get decimal count of applied formatter.
            } else {
                if (firstSelectedCell.value() !== null) {
                    decimalCount = jQuery(this).ExcellentableCustom({value: firstSelectedCell.value().toString()}).countDecimals(); //get decimal count of first selected value.
                }
            }
            if (type === "increment") {
                ++decimalCount;
            } else if (type === "decrement") {
                --decimalCount;
            }
            if (decimalCount >= 0 && decimalCount <= 30) {
                if (decimalCount > 0) { //create new formatter with desired decimal count.
                    newFormatter = "0.0";
                    for (var i = 1; i < decimalCount; i++) {
                        newFormatter = newFormatter + "0";
                    }
                }
                if (currentFormatter !== undefined && currentFormatter.indexOf('%') >= 0) { //If percent format is applied on that cell.
                    selection.formatter(newFormatter + "%");
                } else if (currentFormatter !== undefined && currentFormatter.indexOf("$") >= 0) {  //If currency format is applied on that cell.
                    selection.formatter("$#,##" + newFormatter);
                } else if (currentFormatter !== undefined && currentFormatter.substring(0, 4) == "#,##") {  //If number format is applied on that cell.
                    selection.formatter("#,##" + newFormatter);
                } else {
                    selection.formatter(newFormatter);
                }
            }
        };
        this.numberFormatting = function (format) {
            var currentFormatter = selection.formatter();
            if (currentFormatter !== undefined && currentFormatter === format) { //if already in number format is applied to the cell
                selection.formatter(undefined);
                $numberButton.removeClass("active"); //remove highlight of number button
            } else {
                cellsWithinSelection.formatter(undefined);
                selection.formatter(format);
                $numberButton.addClass("active"); //highlight number format button
            }
        };
        this.init();
        return this;
    };
})(jQuery);
