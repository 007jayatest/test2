Confluence.Blueprint.setWizard('Addteq.Excellentable:excellentable-blueprint-item', function(wizard) {   
    //On Excellentable Blueprint Dialog Post Render.
    wizard.on("post-render.excellentableBpPage1", function () {
        //Replacement of AJS.tabs.setup(), to fix issue of background page jump on click of links (Refer: EXC-2703).
        jQuery("#euiBlueprintForm").on("click", "a", function (e) {
            var href = jQuery(this).attr("href");
            jQuery('#euiBlueprintForm').find(".active-tab").removeClass("active-tab");
            jQuery('#euiBlueprintForm').find(".active-pane").removeClass("active-pane");
            jQuery(this).closest("li").addClass("active-tab");
            jQuery(href).addClass("active-pane");
            e.stopPropagation();
        });
    });
    
    //On Excellentable Blueprint Dialog Pre Render.
    wizard.on("pre-render.excellentableBpPage1", function (e,state) {
        state.soyRenderContext['contextPath'] = AJS.params.contextPath;
    });
    
    //Allow user to select Blueprint by double clicking on template.
    jQuery(document).on("dblclick",".eui-template-element-container",function(){
        jQuery(this).closest(".aui-dialog").find(".create-dialog-create-button").click();
    });
});