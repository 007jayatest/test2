AJS.toInit(function () {
    var toggle = document.getElementById('euiIsMultieditEnabled');
    if (toggle == null) {
        return;
    }
    var requestRunning = false;
    //To enable/disable excellentable-multiedit
    jQuery('#euiIsMultieditEnabled').on("click", function () {
        if(requestRunning) {
            return;
        }
        // Remove error if any
        jQuery(".eui-aui-msg").remove();
        requestRunning = true;
        toggle.disabled = true;
        toggle.busy = true;
        var getStatus = AJS.Data.get("is-excellentable-multiedit-enabled");
        if (getStatus == 1) {
            var status = 0;
            var postData = JSON.stringify({
                'status': status
            });
            jQuery.ajax({
                url: AJS.contextPath() + "/rest/excellentable/1.0/multieditconfig/settings/",
                cache: false,
                type: "POST",
                data: postData,
                dataType: "json",
                contentType: 'application/json',
                success: function (data) {
                    toggle.disabled = false;
                    toggle.checked = false;
                    toggle.busy = false;
                    requestRunning = false;
                    jQuery('meta[name=ajs-is-excellentable-multiedit-enabled]').remove();
                    jQuery('head').append('<meta name="ajs-is-excellentable-multiedit-enabled" content="0">');
                    console.log(data.message);
                },
                error: function () {
                    toggle.disabled = false;
                    toggle.checked = true;
                    toggle.busy = false;
                    requestRunning = false;
                    var errorMsgTitle = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.admin.server-unreachable.title");
                    var errorMsgBody = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.admin.server-unreachable.body");
                    showErrorMessage(errorMsgTitle, errorMsgBody, false);
                }
            });
        } else {
            testConnection();
        }
    });

    //When accept Customer Agreement
    function onAcceptAgreement() {
        AJS.dialog2("#euiCustomerAgreementDialog").hide();
        AJS.$('#euiCustomerAgreementDialog').remove();
        var status = 1;
        var postData = JSON.stringify({
            'status': status
        });
        jQuery.ajax({
            url: AJS.contextPath() + "/rest/excellentable/1.0/multieditconfig/settings/",
            cache: false,
            type: "POST",
            data: postData,
            dataType: "json",
            contentType: 'application/json',
            success: function (data) {
                toggle.busy = false;
                toggle.disabled = false;
                requestRunning = false;
                if(data.hasError) {
                    toggle.checked = false;
                    var errorMsgTitle = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.admin.server-unreachable.title");
                    var errorMsgBody = data.message;
                    showErrorMessage(errorMsgTitle, errorMsgBody, false);
                } else {
                    toggle.checked = true;
                    jQuery('meta[name=ajs-is-excellentable-multiedit-enabled]').remove();
                    jQuery('head').append('<meta name="ajs-is-excellentable-multiedit-enabled" content="1">');
                    console.log(data.message);
                }
            },
            error: function () {
                toggle.disabled = false;
                toggle.busy = false;
                toggle.checked = false;
                requestRunning = false;
                var errorMsgTitle = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.admin.server-unreachable.title");
                var errorMsgBody = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.admin.server-unreachable.body");
                showErrorMessage(errorMsgTitle, errorMsgBody, false);
            }
        });
    }

    //When decline Customer Agreement
    function onDeclineAgreement() {
        toggle.checked = false;
        toggle.busy = false;
        toggle.disabled = false;
        requestRunning = false;
        AJS.dialog2("#euiCustomerAgreementDialog").hide();
        AJS.$('#euiCustomerAgreementDialog').remove();
    }

    //Check actual status of excellentable-multiedit
    function isMultieditingEnabled() {
        var status = AJS.Data.get("is-excellentable-multiedit-enabled");
        if (status == 1) {
            toggle.checked = true;
        } else {
            toggle.checked = false;
        }
    }

    //test server connection before showing popup
    function testConnection() {
        jQuery.ajax({
            url: AJS.contextPath() + "/rest/excellentable/1.0/multieditconfig/settings/testConnection",
            cache: false,
            type: "GET",
            contentType: 'application/json',
            success: function (data) {
                if (data.hasError) {
                    toggle.disabled = false;
                    toggle.busy = false;
                    requestRunning = false;
                    showErrorMessage("",data.message, false);
                } else {
                    var agreementDialogHtml = Confluence.Templates.Excellentable.liveEditAgreement();
                    jQuery('#admin-content').append(agreementDialogHtml);
                    AJS.dialog2("#euiCustomerAgreementDialog").show();

                    jQuery('#euiButtonAccept').on("click", function () {
                        onAcceptAgreement();
                    });

                    jQuery('#euiButtonDecline').on("click", function () {
                        onDeclineAgreement();
                    });
                }
            },
            // Check what it returns in data and code accordingly.
            error: function () {
                toggle.disabled = false;
                toggle.busy = false;
                toggle.checked = false;
                requestRunning = false;
                var errorMsgTitle = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.admin.server-unreachable.title");
                var errorMsgBody = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.admin.server-unreachable.body");
                showErrorMessage(errorMsgTitle, errorMsgBody, false);
            }
        });
    }

    //Show error message
    function showErrorMessage(titleName, actualMessage, fadeout) {
      var selector = "#admin-body-content";
      jQuery(selector)
        .ExcellentableNotification({
          title: titleName,
          body: actualMessage,
          target: selector,
          fadeout: fadeout
        })
        .showErrorMsg();
    }

    // Call functions defined above
    setTimeout(function () {
        isMultieditingEnabled();
    }, 1);

});
