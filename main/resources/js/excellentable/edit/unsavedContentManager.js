/**
 * File         : unsavedContnetManager.js
 * Version      : 1.0.0
 * Author       : Vikash
 * Responsbility: To find if there is any content change or unsaved data in the Excellentable.
 */
"use strict";

/**
 * @desc To find if there is any content change or unsaved data in the Excellentable.
 * @param options
 * @param options.spreadObject - The excellentable spread Object
 * @return None
 */

var UnsavedContentManager = function (options) {
    var self = this;
    self.spreadObject = options.spreadObject;
    self.hasUnsavedContent = false;
    self.eventList = [];
};

UnsavedContentManager.prototype = {

    setEventList: function () {
        var self = this;

        self.eventList = [
            GcSpread.Sheets.Events.CellChanged,
            GcSpread.Sheets.Events.ClipboardPasted,
            GcSpread.Sheets.Events.ColumnChanged,
            GcSpread.Sheets.Events.ColumnWidthChanged,
            GcSpread.Sheets.Events.CommentChanged,
            GcSpread.Sheets.Events.DragDropBlock,
            GcSpread.Sheets.Events.DragFillBlock,
            GcSpread.Sheets.Events.EditChange,
            GcSpread.Sheets.Events.FloatingObjectRemoved,
            GcSpread.Sheets.Events.RowHeightChanged,
            GcSpread.Sheets.Events.SheetNameChanged,
            GcSpread.Sheets.Events.SparklineChanged,
            GcSpread.Sheets.Events.TableFiltered,
            GcSpread.Sheets.Events.ValueChanged,
            GcSpread.Sheets.Events.RangeSorted,
            GcSpread.Sheets.Events.PictureChanged
        ];
        return self.eventList;
    },
    /* ****************************************************************** *
     * Listen for cell content change and set `hasUnsavedContent` to true *
     * ****************************************************************** */

    bindContentChangeListener: function () {
        var self = this;
        self.setEventList();
        // Listen for all the events and bind the function to be called
        for (var index in self.eventList) {
            self.spreadObject.bind(self.eventList[index], function (e) {
                self.updateUnsavedContentChangedFlag();
            });
        }
    },

    updateUnsavedContentChangedFlag: function () {
        var self = this;
        self.hasUnsavedContent = true;
    },

    init: function () {
        var self = this;
        self.bindContentChangeListener();
    },

    hasUnsavedContentFlag: function () {
        var self = this;
        return self.hasUnsavedContent;
    },

    clearUnsavedContentFlag: function () {
        var self = this;
        self.hasUnsavedContent = false;
    }
};
