(function ($) {
    $.fn.ExcellentableMacroCopyPaste = function (config) {

        $.fn.ExcellentableMacroCopyPaste.defaultOptions = {
            macroBackgroundImage: AJS.contextPath() + "/plugins/servlet/confluence/placeholder/macro-heading?definition=e2V4Y2VsbGVudGFibGV9&locale=en_GB" // definition is the name of the macro in encoded format.
        };

        var $self = this, options = jQuery.extend({}, $.fn.ExcellentableMacroCopyPaste.defaultOptions, config);

        this.init = function () {
            $self.bindEvents();
        };
        this.bindEvents = function () {
            $self.bind('postPaste', function (e, pl, o) {
                var $copied = jQuery(o.node).find("img[data-macro-name=excellentable]");
                for (var i = 0; i < $copied.length; i++) {
                    var $macro = jQuery($copied[i]);
                    var prevId = $self.ExcellentableCustom({URL: $macro.attr("data-macro-parameters"), param: "excellentable-id"}).getUrlParameter().trim();
                    var response = $self.ExcellentableDBOperations({"operation": "copy", ID: prevId, async: false});
                    response.success(function (data) {
                        var newTableId = data.id;
                        $macro.attr("data-macro-parameters", "excellentable-id=" + newTableId);
                    });
                    if (typeof $macro.css("background-image") === "undefined" || $macro.css('background-image').trim() === "") {
                        $macro.css("background-image", "url(" + options.macroBackgroundImage + ")");
                    }
                }
            });
        };
        this.init();
    };
})(jQuery);




AJS.bind("init.rte", function () {
    jQuery(document).ExcellentableMacroCopyPaste();
});