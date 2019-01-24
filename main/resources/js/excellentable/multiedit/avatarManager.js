/**
 * Version          :           1.0.0
 * Author           :           Saurabh
 */
"use strict";
/**
 * Initialize AvatarManager method.
 * @param excID                     :Id of excellentable where collaborative editing needs to be initialized.
 * @param versionNumber             :versionNumber of the excellentable currently being edited.
 * @param repeatTime                :Check for new collaborators will be done after repeatTime seconds.(Default: 15(sec))
 * @param htmlElement               :The html div where the collaborators profile pic will be shown.
 * @param maxWidthPercentage        :Size of the div in percentage, where user pic will be kept.(Default: 40(%))
 * @constructor
 */
var AvatarManager = function (excID, versionNumber, repeatTime, htmlElement, maxWidthPercentage) {
    this.excID = excID;
    this.versionNumber = versionNumber;
    //If not set or null, taken from property file
    this.repeatTime = (!isNaN(parseFloat(repeatTime)) && isFinite(repeatTime)) ? repeatTime :
        (AJS.I18n.getText('com.addteq.confluence.plugin.excellentable.multiedit.collaborators.push.interval') * 1000);
    this.htmlElement = htmlElement;
    this.maxWidthPercentage = maxWidthPercentage;
    this.latestCollaborators = [];
    this.collaborators = [];
    this.status = false;
    this.updateTimeoutID = {};
};
/**
 * Add all the methods related to collaborative editing.
 * @type {{initializeAndAddUser: AvatarManager.initializeAndAddUser,
 * getAllCollaboratorsAndDraw: AvatarManager.getAllCollaboratorsAndDraw,
 * getAllCollaboratorsDataByExcID: AvatarManager.getAllCollaboratorsDataByExcID,
 * updateTimeStampOfCollaborator: AvatarManager.updateTimeStampOfCollaborator,
 * deleteCollaborator: AvatarManager.deleteCollaborator,
 * bindUpdateTimeStamp: AvatarManager.bindUpdateTimeStamp,
 * unbindUpdateTimeStamp: AvatarManager.unbindUpdateTimeStamp,
 * bindDeleteUserCall: AvatarManager.bindDeleteUserCall,
 * drawImages: AvatarManager.drawImages,
 * areTheirNewCollaborators: AvatarManager.areTheirNewCollaborators,
 * addUserImage: AvatarManager.addUserImage,
 * addUsersImage: AvatarManager.addUsersImage,
 * removeUserImage: AvatarManager.removeUserImage,
 * rearrangeUserImages: AvatarManager.rearrangeUserImages,
 * moveImages: AvatarManager.moveImages}}
 */
