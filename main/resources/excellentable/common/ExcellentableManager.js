AJS.toInit(function () {

var Excellentable = window.Excellentable || {};
 
var ExcellentableManager = Excellentable.ExcellentableManager = {

    getContentData : function(id){
	
		var response = jQuery('body').ExcellentableDBOperations({"operation": "getContentData", ID: id});
        return new Excellentable.ExcRestResponse(response);
        
    },
    
    createExcellentable : function(metaData){
    	
    	var tableContents = metaData || "";
    	var response = jQuery('body').ExcellentableDBOperations({"operation": "create", "metaData": tableContents});
    	return new Excellentable.ExcRestResponse(response);
    }
    
};

var ExcRestResponse = Excellentable.ExcRestResponse = function(restResponse){
	
	var defer = $.Deferred();
	  	var excObject = this;
	 
	  	restResponse.done(function(data){
	  		excObject.contentEntityId = data.contentEntityId;
	  		excObject.contentType = data.contentType;
	  		excObject.id = data.id;
	  		excObject.metaData = data.metaData;
	  		defer.resolve(excObject)
 
	 })
	 .fail(function(){
		 defer.reject();
	 });
	 
	 return defer.promise();
	 
} 

});
	
