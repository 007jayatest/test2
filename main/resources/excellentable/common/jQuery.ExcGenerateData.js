/* 
 * This file is for ET testing purpose. It contains a plugin called an ExcellentableGenerateData which 
 * is used to generate data depend on different datatypes such as number,string and alphanumeric.
 */

$.fn.ExcellentableGenerateData = function (options) {
    var spread, activeSheet, $self = this, stringLength;
    this.init = function () {
        $self = this;
        spread = $self.find(options.excellentableId).wijspread("spread");
        activeSheet = spread.getActiveSheet();
        $self.generateData(options.rowCount, options.columnCount, options.dataType);
    };
    this.generateData = function (rowCount, columnCount, dataType) {
        if (dataType === "onlyNumber") {
            $self.generateOnlyNumber(rowCount, columnCount);
        } else if (dataType === "onlyString") {
            $self.generateOnlyString(rowCount, columnCount);
        } else if (dataType === "Alphanumeric") {
            $self.generateAlphanumeric(rowCount, columnCount);
        }
    };
    this.generateOnlyNumber = function (rowCount, columnCount) {
        activeSheet.isPaintSuspended(true);
        for (var row = 0; row < rowCount; row++) {
            for (var col = 0; col < columnCount; col++) {
                stringLength = Math.floor(Math.random() * 5) + 1;
                activeSheet.setValue(row, col, $self.onlyNumber(stringLength));
            }
        }
        activeSheet.isPaintSuspended(false);
    };
    this.generateOnlyString = function (rowCount, columnCount) {
        activeSheet.isPaintSuspended(true);
        for (var row = 0; row < rowCount; row++) {
            for (var col = 0; col < columnCount; col++) {
                stringLength = Math.floor(Math.random() * 5) + 1;
                activeSheet.setValue(row, col, $self.onlyString(stringLength));
            }
        }
        activeSheet.isPaintSuspended(false);
    };
    this.generateAlphanumeric = function (rowCount, columnCount) {
        activeSheet.isPaintSuspended(true);
        for (var row = 0; row < rowCount; row++) {
            for (var col = 0; col < columnCount; col++) {
                stringLength = Math.floor(Math.random() * 5) + 1;
                var randomNumber = Math.floor(Math.random() * 7) + 1;
                if (randomNumber % 2 === 0) {
                    // generate string only
                    activeSheet.setValue(row, col, $self.onlyString(stringLength));
                } else {
                    //generate number only
                    activeSheet.setValue(row, col, $self.onlyNumber(stringLength));
                }
            }
        }
        activeSheet.isPaintSuspended(false);
    };
    this.onlyNumber = function (stringLength) {
        var text = " ", charset = "0123456789";
        for (var i = 0; i < stringLength; i++)
            text += charset.charAt(Math.floor(Math.random() * charset.length));
        return text;
    };
    this.onlyString = function (stringLength) {
        var text = " ", charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        for (var i = 0; i < stringLength; i++)
            text += charset.charAt(Math.floor(Math.random() * charset.length));
        return text;
    };
    this.init();
    return this;
};