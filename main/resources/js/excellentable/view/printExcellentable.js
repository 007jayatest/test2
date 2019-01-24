AJS.toInit(function () {
    if (window.matchMedia("print")) { // for chrome and safari
        var mediaQueryList = window.matchMedia('print');
        mediaQueryList.addListener(function (mql) {
            if (mql.matches) {
                retrieveExcellentableHtmlData();
            } 
        });
    }
    window.onbeforeprint = retrieveExcellentableHtmlData;  //for firefox and IE
    function retrieveExcellentableHtmlData() {
    	//do not add excellentable html if in edit mode
    	if($("#excellentable-dialog").is(":visible")){
    		$(".print-excellentable").html("");
    		return;
    	}
    	//add html for each excellentable
        $(".print-excellentable").each(function () {
            var excId = jQuery(this).attr("excellentable-id");

            $(this).ExcellentableDBOperations({
                operation: "retrieveHtml",
                "ID": excId,
                async: false
            }).success(function (data) {
                $(".print-excellentable[excellentable-id='" + excId + "']").html(data);
            });

	            	$(".eui-print-img").each(function(){
	            		
	            		var excPrintContainer = $(this);
	            		var excId = excPrintContainer.attr("excellentable-id");
	            		
	            		$(this).ExcellentableDBOperations({
		                    operation: "retrieveHtml", 
		                    "ID": excId,
		    				async : false
		                }).success(function (data) {
		                	$(".eui-print-img[excellentable-id='"+ excId +"']").html(data);
		                });
	            	
	            	});
	            	
	        });
	    }
	 
        });