AvatarManager.prototype = {
    /**
     * This method requires ExcellentableId and versionNumber. It will also bind the update method and delete method i.e
     * will initialize everything that collaborative editing needs (get updated collaborators, delete collaborators
     * when user leaves and draw user images).
     */
    initializeAndAddUser: function () {
        var $self = this;
        //Initialize i.e make a initializeAndAddUser call to back end4
        var addUser = jQuery(document).ExcellentableDBOperations({
            operation: "addCollaborator",
            "ID": this.excID,
            "versionNumber": this.versionNumber
        });
        //Set that excellentable exit flag as false, so that delete method won't run twice
        window.closingExcellentableFlag = false;
        addUser.done(function () {
            //Request successful, below line means user is connected and editing.
            this.status = true;
            //Retrieve all the user editing and populate them
            $self.getAllCollaboratorsAndDraw();
        }).fail(function () {
            console.log("AVM Registration failed");
        });
        //Bind update method to run after timeStamp sec
        this.bindUpdateTimeStamp();
        //Bind all user delete calls and unbind previously bind update collaborators method.
        this.bindDeleteUserCall();
    },
    /**
     * getAllCollaboratorsAndDraw by excellentable id
     */
    getAllCollaboratorsAndDraw: function () {
        var $self = this;
        var getAllUsers = jQuery(document).ExcellentableDBOperations({
            operation: "getAllCollaborators",
            "ID": this.excID
        });
        getAllUsers.done(function (data) {
            $self.latestCollaborators = data;
            $self.drawImages();

            if($self.latestCollaborators.length > 1){
                AJS.$(document).ExcellentableNotification({title: AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.noncollaborative.multiuser.title"),
                                                            body: AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.noncollaborative.multiuser.body"),
                                                            fadeout: false}).showWarningMsg();
            }
        }).fail(function () {
            console.log("AVM get all failed");
        });
    },
    /**
     * get collaborator by excellentable id and userKey
     * @returns {*}
     */
    getAllCollaboratorsDataByExcID: function () {
        var getUserByKey = jQuery(document).ExcellentableDBOperations({
            operation: "getCollaboratorsByUserKey",
            "ID": this.excID
        });
        getUserByKey.done(function (data) {
            return data;
        }).fail(function () {
            console.log("get all by exc id failed");
        });
    },
    /**
     * update timeStamp of collaborator by making a rest call to back end,
     * Update the latestCollaborator collection.
     * Will also draw new user pictures in the div provided at the time of initialization.
     */
    updateTimeStampOfCollaborator: function () {
        var $self = this;
        var updateUser = jQuery(document).ExcellentableDBOperations({
            operation: "updateTimeStampOfCollaborator",
            "ID": this.excID,
            "versionNumber": this.versionNumber
        });
        updateUser.done(function (data) {
            $self.latestCollaborators = data;
            $self.drawImages();
        }).fail(function () {
            $self.unbindUpdateTimeStamp();
            console.log("AVM got disconnected.");
        });
    },
    /**
     * Delete the collaborator from db by making rest call the back end.
     */
    deleteCollaborator: function () {
        var $self = this;
        if (this.status) {
            return;
        }
        var deleteUser = jQuery(document).ExcellentableDBOperations({
            operation: "deleteCollaborator",
            "ID": this.excID
        });
        deleteUser.done(function () {
        }).fail(function () {
            $self.status = true;
            console.log("AVM tried deleting again but failed");
        });
    },
    /**
     * Bind UpdateTimeStamp method, to be run after every 'repeatTime' seconds.
     * @returns number return a reference variable to setInterval method, so it can be unloaded later
     */
    bindSetInterval: function () {
        if (!isNaN(parseFloat(this.repeatTime)) && isFinite(this.repeatTime) && this.repeatTime < 5000) {
            this.repeatTime = 5000;
        }
        var $self = this;//Loses reference to this when running inside a function.
        return setInterval(function () {
            $self.updateTimeStampOfCollaborator($self)
        }, this.repeatTime);
    },
    /**
     * Bind update collaborators method to variable updateTimeoutId, which will be used by other methods.
     */
    bindUpdateTimeStamp: function () {
        this.updateTimeoutID = this.bindSetInterval();
    },
    /**
     * Unbind UpdateTimeStamp method which is binded in bindUpdateTimeStamp method.
     */
    unbindUpdateTimeStamp: function () {
        if (typeof this.updateTimeoutID !== "undefined") {
            clearInterval(this.updateTimeoutID);
        }
    },
    /**
     * Bind delete call to be made when user clicks on exit button or refresh the page or closes the tab.
     */
    bindDeleteUserCall: function () {
        var $self = this;
        var entryDeleted = $.Deferred(), exportClicked = false;
        //Binding to exit button
        jQuery("#euiDialogCloseButton,#euiDropdownExit").click(function () {
            //When you click on close and there are no changes, so timeout set so that exitMethod code can run first.
            window.setTimeout(function () {
                if (window.closingExcellentableFlag) {
                    $self.unbindUpdateTimeStamp();
                    $self.deleteCollaborator();
                    jQuery("#euiUserImageArea").find('li').remove();
                    entryDeleted.resolve();
                } else {
                    //When you click on close and there are changes and dialog pops up
                    jQuery("#euiDialogSaveChangesOkButton").click(function () {
                        $self.unbindUpdateTimeStamp();
                        $self.deleteCollaborator();
                        jQuery("#euiUserImageArea").find('li').remove();
                        entryDeleted.resolve();
                    });
                }
            }, 10);//Timer is set so that closing flag can be set by the required function.
        });
        //Binding to page state change
        window.onbeforeunload = function () {
            if (entryDeleted.state() === "pending" && !exportClicked) {
                $self.unbindUpdateTimeStamp();
                $self.deleteCollaborator();
                jQuery("#euiUserImageArea").find('li').remove();
                entryDeleted.resolve();
                exportClicked = false;
            }
        };
        //Check for click on export to xlsx or csv which fires onbeforeunload event. To avoid deletion of avatar.
        jQuery("aui-item-link[name^=exportas]").on("click", function () {
            exportClicked = true;
        });
    },

    /**
     * Draws images on top right end of the excellentable
     */
    drawImages: function () {
        var i;

        //Check if htmlElement where the images are to be drawn is provided or not
        if (typeof this.htmlElement === "undefined") {
            return;
        } else if (!jQuery(this.htmlElement).hasClass("eui-multiedit-avatar-container")) {
            //Add all the html elements which will used in displaying it later.
            jQuery(this.htmlElement).addClass("eui-multiedit-avatar-container")
                .append("<ul></ul>" +
                    "<a id=\"euiAvatarDropdownButton\" class=\"aui-button aui-dropdown2-trigger aui-dropdown2-trigger-arrowless eui-dropdown aui-alignment-target aui-alignment-element-attached-top aui-alignment-element-attached-right aui-alignment-target-attached-bottom aui-alignment-target-attached-right\"\n" +
                    "aria-expanded=\"false\" href=\"#euiAvatarDropdownContainer\" aria-controls=\"euiAvatarDropdownContainer\">" +
                    ". . .</a>" +
                    "<div id=\"euiAvatarDropdownContainer\" " +
                    "class=\"aui-style-default aui-dropdown2 aui-layer aui-alignment-element aui-alignment-side-bottom aui-alignment-snap-right aui-alignment-element-attached-top aui-alignment-element-attached-right aui-alignment-target-attached-bottom aui-alignment-target-attached-right\" " +
                    "data-aui-alignment=\"bottom auto\" data-aui-alignment-static=\"true\">" +
                    "<ul class=\"aui-list-truncate\"></ul></div>");
        } else{
            //Remove any live editing user if already exists
            jQuery("#euiUserImageArea").find('li[data-username-sessionid]').remove();
        }
        //Check if size is provided of html element where images will be drawn if not grab default from properties file.
        if (typeof this.maxWidthPercentage === "undefined") {//Default 40%
            this.maxWidthPercentage = AJS.I18n.getText('com.addteq.confluence.plugin.excellentable.multiedit.imageDiv.maxWidthPercentage')
        }
        //Set width of the div
        jQuery(this.htmlElement).css("width", this.maxWidthPercentage + "%");
        //Nothing is populated yet
        if (lodash.isEmpty(this.collaborators)) {
            //Populate everything
            this.addUsersImage();
            // bind user mini profile dialog to user image
            Confluence.Binder.userHover();
            //Save copy of latest collaborators to collaborators
            this.collaborators = this.latestCollaborators;
            //Checks if any new collaborator started editing or left
        } else if (this.areTheirNewCollaborators()) {
            var newTmp = [], oldTmp = [];
            var collabLength = this.collaborators.length;
            var latestCollabLength = this.latestCollaborators.length;
            //Make array of userKey only from collection
            for (i = 0; i < latestCollabLength; i++) {
                newTmp[i] = this.latestCollaborators[i].userKey;
            }
            //Make array of userKey only from collection
            for (i = 0; i < collabLength; i++) {
                oldTmp[i] = this.collaborators[i].userKey;
            }
            //Delete Collaborators which left
            for (i = 0; i < collabLength; i++) {
                if (lodash.indexOf(newTmp, oldTmp[i]) === -1) {
                    this.removeUserImage(oldTmp[i]);//deleteCollaborator pic by userKey
                }
            }
            //Add Collaborators which joined
            for (i = 0; i < latestCollabLength; i++) {
                if (lodash.indexOf(oldTmp, newTmp[i]) === -1) {
                    this.addUserImage(i);//add collaborator pic by array index
                }
            }
            //Rearrange according to size and width of the html element div
            this.rearrangeUserImages();
            //Save copy of latest collaborators to collaborators
            this.collaborators = this.latestCollaborators;
            // bind user mini profile dialog to user image
            Confluence.Binder.userHover();
        }
    },
    /**
     * Checks if there are new collaborators or not, by comparing the collections returned from updateTimeStamp method.
     * @returns {boolean}
     */
    areTheirNewCollaborators: function () {
        return (typeof (jsondiffpatch.diff(this.collaborators, this.latestCollaborators)) !== "undefined");
    },
    /**
     * Add user image to the div provided at the time of initialization
     * @param indexOfUser :indexOfUser in the latestCollaborator collection
     */
    addUserImage: function (indexOfUser) {
        var excGreen = '#52b052';
        var colorForUserImageBorderAndSelection = (indexOfUser === 0) ? excGreen : randomColor();
        var userMiniProfile = Confluence.Templates.Excellentable.userMiniProfile({
            userKey: this.latestCollaborators[indexOfUser].userKey,
            userName: this.latestCollaborators[indexOfUser].userName,
            avatar: this.latestCollaborators[indexOfUser].avatar,
            colorForUserImageBorder: colorForUserImageBorderAndSelection
        });
        jQuery(this.htmlElement + " > ul").append(userMiniProfile);
    },
    /**
     * Add all users image to the div
     */
    addUsersImage: function () {
        var latestCollabLength = this.latestCollaborators.length;
        for (var i = 0; i < latestCollabLength; i++) {
            this.addUserImage(i);
        }
        this.rearrangeUserImages();
    },
    /**
     * Remove user image based on the userKey provided from the div
     * @param userKey
     */
    removeUserImage: function (userKey) {
        jQuery(this.htmlElement).find("li[data-userkey='" + userKey + "']").remove();
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
        var presentNumberOfUserImagesInDropdown = jQuery("#euiAvatarDropdownContainer").find("> ul > li").length;
        //If div has less images then allowed and dropdown has some images in it
        if (availableNumberOfUserImages - presentNumberOfUserImagesInDiv > 0 && presentNumberOfUserImagesInDropdown > 0) {
            numberOfImagesToBeMoved = Math.min(availableNumberOfUserImages - presentNumberOfUserImagesInDiv, presentNumberOfUserImagesInDropdown);
            for (i = 0; i < numberOfImagesToBeMoved; i++) {
                this.moveImages(true);//fromDropdownToDiv
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
    }
};

//Event Listeners
$(document).on("initializeAvatarManager", initializeAvatarHandler);
$(document).on("updateVersionNumber", updateVersionNumber);
//Event handler
var av;

function initializeAvatarHandler(e) {
    if (typeof av === "undefined") {
        av = new AvatarManager(e.msg.excellentableId, e.msg.versionNumber, e.msg.repeatTime, e.msg.htmlElement, e.msg.maxWidthPercentage);
    } else {
        var div = jQuery(e.msg.htmlElement);
        div.children().remove();
        div.removeClass("eui-multiedit-avatar-container");
        av.collaborators = [];
    }
    av.initializeAndAddUser();
}

function updateVersionNumber(e) {
    av.versionNumber = e.msg.versionNumber;
    av.unbindUpdateTimeStamp();
    av.bindUpdateTimeStamp();
}
