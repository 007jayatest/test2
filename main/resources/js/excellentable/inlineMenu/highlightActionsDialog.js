/**
 * Confluence.HighlightDemoDialogs does two things:
 * 1. _appendDialogTarget function will use 
 * Confluence.DocThemeUtils.appendAbsolutePositionedElement to calculate the correct position of the selected text.
 * 
 * 2. Render the dialog content with selectionObject. 
 * Show and hide handlers are provided to handle these events.
 */
Confluence.HighlightDialogs = Confluence.HighlightDialogs || (function($) {
    var DIALOG_MAX_HEIGHT = 200;
    var DIALOG_WIDTH = 300;
    var highlightDialog;

    var defaultDialogOptions = {
        hideDelay: null,
        width : DIALOG_WIDTH,
        maxHeight: DIALOG_MAX_HEIGHT
    };

    function showHighlightDialog(selectionObject) {
        highlightDialog && highlightDialog.remove();
        var displayFn = function(content, trigger, showPopup) {
            // Add highlightedTest, num of occurrences, search index on the inline dialog which
            // are being used by REST call
            $(content).html(Confluence.HighlightDialogs.Templates.createDialogContent(
                {
                    highlightedText: selectionObject.text,
                    numOccurrences: selectionObject.searchText.numMatches,
                    searchIndex: selectionObject.searchText.index
                }
            ));
            showPopup();
            return false;
        };

        highlightDialog = _openDialog(selectionObject, 'highlightDialog', defaultDialogOptions, displayFn); // add a reference to highlight dialog to clean up in the next highlight action
    };

    function _openDialog(selectionObject, id, options, displayFn) {
        var $target = $("<div>");
        _appendDialogTarget(selectionObject.area.average, $target);

        var originalCallback = options.hideCallback;
        options.hideCallback = function() {
            $target.remove(); // clean up dialog target element when hiding the dialog
            originalCallback && originalCallback();
        };

        var dialog = Confluence.ScrollingInlineDialog($target, id, displayFn, options);
        dialog.show();
        return dialog;
    };

    function _appendDialogTarget(targetDimensions, $target) {
        Confluence.DocThemeUtils.appendAbsolutePositionedElement($target);
        $target.css({
            top: targetDimensions.top,
            height: targetDimensions.height,
            left: targetDimensions.left,
            width: targetDimensions.width,
            "z-index": -9999,
            position: 'absolute'
        });
    };

    return {
        showHighlightDialog: showHighlightDialog
    };
})(AJS.$);
