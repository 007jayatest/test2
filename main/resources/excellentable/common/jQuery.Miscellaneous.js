(function ($) {
    
    $.fn.DisableExcellentableFromCommentEditor = function (options) {
        
        this.init = function(){
            jQuery('body').bind('DOMNodeInserted', function (e) {
                if (jQuery(".quick-comment-form .editor-container").length > 0) { 
                    if (jQuery(e.target).hasClass("autocomplete-macros")) {
                        jQuery(".autocomplete-macro-excellentable").closest('li').remove();
                    }else if (jQuery(".aui-dropdown").length > 0 && jQuery("li.macro-excellentable").length > 0) {
                        jQuery("li.macro-excellentable").remove();
                    }else if (jQuery("#macro-browser-dialog").length > 0) {
                        if (jQuery(e.target).hasClass("dialog-panel-body")) {
                            jQuery("#macro-excellentable").remove();

                        }
                    }
                }
            });
        };
        
        this.init();
        return this;
    };
}(jQuery));

AJS.toInit(function () {
    jQuery('body').DisableExcellentableFromCommentEditor();
});