(function ($) {
    $.fn.ExcellentableAjax = function (options) {
        //Sending and receiving JSON data
        $.fn.ExcellentableAjax.defaults = {
            dataType: "json",
            contentType: 'application/json',
            processData: false,
            async: true
        };

        options = $.extend({}, $.fn.ExcellentableAjax.defaults, options);
        var request;
        try {
            if (options.formUrl === "" || typeof options.formUrl === "undefined") {
                throw("Error: formUrl is undefined");
            } else if (options.type === "" || typeof options.type === "undefined") {
                throw("Error: type is undefined");
            } else {
                request = jQuery.ajax({
                    url: options.formUrl,
                    type: options.type,
                    data: options.postData,
                    contentType: options.contentType,
                    processData: options.processData,
                    dataType: options.dataType,
                    async: options.async,
                    beforeSend: function (xhr) {
                        xhr.setRequestHeader("X-Atlassian-Token", "no-check");
                        //Allow generic method to set Accept and responseType header
                        if (typeof options.accept !== "undefined") {
                            xhr.setRequestHeader("Accept", options.accept);
                        }
                        if (typeof options.responseType  !== "undefined") {
                            xhr.responseType = options.responseType;
                        }
                    },
                    success: function (data, textStatus, jqXHR)
                    {                      
                        return data;
                    }
                });
            }
        } catch (err) {
        	console.log(err)
            return err;
        }

        return request;
    };



    $.fn.ExcellentableCustom = function (options) {
        
        //Default blank properties are defined to get rid of "undefined" error.
        $.fn.ExcellentableCustom.defaults = {
            URL : "",
            param : ""
        };
        
        options = $.extend({}, $.fn.ExcellentableCustom.defaults, options);
        
         this.getUrlParameter = function () {
            var sURLVariables = options.URL.split(/[&|?]/g);
            for (var i = 0; i < sURLVariables.length; i++)
            {
                var sParameterName = sURLVariables[i].split('=');
                if (sParameterName[0] === options.param)
                {
                    return sParameterName[1];
                }
            }
        };
        //Method to return decimal count of a number
        this.countDecimals = function () {
            if ((Math.floor(options.value)).toString() !== options.value) {
                var index = options.value.trim().indexOf(".");
                if (index === -1) {
                    return 0;
                }
                else {
                    var decimals = (options.value.trim().length - 1) - index;
                    return decimals;
                }
            }
            else {
                return 0;
            }
        };
        //Check if the current browser is IE
        this.isIE = function () {
            var ua = window.navigator.userAgent;
            var msie = ua.indexOf("MSIE ");
            if (msie > 0 || !!navigator.userAgent.match(/Trident.*rv\:11\./))  // If Internet Explorer, return true
            {
                return true;
            } else  // If another browser, return false
            {
                return false;
            }

        };
        return this;
    };

    $.fn.ExcellentableCustomDialog = function (options) {
        $.fn.ExcellentableCustomDialog.defaults = {
            dialogId:"confirmDialog",
            msg : AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.default.save.confirmation")
        }
        options = $.extend({}, $.fn.ExcellentableCustomDialog.defaults, options);
        this.init= function(){
            var dialogHtml = Confluence.Templates.Excellentable.CustomDialog({
                dialogId: options.dialogId,
                msg: options.msg
            });
            jQuery('body').append(dialogHtml);
            this.bindEvent();
            return this;
        };
        this.show = function(){
            AJS.dialog2("#"+options.dialogId).show();
        };
        this.hide = function(){
            AJS.dialog2("#"+options.dialogId).hide();
        };
        this.bindEvent = function () {
            AJS.dialog2("#" + options.dialogId).on("show", function () {
                setTimeout(function () {
                    jQuery('.aui-blanket').addClass("overlayDiv");
                }, 100);
            });
            AJS.dialog2("#" + options.dialogId).on("hide", function () {
                    jQuery('.aui-blanket').removeClass("overlayDiv");
                    jQuery("#" + options.dialogId).remove();
            });
            jQuery('#'+options.dialogId).on("click","#euiSaveChangesBeforeExportButton,#euiUnsaveChangesBeforeExportButton",function(){
                jQuery('#'+options.dialogId+' , .aui-blanket').remove();
            });
        };
        return this;
    };
    
    $.fn.ExcellentableNotification = function (options) {
        $.fn.ExcellentableNotification.defaults = {
            title: "",
            fadeout: true,
            target: "body",
            delay: 3000,
            global: false
        };
        options = $.extend({}, $.fn.ExcellentableNotification.defaults, options);
        this.showSuccessMsg = function () {
            if (jQuery(options.target).find(".eui-success-msg").length == 0) {
                AJS.messages.success({
                    title: options.title,
                    fadeout: options.fadeout,
                    target: options.target,
                    delay: options.delay
                }).addClass('eui-success-msg eui-aui-msg').prependTo(options.target).attr('data-global', options.global);
            }
        };
        this.showWarningMsg = function () {
            if (jQuery(options.target).find(".eui-warning-msg").length == 0) {
                AJS.messages.warning({
                    title: options.title,
                    body: options.body,
                    fadeout: options.fadeout,
                    target: options.target,
                    delay: options.delay
                }).addClass('eui-warning-msg eui-aui-msg').prependTo(options.target).attr('data-global', options.global);
            }
        };
        //GENERIC MESSAGES ARE DEPRECATED
        this.showGenericMsg = function () {
            if (jQuery(options.target).find(".eui-generic-msg").length == 0) {
                AJS.messages.generic({
                    title: options.title,
                    fadeout: options.fadeout,
                    target: options.target,
                    delay: options.delay
                }).addClass('eui-generic-msg eui-aui-msg').prependTo(options.target).attr('data-global', options.global);
            }
        };
        this.showErrorMsg = function () {
            if (jQuery(".eui-error-msg").length === 0) {
                var $msgObj = AJS.messages.error({
                    title: options.title,
                    body: options.body,
                    fadeout: false,
                    target: options.target,
                    delay: options.delay
                });
                $msgObj.addClass("eui-error-msg eui-aui-msg").prependTo(options.target).attr('data-global', options.global);
            }
        };
        this.showInfoMsg = function () {
            if (jQuery(options.target).find(".eui-info-msg").length == 0) {
                AJS.messages.info({
                    title: options.title,
                    body: options.body,
                    fadeout: options.fadeout,
                    target: options.target,
                    delay: options.delay
                }).addClass('eui-info-msg eui-aui-msg').prependTo(options.target).attr('data-global', options.global);
            }
        };
        this.removeMsg = function () {
            jQuery(".eui-aui-msg").remove();
        };
        return this;
    };

    /**
     * Creates a generic dialog
     * 
     * @param options json
     * 
     * options{
     * 
     * 		title : "My Title",  // Title of dialog
     * 		msg : "<p>content</p>", // Body of dialog  
     * 		okText : "Save" // Default 'OK'  
     * 
     * }
     * 
     * 
     * **/
    $.fn.getDialog = function (op) {
        var okAction;
        var dialog = {
            ID: "addteq-dialog",
            okText: "OK"
        };

        $.extend(dialog, op);

        this.setOkAction = function (okAction) {
            this.okAction = okAction;
            jQuery("#euiDialogSaveChangesOkButton").click(function (e) {
                e.preventDefault();
                okAction();
            });
        };
        this.hide = function () {
            AJS.dialog2("#" + dialog.ID).hide();
            jQuery("#" + dialog.ID).remove();
        };
        this.show = function () {
            var dialogHtml =    "<section role='dialog' id='" + dialog.ID + "' class='aui-layer aui-dialog2 aui-dialog2-small' aria-hidden='true' data-aui-modal='true'>\
                                    <!-- Dialog header -->\
                                    <header class='aui-dialog2-header'>\
                                        <!-- The dialog's title -->\
    					<h2 class='aui-dialog2-header-main'>" + dialog.title + "</h2>\
                                    </header>\
                                    <!-- Main dialog content -->\
                                    <div class='aui-dialog2-content'>\
                                        <p>" + dialog.msg + "</p>\
                                    </div>\
                                    <!-- Dialog footer -->\
                                    <footer class='aui-dialog2-footer'>\
                                        <!-- Actions to render on the right of the footer -->\
    					<div class='aui-dialog2-footer-actions'>\
                                            <button id='euiDialogSaveChangesOkButton' class='aui-button aui-button-primary'>" + dialog.okText + "</button>\
                                            <button id='euiDialogSaveChangesCancelButton' class='aui-button aui-button-link'>"+AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.cancel")+"</button>\
    					</div>\
                                    </footer>\
    				</section>";

            jQuery('body').append(dialogHtml);

            AJS.dialog2("#" + dialog.ID).show();
            AJS.$("#euiDialogSaveChangesCancelButton").click(function (e) {
            	 e.preventDefault();
                 e.stopPropagation();
                 jQuery("#" + dialog.ID + ",.aui-blanket").remove();
            });
        };
        return this;
    };

    /**
     * Bouncy Ball spinner code implemented here
     * StyleSheet name - bouncyBall.css
     *
     * @returns {$.fn.Excellentable.BouncyBallSpinner}
     * @constructor
     */
    if ($.fn.Excellentable) {
      $.fn.Excellentable.BouncyBallSpinner = function () {
          var spinnerHtml = '<div class="eui-bouncy-ball-wrapper">' +
              '<section class="eui-bouncy-ball-main">' +
              '<h3 class="eui-bouncy-ball-explanation">textReplace</h3>' +
              '<div id="euiBouncyBallWrapper">' +
              '<div id="euiBouncyBall-ball"></div>' +
              '<div id="euiBouncyBallShadow"></div>' +
              '</div></section></div>';
          var spinnerHtmlWithoutText = '<div class="eui-bouncy-ball-wrapper" style="height: inherit;width: inherit">' +
              '<section class="eui-bouncy-ball-main">' +
              '<div id="euiBouncyBallWrapper" style="position: relative">' +
              '<div id="euiBouncyBall-ball"></div>' +
              '<div id="euiBouncyBallShadow"></div>' +
              '</div></section></div>';
          $.fn.Excellentable.BouncyBallSpinner.defaults = {
              targetDiv: "body",
              containerClass: "eui-spinner-container"
          };
          //Second variable set for view page
          this.show = function (selector, viewPage, text) {
              var deferred = jQuery.Deferred();
              if(viewPage === undefined){
                  viewPage = false;
              }
              selector = jQuery(selector);
              if(selector.length == 0){
                  selector = jQuery("body");
              }
              if(selector.children(".eui-bouncy-ball-wrapper").length == 0){
                  if(text === undefined){
                      var statmentNumber = Math.floor(Math.random() * 1) + 1;// Formula for range 1 - 10 :- Math.floor(Math.random() * 10) + 1
                      var spinnerDisplayMessage = "com.addteq.confluence.plugin.excellentable.spinner.statement" + statmentNumber;
                      spinnerDisplayMessage = AJS.I18n.getText(spinnerDisplayMessage);
                      text = spinnerDisplayMessage;
                  }
                  spinnerHtml = spinnerHtml.replace("textReplace", text);
                  if(viewPage){
                      jQuery(spinnerHtmlWithoutText).prependTo(selector);
                  } else {
                      jQuery(spinnerHtml).prependTo(selector);
                  }
              }else{
                  selector.find(".eui-bouncy-ball-wrapper").show();
              }
              selector.addClass($.fn.Excellentable.BouncyBallSpinner.defaults.containerClass);
              /*
              * Ref: EXC-3024
              * Bouncy ball logo takes little time to load hence we need to wait to get logo loaded. 
              */
              setTimeout(function () {
                  deferred.resolve();
              }, 10);
              return deferred.promise();
          };
          this.hide = function (selector) {
              selector = jQuery(selector);
              if(selector.length == 0){
                  selector = jQuery("body");
              }
              jQuery(selector).children(".eui-bouncy-ball-wrapper").remove();
              selector.removeClass($.fn.Excellentable.BouncyBallSpinner.defaults.containerClass);
          };
          return this;
      };
  }
})(jQuery);
