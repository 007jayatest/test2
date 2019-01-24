(function ($) {
    $.fn.ConfluencePageEditOperations = function () {

        var EXC_MACRO_SELECTOR = ".editor-inline-macro[data-macro-name='excellentable']";
        var options = {
            excellentableIdAttr: "excellentable-id",
            dataMacroNameAttr: "data-macro-name",
            dataMacroParametersAttr: "data-macro-parameters"
        };

        var $self = this, hasValidLicense = true;
        var excManager = Excellentable.ExcellentableManager;

        this.init = function () {
            $self.setHasValidLicense();
            /* Override default behaviour of Excellentable MacroBrowser.*/
            AJS.MacroBrowser.setMacroJsOverride("excellentable",
                {
                    opener: function (macro) {
                        AJS.MacroBrowser.dialog = AJS.MacroBrowser.dialog || {};
                        AJS.MacroBrowser.dialog.activeMetadata = AJS.MacroBrowser.dialog.activeMetadata || {};
                        AJS.MacroBrowser.dialog.activeMetadata.macroName = "excellentable";

                        var Excellentable = window.Excellentable || {};
                        if(Excellentable.clickedOnMacro === true){
                            tinymce.confluence.macrobrowser.macroBrowserCancel();
                            setTimeout(function () {
                                Excellentable.clickedOnMacro = undefined;
                            }, 2000);
                            return;
                        }
                        tinymce.confluence.macrobrowser.macroBrowserComplete({
                            name: "excellentable",
                            "bodyHtml": "",
                            "params": {}
                        });
                    }
                }
            );

            if (hasValidLicense) {
                $self.listenForNewMacros();
                $self.listenForMacroClick()
            }

        };

        this.setHasValidLicense = function () {
            $self.find(EXC_MACRO_SELECTOR).each(function (index, excellentable) {
                // Find out if the plugin is licensed/expired or not.
                if (excellentable.src.indexOf("excellentable-unlicensed.png") > -1 || excellentable.src.indexOf("excellentable-expired-license.png") > -1) {
                    hasValidLicense = false;
                }
            });
        };

        this.listenForMacroClick = function () {

            $self.off("click").on("click", EXC_MACRO_SELECTOR, lodash.debounce($self.openExcellentable, 2000,
                {'leading': true, 'trailing': false}));
        };

        this.openExcellentable = function () {
            var Excellentable = window.Excellentable || {};
            Excellentable.clickedOnMacro = true;
            try {
                var $macro = jQuery(this);
                var tableId = AJS.SummaryHelper.getParam($macro, "excellentable-id"); //get excellentable-id parameter of macro.
                if (tableId === undefined) { //If macro does not have unique excellentableId

                    excManager.createExcellentable().then(function (newExctable) {
                        $self.updateMacro($macro, newExctable.id);
                        $self.ExcellentableDialog({excellentableId: newExctable.id});
                    });
                }
                else {
                    excManager.getContentData(tableId).then(function (exctable) {
                        if (exctable.contentType === "draft" || exctable.contentEntityId.toString() === Confluence.getContentId()) {
                            $self.ExcellentableDialog({excellentableId: exctable.id});
                        } else if (exctable.contentType === "page") {
                            //EXC-3460 Bug fix - This case occurs when the page has excellentable macro copied
                            if (exctable.contentEntityId.toString() !== Confluence.getContentId()) {
                                $self.ExcellentableDialog({excellentableId: exctable.id});
                            }
                        } else if (exctable.contentType === "template") {
                            //EXC-4356 Bug Fix - If its a space template, then render the excellentable saved in the
                            // template in edit mode and make sure the template itself is not affected!
                            var response = $self.ExcellentableDBOperations({"operation": "copy", ID: exctable.id, async: false});
                            response.success(function(data) {
                                var newTableId = data.id; //Generate a new exc id
                                $self.ExcellentableDialog({excellentableId: newTableId});
                                $self.updateMacro($macro, newTableId);
                            });
                        } else {
                            excManager.createExcellentable(exctable.metaData).then(function (newExctable) {
                                $self.updateMacro($macro, newExctable.id);
                                $self.ExcellentableDialog({excellentableId: newExctable.id});
                            });
                        }
                    })
                }
            } catch (e) {
                $self.ExcellentableNotification({
                    type: "unableToRender",
                    title: AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.error.rendering.macro")
                }).showErrorMsg();
            }
        };

        this.listenForNewMacros = function () {

            $self.bind('DOMNodeInserted', function (e) {
                var $macro = jQuery(e.target);
                var tableId = AJS.SummaryHelper.getParam($macro, "excellentable-id");
                var isExcMacro = $macro.hasClass("editor-inline-macro") && $macro.attr(options.dataMacroNameAttr) === "excellentable";
                if (isExcMacro && tableId == undefined) { //If new macro inserted
                    excManager.createExcellentable().then(function (newExctable) {
                        $self.updateMacro($macro, newExctable.id);
                    });
                }
            });


        };

        this.updateMacro = function (macro, excId) {
            tinymce.confluence.MacroUtils.updateMacro(
                {"excellentable-id": excId},
                "",
                "excellentable",
                $(macro)[0])
        };
        this.init();
        return this;
    };

})(jQuery);

jQuery(document).ready(function () {


    //If QuickPageEdit plugin is enabled
    if (typeof Confluence.QuickEdit != 'undefined') {
        //On QuickEdit load event.
        AJS.bind('quickedit.visible', function () {
            //On excellentable load in page-edit mode.
            jQuery('iframe').contents().find('body').ConfluencePageEditOperations();
        });
    } else {
        //On excellentable load in page-edit mode.
        jQuery('iframe').contents().find('body').ConfluencePageEditOperations();

    }
    // checking devMode 
    setTimeout(function () {
        if (AJS.params.isDevMode) {
            jQuery(".eui-export-json").removeClass("hidden");
        }
    }, 1000);
});
