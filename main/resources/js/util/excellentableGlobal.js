/**
 * Name: excellentableGlobal.js
 * Author: vikash.kumar@addteq.com
 * 
 * A JavaScript API which can be used to manage Global objects and methods.
 * - Can be used to get collaborative editing status in JavaScript using syntax
 *   ExcellentableApp.CollaborativeEditing.getStatus(): returns true if it is enable else false.
 * 
 * - Can be used to set collaborative editing status in JavaScript using syntax
 *   ExcellentableApp.CollaborativeEditing.getStatus().
 */
var ExcellentableApp = ExcellentableApp || {};
ExcellentableApp.CollaborativeEditing = ExcellentableApp.CollaborativeEditing || {};

ExcellentableApp.CollaborativeEditing = (function () {

    var isCollaborativeEditingEnabled = false;
    var API = {};
    // A public method setting collaborative editing status on the API
    API.setStatus = function (flag) {
        isCollaborativeEditingEnabled = flag;
    },
    
    // A public method getting collaborative editing status on the API
    API.getStatus = function () {
        return isCollaborativeEditingEnabled;
    }
    
    return API;
})();