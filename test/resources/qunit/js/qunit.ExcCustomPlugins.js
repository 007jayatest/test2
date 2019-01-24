//Excellentable Ajax
module("Excellentable Ajax Plugin");
test("Retrieve Data by Ajax Call", function () {
    var postData, url, data, response, actualResult;
    postData = '{"id":1}';
    url = "http://localhost:1990/confluence/rest/excellentable/1.0/content/table";
    data = {"formUrl": url, "type": "POST", "postData": postData};
    response = jQuery('body').ExcellentableAjax(data);
    actualResult = response.hasOwnProperty("success");
    ok(actualResult, "ajaxcall success ");
    //checking for the metaData
    actualResult = response.responseJSON.hasOwnProperty("metaData");
    ok(actualResult, "metaData attribute is present!!!");
});
test("Create Data by Ajax Call", function () {
    var postData, url, data, response, actualResult;
    var postData = '{"tableId":"","metaData":"","themeName":"default"}';
    url = "http://localhost:1990/confluence/rest/excellentable/1.0/content/table";
    data = {"formUrl": url, "type": "POST", "postData": postData};
    response = jQuery('body').ExcellentableAjax(data);
    actualResult = response.hasOwnProperty("success");
    ok(actualResult, "ajaxcall success ");
    //checking for the id
    actualResult = response.responseJSON.hasOwnProperty("id");
    ok(actualResult, "Id attribute is present!!!");
});

module("Negative Testing for Ajax Plugin: Checking Pulgins parameters");
test("FormUrl is null", function () {
    var response, postData;
    postData = '{"tableId":"","metaData":"","themeName":"default"}';
    response = jQuery('body').ExcellentableAjax({"formUrl": "", "type": "POST", "postData": postData});
    if (response.indexOf("Error") >= 0) {
        ok(response, "Success Thrown " + response + "\"");
    } else {
        throw(response);
    }
});
test("type is null", function () {
    var response, postData, url;
    postData = '{"tableId":"","metaData":"","themeName":"default"}';
    url = "http://localhost:1990/confluence/rest/excellentable/1.0/content/table";
    response = jQuery('body').ExcellentableAjax({"formUrl": url, "type": "", "postData": postData});
    if (response.indexOf("Error") >= 0) {
        ok(response, "Success Thrown " + response + "\"");
    } else {
        throw(response);
    }
});
test("PostData is null", function () {
    var response, url;
    url = "http://localhost:1990/confluence/rest/excellentable/1.0/content/table";
    response = jQuery('body').ExcellentableAjax({"formUrl": url, "type": "POST", "postData": ""});
    if (response.indexOf("Error") >= 0) {
        ok(response, "Success Thrown " + response + "\"");
    } else {
        throw(response);
    }
});

//Excellentable Custom Plugin
module("Excellentable Custom");

//Checking Parameters in the URL
test("Excellentable Custom Plugin", function () {
    var actualResult, expectedResult;
    var url = "http://localhost:1990/confluence/pages/createpage-entervariables.action?templateId=917507&spaceKey=TEST&title=&newSpaceKey=TEST&fromPageId=884786";
    var data = {URL: url, param: "templateId"};
    var data1 = {URL: url, param: "spaceKey"};
    actualResult = jQuery('body').ExcellentableCustom(data).getUrlParameter();
    expectedResult = 917507;
    equal(actualResult, expectedResult, "Parameters value of templateId is equals !!!");
    actualResult = jQuery('body').ExcellentableCustom(data1).getUrlParameter();
    expectedResult = "TEST";
    equal(actualResult, expectedResult, "Parameters values spaceKey is equals !!!");
});