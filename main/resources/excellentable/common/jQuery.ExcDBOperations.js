(function ($) {

    $.fn.ExcellentableDBOperations = function (options) {
        var $self = this,
            contentEntityId = this.attr('content-entity-id'),
            contentType = AJS.params.contentType;

        //If user is on PageEdit Mode.
        if (typeof contentEntityId === "undefined" || contentEntityId == "undefined") {
            contentEntityId = AJS.params.contentId;
        }

        if (typeof contentType == "undefined" || contentType == "undefined" || contentType == "comment") {
            if (window.location.href.indexOf("viewpagetemplate.action") > 1 || window.location.href.indexOf("createpagetemplate.action") > 1) {
                contentType = "template";
                contentEntityId = $self.ExcellentableCustom({
                    URL: window.location.href,
                    param: "entityId"
                }).getUrlParameter();
            } else {
                contentType = "page";
            }
        }

        //If the excellentable being inserted into the page which is not saved yet then the contentType would be draft.
        if ((contentType == "page" || contentType == "blogpost") && AJS.params.newPage == true) {
            contentType = "draft";
        }

        /* Fix for confluence version prior 5.9 */
        if (AJS.params.contentId == "") {
            contentEntityId = "0";
        }

        if (typeof AJS.params != "undefined") { // Default params for atlassian plugin
            $.fn.ExcellentableDBOperations.defaults = {
                baseUrl: AJS.contextPath(),
                contentEntityId: contentEntityId,
                contentType: contentType,
                spaceKey: AJS.params.spaceKey,
                userKey: AJS.params.remoteUserKey,
                versionNumber: 0,
                async: true
            };
        } else { // Default params for Qunit
            $.fn.ExcellentableDBOperations.defaults = {
                baseUrl: "http://localhost:1990/confluence"
            };
        }
        options = $.extend({}, $.fn.ExcellentableDBOperations.defaults, options);

        try {
            if (options.operation === "" || typeof options.operation === "undefined") {
                throw("Error: operation is undefined");
            } else if (options.ID === "" || typeof options.ID === "undefined" && options.operation !== "create" && options.operation !== "isLicenseEval") {
                throw("Error: ID is undefined");
            } else if (options.metaData === "undefined" && options.operation === "create") {
                throw("Error: metaData is undefined");
            } else if (options.metaData === "" && options.operation === "update") {
                throw("Error: metaData is undefined");
            } else {
                defaults = {//AJAX properties for different AO operations
                    create: {//on new excellentable create
                        URL: options.baseUrl + "/rest/excellentable/1.0/content/table",
                        type: "POST",
                        postData: function () {
                            var tempData = {};
                            tempData ["metaData"] = options.metaData;
                            tempData ["spaceKey"] = options.spaceKey;
                            tempData ["contentType"] = options.contentType;
                            tempData ["contentEntityId"] = options.contentEntityId;
                            return JSON.stringify(tempData);
                        },
                        failMessage: AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.error.message.create")
                    },
                    update: {
                        URL: options.baseUrl + "/rest/excellentable/1.0/content/table/" + options.ID,
                        type: "PUT",
                        postData: function () {
                            var tempData = {};
                            tempData ["metaData"] = JSON.stringify(options.metaData);
                            return JSON.stringify(tempData);
                        },
                        errorMsgFade: false,
                        failMessage: AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.update.fail")
                    },
                    copy: {
                        URL: options.baseUrl + "/rest/excellentable/1.0/content/table/copy",
                        type: "POST",
                        postData: '{"id":' + options.ID + ',"contentEntityId":' + options.contentEntityId + ',"contentType":"' + options.contentType + '","spaceKey":"' + options.spaceKey + '"}',
                        failMessage: ""
                    },
                    retrieve: {
                        URL: options.baseUrl + "/rest/excellentable/1.0/content/table/" + options.ID + "?mode=" + options.mode,
                        type: "GET",
                        failMessage: AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.retrieve.fail")
                    },
                    remove: {
                        URL: options.baseUrl + "/rest/excellentable/1.0/content/table/" + options.ID,
                        type: "DELETE",
                        failMessage: AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.remove.fail")
                    },
                    retrieveSharedTable: {
                        URL: options.baseUrl + "/rest/excellentable/1.0/share/filter?excId=" + options.ID + "&secretKey=" + options.secretKey,
                        type: "GET",
                        failTitle: AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.retrieveSharedTable.title"),
                        failMessage: AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.retrieveSharedTable.failMsg")
                    },
                    retrieveMacroMetaData: {
                        URL: options.baseUrl + "/rest/api/content/" + options.contentEntityId + "/history/0/macro/id/" + options.ID,
                        type: "GET"
                    },
                    retrieveAllHistory: {
                        URL: options.baseUrl + "/rest/excellentable/1.0/content/table/" + options.ID
                        + "/versions?limit=" + options.limit + "&page=" + options.pageNumber,
                        type: "GET",
                        failTitle: AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.retrieveAllHistory.title"),
                        failMessage: AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.retrieveAllHistory.failMsg") + ".  " + AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.logout.errorMessage", AJS.params.baseUrl + '/login.action'),
                        errorMsgFade: false
                    },
                    retrieveHistoryById: {
                        URL: options.baseUrl + "/rest/excellentable/1.0/content/table/" + options.ID + "/versions/" + options.historyId,
                        type: "GET",
                        errorMsgFade: false
                    },
                    retrieveDiff: {
                        URL: options.baseUrl + "/rest/excellentable/1.0/content/table/diff/" + options.ID + "/old/" + options.oldExc + "/new/" + options.newExc,
                        type: "GET",
                        dataType: "html",
                        contentType: "text/html"
                    },
                    restoreTo: {
                        URL: options.baseUrl + "/rest/excellentable/1.0/content/table/" + options.ID + "/versions/" + options.historyId,
                        type: "PUT"
                    },
                    retrieveHtml: {
                        URL: options.baseUrl + "/rest/excellentable/1.0/content/table/html/" + options.ID,
                        type: "GET",
                        dataType: "html",
                        contentType: "text/html"
                    },
                    isLicenseEval: {
                        URL: options.baseUrl + "/rest/excellentable/1.0/content/license/isEval",
                        type: "GET"
                    },
                    addCollaborator: {
                        URL: options.baseUrl + "/rest/excellentable/1.0/multiedit/table/" + options.ID + "/collaborators/" + options.userKey + "/versions/" + options.versionNumber,
                        type: "POST",
                        showErrorMsg: false
                    },
                    updateTimeStampOfCollaborator: {
                        URL: options.baseUrl + "/rest/excellentable/1.0/multiedit/table/" + options.ID + "/collaborators/" + options.userKey + "/versions/" + options.versionNumber,
                        type: "PUT",
                        showErrorMsg: false
                    },
                    getAllCollaborators: {
                        URL: options.baseUrl + "/rest/excellentable/1.0/multiedit/table/" + options.ID + "/collaborators",
                        type: "GET",
                        showErrorMsg: false
                    },
                    getCollaboratorsByUserKey: {
                        URL: options.baseUrl + "/rest/excellentable/1.0/multiedit/table/" + options.ID + "/collaborators/" + options.userKey,
                        type: "GET",
                        showErrorMsg: false
                    },
                    deleteCollaborator: {
                        URL: options.baseUrl + "/rest/excellentable/1.0/multiedit/table/" + options.ID + "/collaborators/" + options.userKey,
                        type: "DELETE",
                        showErrorMsg: false
                    },
                    saveGZip: {
                        URL: options.baseUrl + "/rest/excellentable/1.0/content/table/zip/" + options.ID,
                        type: "PUT",
                        async: true,
                        postData: options.metaData,
                        errorMsgFade: false,
                        contentType: "application/gzip",
                        failMessage: AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.update.fail") + "  " + AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.logout.errorMessage", AJS.params.baseUrl + '/login.action')
                    },
                    getContentData: {
                        URL: options.baseUrl + "/rest/excellentable/1.0/content/page/" + options.ID,
                        type: "GET",
                        failMessage: AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.retrieve.fail")
                    }
                };
            }
        }
        catch (err) {
            return err;
        }
        this.init = function () {
            var updatedPostData;
            if (typeof defaults[options.operation].postData === "function") {
                updatedPostData = defaults[options.operation].postData.call();
            } else {
                updatedPostData = defaults[options.operation].postData;
            }

            var ajaxObj = $self.ExcellentableAjax({
                postData: updatedPostData,
                formUrl: defaults[options.operation].URL,
                type: defaults[options.operation].type,
                async: options.async,
                dataType: defaults[options.operation].dataType,
                contentType: defaults[options.operation].contentType
            });
            ajaxObj.error(function (err) {
                // Used to suppress error message from avatar manager calls(EXC-5150)
                if (typeof defaults[options.operation].showErrorMsg !== "undefined" &&
                    defaults[options.operation].showErrorMsg === false) {
                    console.error(err);
                    $self.find('.loadingDiv').remove();
                    $.fn.Excellentable.BouncyBallSpinner().hide();
                    return false;
                }
                var $target = $self.find('.eui-aui-msg-container');
                if ($target.length === 0) {
                    $target = jQuery("#editor-notifications-container");
                }
                var failTitle = defaults[options.operation].failTitle || "Error in performing operation";
                var errorMessage;
                if (!err.responseText.trim().startsWith("<")) {
                    errorMessage = (lodash.size(err.responseText) > 0) ? (JSON.parse(err.responseText)).errorMessage : undefined;
                }
                var failMsg = defaults[options.operation].failMessage || errorMessage || (err.statusText + " " + options.operation);

                //In case of being unauthorized and not having custom messages, append login link in the end of the message
                if (err.status === 401) {
                    failMsg = failMsg + ".  <br />" + AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.logout.errorMessage", AJS.params.baseUrl + '/login.action');
                }

                //This check is being done to set a better error mesage for users that attempt to add excellentable to global templates
                if(err.status === 400 && errorMessage.search(/1.ContentEntityId  2.SpaceKey/)){
                    failMsg = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.error.globalTemplate");
                }
                $self.ExcellentableNotification({ title: failTitle,body: '<p class="eui-message-body"> '+failMsg+' </p>', fadeout : false}).showErrorMsg();

                $self.find('.loadingDiv').remove();
                $.fn.Excellentable.BouncyBallSpinner().hide();
            });
            return ajaxObj;
        };

        return this.init();
    };

})(jQuery);
