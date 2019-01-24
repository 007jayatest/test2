"use strict";
AJS.toInit(function () {
    //Creating method for easy animation
    jQuery.fn.extend({
        animateCss: function (animationName, callback) {
            var animationEnd = (function (el) {
                var animations = {
                    animation: 'animationend',
                    OAnimation: 'oAnimationEnd',
                    MozAnimation: 'mozAnimationEnd',
                    WebkitAnimation: 'webkitAnimationEnd'
                };

                for (var t in animations) {
                    if (el.style[t] !== undefined) {
                        return animations[t];
                    }
                }
            })(document.createElement('div'));

            this.addClass('animated ' + animationName).one(animationEnd, function () {
                $(this).removeClass('animated ' + animationName);

                if (typeof callback === 'function') callback();
            });

            return this;
        }
    });

    //Initialization
    var Excellentable = window.Excellentable || {};
    //Constructor - provide default dialog with data validation from database
    var WhatsNew = Excellentable.WhatsNew = Excellentable.WhatsNew || function (location, body) {
        //Remove if dialog is already present
        if (typeof Excellentable.wnm !== "undefined" && typeof Excellentable.wnm.dialog !== "undefined") {
            Excellentable.wnm.removeDialog();
        }
        //Provide done method to run methods(ex- css change methods) as ajax call is breaking other method calls
        this.ready = jQuery.Deferred();
        //Stop from providing object to anonymous user
        if (AJS.params.remoteUserKey === "") {
            return;
        }
        var $self = this;
        this.body = body;
        this.version = [];
        this.location = location;
        this.getDataAjax = this._getData();
        this.getDataAjax.done(function (response) {
            $self.data = response.data;

            //Below function will set this.data
            $self._getNotificationVersion();

            //Updating dialog as links are not rendering through soy
            if ($self.version.length === 0) {
                return;
            }

            //Got the data continue
            $self.dialog = $self.createDefaultDialog(body);

            //Update dialog with first notification
            var currentVersion = $self.version.shift();
            $self.traversedVersion = [currentVersion];
            var content = (typeof $self.body === "object") ? $self.body[currentVersion] : $self.body;
            if (typeof content !== "undefined") {
                $self.dialog.find(".wnm-body > p").html(content);
                $self._populateButtons();
                $self.initEvents();
            } else {
                console.log("Not able to find any content in body object.");
                $self.removeDialog();
            }
            $self.ready.resolve();
        });
        this.appendTimeout = 2000;
    };
    /**
     * allows to change source of logo
     * @param source
     */
    WhatsNew.prototype.logo = function (source) {
        if (typeof source !== "undefined") {
            this.dialog.find(".wnm-logo").css({'content': 'url(' + source + ')'});
        }
    };
    WhatsNew.prototype.logoSize = function (width, height) {
        var propertyName = ["width", "height"];
        var suffix = ["px", "px"];
        this._updateProperty(propertyName, arguments, ".wnm-logo", suffix);
    };
    WhatsNew.prototype.logoLocation = function (topPixel, leftPercentage) {
        //Method needs to be updated to remove location property first
        var propertyName = ["top", "left"];
        var suffix = ["px", "%"];
        this._updateProperty(propertyName, arguments, ".wnm-logo", suffix);
    };
    WhatsNew.prototype.dialogSize = function (width, height) {
        var propertyName = ["width", "height"];
        var suffix = ["px", "px"];
        this._updateProperty(propertyName, arguments, ".wnm-content", suffix);
    };
    WhatsNew.prototype.dialogLocation = function (top, right, bottom, left, position) {
        var propertyName = ["top", "right", "bottom", "left", "position"];
        this._updateProperty(propertyName, arguments, ".wnm-container");
    };
    WhatsNew.prototype.updateAppendTimeout = function (newTimeout) {
        this.appendTimeout = (typeof newTimeout === "number") ? newTimeout : 2000;
    };
    WhatsNew.prototype.updateDialogZIndex = function (zindex) {
        zindex = (typeof zindex === "number") ? zindex : 1050;
        this.dialog.css("z-index",zindex);
    };
    /**
     * provide default dialog
     * @returns {undefined|*}
     */
    WhatsNew.prototype.getDefaultDialog = function () {
        return this.dialog;
    };
    /**
     * creates default dialog from soy template
     * @param body
     * @returns {*}
     */
    WhatsNew.prototype.createDefaultDialog = function (body) {
        if (typeof body !== "undefined") {
            var dialog = Confluence.Templates.Addteq.WhatsNew.defaultDialog();
            return jQuery(dialog);
        }
    };
    /**
     * Checks and append default dialog to page
     */
    WhatsNew.prototype.appendDefaultDialog = function () {
        var $self = this;
        if (typeof this.getDataAjax === "undefined") {
            console.log("WhatsNew object not found");
            return;
        }
        this.getDataAjax.then(function (value) {
            if (typeof $self.dialog === "undefined") {
                console.log("No new notification found.")
            } else {
                setTimeout(function () {
                    jQuery("body").append($self.dialog);
                    $self.dialog.find(".wnm-body,.wnm-footer").css("visibility", "hidden");
                    $self.dialog.find(".wnm-logo").animateCss('rollIn', function () {
                        $self.dialog.find(".wnm-body,.wnm-footer").css("visibility", "");
                        $self.dialog.find(".wnm-body,.wnm-footer").animateCss('slideInUp');
                    });
                }, $self.appendTimeout);
            }
        })
    };
    /**
     * deletes dialog from page and also removes default dialog
     */
    WhatsNew.prototype.removeDialog = function () {
        jQuery("body").find(this.dialog).remove();
        this.dialog = undefined;
    };
    /**
     * Initialize events related to dialog like close and next button
     * As animateCss function was iterating on the basis of number of animated elements, it was being called twice.
     * To avoid two _setData calls, using deferred object.
     */
    WhatsNew.prototype.initEvents = function () {
        var $self = this,
            logo = $self.dialog.find(".wnm-logo"),
            bodyFooter = $self.dialog.find(".wnm-body,.wnm-footer"),
            animate;

        function hideBody() {
            bodyFooter.css("display", "none");
            animate.resolve();
        }

        this.dialog.find(".wnm-close").on("click", function () {
            $self._updateData();
            animate = jQuery.Deferred();
            logo.animateCss('zoomOutDown', function () {
                $self.dialog.find(".wnm-logo").css("visibility", "hidden");
                bodyFooter.animateCss('bounceOutDown', hideBody);
            });
            animate.done(function () {
                $self._setData();
            })
        });

        $self.dialog.find(".wnm-next").on("click", function () {
            var currentVersion = $self.version.shift();
            $self.traversedVersion.push(currentVersion);
            if (typeof $self.body[currentVersion] !== "undefined") {
                $self.dialog.find(".wnm-body > p").html($self.body[currentVersion]);
                $self._populateButtons();
            } else {
                console.log("Not able to find any content in body object.");
                $self.removeDialog();
            }
        });

        this.dialog.find(".wnm-dont").on("click", function () {
            $self._updateData(true);
            animate = jQuery.Deferred();
            logo.animateCss('hinge', function () {
                $self.dialog.find(".wnm-logo").css("visibility", "hidden");
                bodyFooter.animateCss('flipOutX', hideBody);
            });
            animate.done(function () {
                $self._setData();
            });
        });
    };
    /**
     * Helper method, helps in updating css of different html tags in dialog, used internally.
     * @param propertyNameArr
     * @param propertyValueArr
     * @param elementSelector
     * @param suffix
     * @private
     */
    WhatsNew.prototype._updateProperty = function (propertyNameArr, propertyValueArr, elementSelector, suffix) {
        var i = 0;
        if (typeof suffix === "undefined") {
            for (i = 0; i < propertyValueArr.length; i++) {
                if (typeof propertyValueArr[i] !== "undefined") {
                    this.dialog.find(elementSelector).css(propertyNameArr[i], propertyValueArr[i]);
                }
            }
        } else {
            for (i = 0; i < propertyValueArr.length; i++) {
                if (typeof propertyValueArr[i] !== "undefined") {
                    if (typeof suffix[i] !== "undefined") {
                        propertyValueArr[i] = propertyValueArr[i] + suffix[i];
                    }
                    this.dialog.find(elementSelector).css(propertyNameArr[i], propertyValueArr[i]);
                }
            }
        }
    };
    /**
     * Method which gets data from db and returns it
     * @returns {*|boolean}
     * @private
     */
    WhatsNew.prototype._getData = function () {
        var settings = {
            "url": AJS.params.contextPath + "/rest/excellentable/1.0/whatsnew/" + this.location,
            "method": "GET",
            "headers": {
                "cache-control": "no-cache"
            }
        };
        return jQuery.ajax(settings);
    };
    /**
     * method that sets data in db and also removes the dialog
     * @private
     */
    WhatsNew.prototype._setData = function () {
        var $self = this;
        var settings = {
            "url": AJS.params.contextPath + "/rest/excellentable/1.0/whatsnew",
            "type": "POST",
            "headers": {
                "content-type": "application/json",
                "cache-control": "no-cache"
            },
            "data": JSON.stringify({
                location: this.location,
                data: this.data
            })
        };
        $.ajax(settings).done(function (response) {
            $self.removeDialog();
        });
    };
    /**
     * adds button to dialog
     * @private
     */
    WhatsNew.prototype._populateButtons = function () {
        if (typeof this.body === "object") {
            //Hide or show next button
            if (this.version.length > 0) {
                this.dialog.find(".wnm-next").removeClass("hidden");
            } else {
                this.dialog.find(".wnm-next").addClass("hidden");
            }

            //Hide or show "dont't show me again" button
            if (this.version.length === 0) {
                this.dialog.find(".wnm-dont").removeClass("hidden");
            } else {
                this.dialog.find(".wnm-dont").addClass("hidden");
            }

        }
    };
    /**
     * update the data structure used to keep the data related to notification (example subscription, attempts,
     * dialogNotificationNumber)
     * @param setState
     * @private
     */
    WhatsNew.prototype._updateData = function (setState) {
        for (var i = 0; i < this.traversedVersion.length; i) {
            var cnn = this.traversedVersion.shift();//Current Notification Number
            if (this.data.hasOwnProperty(cnn)) {
                if (parseInt(this.data[cnn][0]) < 5) {//data[cnn][0] Attempts
                    var attempts = parseInt(this.data[cnn][0]);
                    attempts += 1;
                    this.data[cnn][0] = (attempts.toString());
                    if (attempts === 5 || setState) {
                        this.data[cnn][1] = "false";
                    }
                }
                else {
                    this.data[cnn][1] = "false";
                }
            } else {
                this.data[cnn] = (setState) ? ["1", "false"] : ["1", "true"];
            }
        }

    };
    /**
     * Gets the latest notification identification version which user is subscribed to
     * @private
     */
    WhatsNew.prototype._getNotificationVersion = function () {
        if (typeof this.body === "object") {
            for (var key in this.body) {
                this.version.push(parseInt(key));
            }
        }
        for (key in this.data) {
            // skip loop if the property is from prototype
            if (!this.data.hasOwnProperty(key))
                continue;

            //Delete all notification dialog body whose subscription is set as false
            if (this.data[key][1] === "true") {
                key = parseInt(key);
                var index = this.version.indexOf(key);
                if (index === -1) {
                    this.version.push(key);
                }

            } else if (this.data[key][1] === "false") {
                key = parseInt(key);
                var index = this.version.indexOf(key);
                if (index > -1) {
                    this.version.splice(index, 1);
                }
            }
        }
    };
    //If person is in view mode
    if (((AJS.params.contentType === "page"  && !((window.location.pathname.indexOf("/resumedraft.action") > -1) || (window.location.pathname.indexOf("/editpage.action") > -1)))) || 
        (AJS.params.contentType === "blogpost" && !((window.location.pathname.indexOf("/resumedraft.action") > -1) || (window.location.pathname.indexOf("/editblogpost.action") > -1)))) {
        var messages = {
            0: AJS.I18n.getText("com.addteq.confluence.plugin.wnm.message1")
        };
        //Code to load whatsNew dialog
        Excellentable.wnm = new WhatsNew("view", messages);
        Excellentable.wnm.appendDefaultDialog();
    }
});