"use strict";

var AvatarManagerLive = function (htmlElement, maxWidthPercentage, selfSessionId) {
    this.htmlElement = htmlElement;
    this.maxWidthPercentage = maxWidthPercentage;
    this.selfSessionId = selfSessionId;
    this.presentUsers = [];
    this.oldList = {};
};
AvatarManagerLive.prototype = {

    checkIfAvatarAreaPresent: function () {
        return (jQuery(this.htmlElement).hasClass("eui-multiedit-avatar-container"));
    },

    initializeAvatarArea: function () {
        jQuery(this.htmlElement).addClass("eui-multiedit-avatar-container")
            .append("<ul></ul>" +
                "<a id=\"euiAvatarDropdownButton\" class=\"aui-button aui-dropdown2-trigger aui-dropdown2-trigger-arrowless eui-dropdown aui-alignment-target aui-alignment-element-attached-top aui-alignment-element-attached-right aui-alignment-target-attached-bottom aui-alignment-target-attached-right\"\n" +
                "aria-expanded=\"false\" href=\"#euiAvatarDropdownContainer\" aria-controls=\"euiAvatarDropdownContainer\">" +
                ". . .</a>" +
                "<div id=\"euiAvatarDropdownContainer\" " +
                "class=\"aui-style-default aui-dropdown2 aui-layer aui-alignment-element aui-alignment-side-bottom aui-alignment-snap-right aui-alignment-element-attached-top aui-alignment-element-attached-right aui-alignment-target-attached-bottom aui-alignment-target-attached-right\" " +
                "data-aui-alignment=\"bottom auto\" data-aui-alignment-static=\"true\">" +
                "<ul class=\"aui-list-truncate\"></ul></div>");
        //Check if size is provided of html element where images will be drawn if not grab default from properties file.
        if (typeof this.maxWidthPercentage === "undefined") {//Default 40%
            this.maxWidthPercentage = AJS.I18n.getText('com.addteq.confluence.plugin.excellentable.multiedit.imageDiv.maxWidthPercentage')
        }
        //Set width of the div
        jQuery(this.htmlElement).css("width", this.maxWidthPercentage + "%");
    },


    addUserIfNotPresentInList: function (liElement) {
        var $self = this;
        var username = liElement.getAttribute('data-userName-sessionId');
        if (lodash.indexOf($self.presentUsers, username) === -1) {
            $self.presentUsers.push(username);
        }
    },

    retrievePopulatedUsers: function () {
        var $self = this;
        this.presentUsers = [];
        var liArray = jQuery(this.htmlElement).find("li");
        lodash.forEach(liArray, function (liElement) {
            $self.addUserIfNotPresentInList(liElement);
        });
    },

    /**
     * Update
     */
    getChangesInCurrentUserList: function (newList) {
        var $self = this;
        $self.changesInList = [];
        //Users need to be added
        lodash.forEach(newList, function (value, key) {
            if (lodash.indexOf($self.presentUsers, key) === -1) {
                $self.changesInList.push([value]);
            }
        });
        //Users need to be deleted
        lodash.forEach($self.presentUsers, function (value) {
            if (typeof newList[value] === "undefined") {
                $self.changesInList.push([value, 0, 0]);
            }
        });

    },

    /**
     * Move images from dropdown to div or vice-versa based on param passed.
     * Also disables the dropdown if no image is present in it.
     * @param fromDropdownToDiv
     */
    moveImages: function (fromDropdownToDiv) {
        var elementDiv = this.htmlElement + " > ul";
        var elementDropdown = "#euiAvatarDropdownContainer > ul";
        //Move image
        if (fromDropdownToDiv) {
            jQuery(elementDiv).append(jQuery(elementDropdown + "> li:first").remove());
        } else {
            jQuery(elementDropdown).append(jQuery(elementDiv + "> li:first").remove());
        }
    },

    /**
     * Disable dropdown div if no user pic is present in it
     */
    hideDropdown: function () {
        if (jQuery("#euiAvatarDropdownContainer").find("> ul > li").length > 0) {
            jQuery("#euiAvatarDropdownButton").removeClass("eui-hide");
            jQuery(this.htmlElement).removeClass("eui-multiedit-avatar-container-right");
        } else {
            jQuery("#euiAvatarDropdownButton").addClass("eui-hide");
            jQuery(this.htmlElement).addClass("eui-multiedit-avatar-container-right");
        }
    },

    addUserImage: function (newUser) {
        var userNameSessionId = newUser[0]+newUser[3];
        var userMiniProfileUserName = Confluence.Templates.Excellentable.userMiniProfileUserName({
            userNameWithSessionId: userNameSessionId,//Confluence template doesn't like operations inside them like add
            userName: newUser[0],
            avatar: newUser[2],
            colorForUserImageBorder: newUser[1]
        });
        jQuery(this.htmlElement + " > ul").append(userMiniProfileUserName);
    },

    /**
     * Remove user image based on the userKey provided from the div
     * @param usernameSessionId
     */
    removeUserImage: function (usernameSessionId) {
        jQuery(this.htmlElement).find("li[data-userName-sessionId='" + usernameSessionId + "']").remove();
    },

    /**
     * Re-arrange user images if the exceed the size given at the time of declaration by moving them to a dropdown or
     * vice versa.
     */
    rearrangeUserImages: function () {
        var i, numberOfImagesToBeMoved;
        //Max number of images that can fit in div
        var availableNumberOfUserImages = Math.floor((this.maxWidthPercentage * $(window).width()) / 3888.9);
        //current number of images in div
        var presentNumberOfUserImagesInDiv = (jQuery(this.htmlElement + " > ul > li").length);
        //Current number of images in dropdown
        var presentNumberOfUserImagesInDropdown = jQuery("#euiAvatarDropdownContainer").find("li").length;
        //If div has less images then allowed and dropdown has some images in it
        if (availableNumberOfUserImages - presentNumberOfUserImagesInDiv > 0 && presentNumberOfUserImagesInDropdown > 0) {
            numberOfImagesToBeMoved = Math.min(availableNumberOfUserImages - presentNumberOfUserImagesInDiv, presentNumberOfUserImagesInDropdown);
            for (i = 0; i < numberOfImagesToBeMoved; i++) {
                this.moveImages(true);//fromDivToDropdown
            }
            //If div has more images than it is allowed
        } else if (availableNumberOfUserImages - presentNumberOfUserImagesInDiv < 0) {
            numberOfImagesToBeMoved = presentNumberOfUserImagesInDiv - availableNumberOfUserImages;
            for (i = 0; i < numberOfImagesToBeMoved; i++) {
                this.moveImages(false);//fromDropdownToDiv
            }
        }
        this.hideDropdown();
    },

    drawUsers: function () {
        var $self = this;
        lodash.forEach($self.changesInList, function (value) {
            switch (value.length) {
                case 1:
                    $self.addUserImage(value[0]);
                    break;
                case 3:
                    $self.removeUserImage(value[0]);
                    break;
            }
        });
        //Rearrange according to size and width of the html element div
        this.rearrangeUserImages();
        // bind user mini profile dialog to user image
        Confluence.Binder.userHover();
    },

    parseList: function (newList) {
        var $self = this;
        var parsedList = {};
        lodash.forEach(newList, function (value) {
            if(typeof value.userId === "undefined"){
                return;
            }
            if (value.sessionId === $self.selfSessionId){
                value.userColor = "#52b052";
            }
            parsedList[value.userName + value.sessionId] = [value.userName, value.userColor, AJS.params.contextPath+value.userAvatarUrl, value.sessionId];
        });
        return parsedList;
    },

    initialize: function (newList, initializeStatus) {
        if(jQuery(this.htmlElement).length < 1){
            console.log("htmlElement provided to avatar manager does not exist.\n Stopping initialization");
        }
        var parsedNewList = this.parseList(newList);
        var isAvatarAreaPresent = this.checkIfAvatarAreaPresent();
        if (!isAvatarAreaPresent) {
            this.initializeAvatarArea();
        }
        this.retrievePopulatedUsers();
        this.getChangesInCurrentUserList(parsedNewList);
        this.drawUsers();
        initializeStatus.resolve();
    },

    parseUser: function (user) {
        return [user.userName, user.userColor, AJS.params.contextPath+user.userAvatarUrl, user.sessionId];
    },
    checkIfUserExist: function (user) {
        return (jQuery(this.htmlElement).find("li[data-userName-sessionId='"+ (user[0]+user[3]) +"']").length > 0)
    },
    addUser: function (user) {
        if(typeof user.userId === "undefined"){
            return;//Bad Entry
        }
        var parsedUser = this.parseUser(user);
        //Check if does not exist and then add
        if(!this.checkIfUserExist(parsedUser)){
            this.changesInList = [];
            this.changesInList.push([parsedUser]);
            this.drawUsers();
        }
    },

    removeUser: function (user) {
        if(typeof user.userId === "undefined"){
            return;//Bad Entry
        }
        var parsedUser = this.parseUser(user);
        //Check if user exist and then deleting user
        if(this.checkIfUserExist(parsedUser)){
            this.changesInList = [];
            this.changesInList.push([(parsedUser[0]+parsedUser[3]), 0, 0]);
            this.drawUsers();
        }
    }
};
