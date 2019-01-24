//DOM inserted events
module("DOM Inserted Event : PageCreatedFromTemplate Plugin");
test("page created from the template", function () {
    var actualResult, response, imageUrl, defination, expectedResult;
    var data = {excellentableId: 1};
    response = jQuery('iframe').contents().find('body').PageCreatedFromTemplate(data);
    var actualResult = jQuery("iframe").contents().find("p img").attr("excellentable-id");
    if (actualResult === null) {
        throws("Excellentable-Id is null");
    } else {
        notEqual(actualResult, data.excellentableId, "Excellentable Id is changed !!!");

    }
    //for defination
    imageUrl = jQuery("iframe").contents().find("p img").css('background-image').replace('url(', '').replace(')', '');
    defination = jQuery('body').ExcellentableCustom({param: "definition", URL: imageUrl}).getUrlParameter();
    actualResult = window.atob(defination);
    expectedResult = "{excellentable:excellentable-id=1}";
    notEqual(actualResult, expectedResult, "Value of defination is changed !!!");
});