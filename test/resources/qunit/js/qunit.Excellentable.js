module("Apply View and Edit", {
  beforeEach: function() {
    jQuery('body').append('<div class="eui-view-spread"></div><div class="eui-edit-spread"></div>');
  },
  afterEach: function() {
    jQuery('.eui-view-spread,.eui-edit-spread,.wijspread-popup,.gcStringWidthSpanStyle').remove();
  }
});
test("Apply View Mode", function () {
    var actualResult, expectedResult, response, newSpreadJs;
    response = jQuery("body").Excellentable({excellentableId: 1, type: "view"});
    newSpreadJs = jQuery(".eui-view-spread").wijspread("spread");
    //sheet is non-editable in view mode
    actualResult = newSpreadJs.getActiveSheet().getIsProtected();
    expectedResult = true;
    equal(actualResult, expectedResult, "Sheet is not editable in the view mode !!!");
    //tabstrip is not visible
    actualResult = newSpreadJs._tabStripVisible;
    expectedResult = false;
    equal(actualResult, expectedResult, "Tab Strip is not Visible !!!");
});

test("Apply Edit Mode", function () {
    var actualResult, expectedResult, response, newSpreadJs;
    response = jQuery("body").Excellentable({excellentableId: 1, type: "edit"});
    newSpreadJs = jQuery(".eui-edit-spread").wijspread("spread");
    expectedResult = false;
    //sheet is editable
    actualResult = newSpreadJs.getActiveSheet().getIsProtected();
    equal(actualResult, expectedResult, "Sheet is editable in the edit mode !!!");
    //tabstrip is visible
    expectedResult = true;
    var actualResult = newSpreadJs._tabStripVisible;
    equal(actualResult, expectedResult, "Tab Strip is Visible in the edit mode !!!");
});

//Multisheet
module("Multisheet", {
  beforeEach: function() {
    jQuery('body').append('<div class="eui-view-spread"></div><div class="eui-edit-spread"></div>');
  },
  afterEach: function() {
    jQuery('.eui-view-spread,.eui-edit-spread,.wijspread-popup,.gcStringWidthSpanStyle').remove();
  }
});
test("Show Mutisheet View Mode", function () {
    var actualResult, expectedResult, response, newSpreadJs;
    response = jQuery("body").Excellentable({excellentableId: 1, type: "view", view: {multisheet: "true"}});
    newSpreadJs = response.getSpreadObject();
    expectedResult = true;
    actualResult = newSpreadJs._newTabVisible;
    equal(actualResult, expectedResult, "MultiSheet is applied in the view mode!!!");
});

test("Hide Mutisheet View Mode", function () {
    var actualResult, expectedResult, response, newSpreadJs;
    response = jQuery("body").Excellentable({excellentableId: 1, type: "view", view: {multisheet: "false"}});
    newSpreadJs = response.getSpreadObject();
    expectedResult = false;
    actualResult = newSpreadJs._newTabVisible;
    equal(actualResult, expectedResult, "MultiSheet is removed in the view mode!!!");
});

test("Show Mutisheet Edit Mode", function () {
    var actualResult, expectedResult, response, newSpreadJs;
    response = jQuery("body").Excellentable({excellentableId: 1, type: "edit", edit: {multisheet: "true"}});
    newSpreadJs = response.getSpreadObject();
    expectedResult = true;
    actualResult = newSpreadJs.newTabVisible();
    equal(actualResult, expectedResult, "MultiSheet is applied in the edit mode!!!");
});

test("Remove Mutisheet Edit Mode", function () {
    var actualResult, expectedResult, response, newSpreadJs;
    response = jQuery("body").Excellentable({excellentableId: 1, type: "edit", edit: {multisheet: "false"}});
    newSpreadJs = response.getSpreadObject();
    expectedResult = false;
    actualResult = newSpreadJs.newTabVisible();
    equal(actualResult, expectedResult, "MultiSheet is removed in the edit mode!!!");
});

//Column filter
module("Column Filter", {
  beforeEach: function() {
    jQuery('body').append('<div class="eui-view-spread"></div><div class="eui-edit-spread"></div>');
  },
  afterEach: function() {
    jQuery('.eui-view-spread,.eui-edit-spread,.wijspread-popup,.gcStringWidthSpanStyle').remove();
  }
});
test("Apply Column Filter View Mode", function () {
    var actualResult, expectedResult, response, newSpreadJs, activeSheet;
    response = jQuery("body").Excellentable({excellentableId: 1, type: "view", view: {columnFilter: "true"}});
    newSpreadJs = response.getSpreadObject();
    activeSheet = newSpreadJs.getActiveSheet();
    expectedResult = true;
    actualResult = activeSheet.rowFilter().filterButtonVisible();
    equal(actualResult, expectedResult, "Row filter Button is not visible in the view Mode !!!");
});

test("Remove Column Filter View Mode", function () {
    var actualResult, expectedResult, response, newSpreadJs, activeSheet;
    response = jQuery("body").Excellentable({excellentableId: 1, type: "view", view: {columnFilter: "false"}});
    newSpreadJs = response.getSpreadObject();
    activeSheet = newSpreadJs.getActiveSheet();
    expectedResult = false;
    actualResult = activeSheet.rowFilter().filterButtonVisible();
    equal(actualResult, expectedResult, "Row filter Button is not visible in the view Mode !!!");
});

test("Apply Column Filter Edit Mode", function () {
    var actualResult, expectedResult, response, newSpreadJs, activeSheet;
    response = jQuery("body").Excellentable({excellentableId: 1, type: "view", view: {columnFilter: "true"}});
    newSpreadJs = response.getSpreadObject();
    activeSheet = newSpreadJs.getActiveSheet();
    expectedResult = true;
    actualResult = activeSheet.rowFilter().filterButtonVisible();
    equal(actualResult, expectedResult, "Row filter Button is not visible in the view Mode !!!");
});

test("Remove Column Filter Edit Mode", function () {
    var actualResult, expectedResult, response, newSpreadJs, activeSheet;
    response = jQuery("body").Excellentable({excellentableId: 1, type: "view", view: {columnFilter: "false"}});
    newSpreadJs = response.getSpreadObject();
    activeSheet = newSpreadJs.getActiveSheet();
    expectedResult = false;
    actualResult = activeSheet.rowFilter().filterButtonVisible();
    equal(actualResult, expectedResult, "Row filter Button is not visible in the view Mode !!!");
});

//Column Resizable
module("Column Resizable", {
  beforeEach: function() {
    jQuery('body').append('<div class="eui-view-spread"></div><div class="eui-edit-spread"></div>');
  },
  afterEach: function() {
    jQuery('.eui-view-spread,.eui-edit-spread,.wijspread-popup,.gcStringWidthSpanStyle').remove();
  }
});
test("Apply Column Resizable View Mode", function () {
    var actualResult, expectedResult, response, newSpreadJs, activeSheet, columnCount;
    response = jQuery("body").Excellentable({excellentableId: 1, type: "view", view: {resizableColumn: "true"}});
    newSpreadJs = response.getSpreadObject();
    activeSheet = newSpreadJs.getActiveSheet();
    columnCount = activeSheet.getColumnCount();
    var expectedResult = true;
    for (var i = 0; i < columnCount; i++) {
        actualResult = activeSheet.getColumn(i).resizable();
        equal(actualResult, expectedResult, "Applied column resizable on" + i + "column in view mode !!!");
    }
});

test("Remove Column Resizable View Mode", function () {
    var actualResult, expectedResult, response, newSpreadJs, activeSheet, columnCount;
    response = jQuery("body").Excellentable({excellentableId: 1, type: "view", view: {resizableColumn: "false"}});
    newSpreadJs = response.getSpreadObject();
    activeSheet = newSpreadJs.getActiveSheet();
    columnCount = activeSheet.getColumnCount();
    var expectedResult = false;
    for (var i = 0; i < columnCount; i++) {
        actualResult = activeSheet.getColumn(i).resizable();
        equal(actualResult, expectedResult, "Removed column resizable on" + i + "column in view mode !!!");
    }
});

test("Apply Column Resizable Edit Mode", function () {
    var actualResult, expectedResult, response, newSpreadJs, activeSheet, columnCount;
    response = jQuery("body").Excellentable({excellentableId: 1, type: "edit", edit: {resizableColumn: "true"}});
    newSpreadJs = response.getSpreadObject();
    activeSheet = newSpreadJs.getActiveSheet();
    columnCount = activeSheet.getColumnCount();
    var expectedResult = true;
    for (var i = 0; i < columnCount; i++) {
        actualResult = activeSheet.getColumn(i).resizable();
        equal(actualResult, expectedResult, "Applied column resizable on" + i + "column in edit mode !!!");
    }
});

test("Remove Column Resizable Edit Mode", function () {
    var actualResult, expectedResult, response, newSpreadJs, activeSheet, columnCount;
    response = jQuery("body").Excellentable({excellentableId: 1, type: "edit", edit: {resizableColumn: "false"}});
    newSpreadJs = response.getSpreadObject();
    activeSheet = newSpreadJs.getActiveSheet();
    columnCount = activeSheet.getColumnCount();
    var expectedResult = false;
    for (var i = 0; i < columnCount; i++) {
        actualResult = activeSheet.getColumn(i).resizable();
        equal(actualResult, expectedResult, "Removed column resizable on" + i + "column in edit mode !!!");
    }
});

//Row Resizable
module("Row Resizable", {
  beforeEach: function() {
    jQuery('body').append('<div class="eui-view-spread"></div><div class="eui-edit-spread"></div>');
  },
  afterEach: function() {
    jQuery('.eui-view-spread,.eui-edit-spread,.wijspread-popup,.gcStringWidthSpanStyle').remove();
  }
});
test("Apply Row Resizable View Mode", function () {
    var actualResult, expectedResult, response, newSpreadJs, activeSheet, rowCount;
    response = jQuery("body").Excellentable({excellentableId: 1, type: "view", view: {resizableRow: "true"}});
    newSpreadJs = response.getSpreadObject();
    activeSheet = newSpreadJs.getActiveSheet();
    rowCount = activeSheet.getRowCount();
    var expectedResult = true;
    for (var i = 0; i < rowCount; i++) {
        actualResult = activeSheet.getRow(i).resizable();
        equal(actualResult, expectedResult, "Applied Row resizable on" + i + "Row in view mode !!!");
    }
});

test("Remove Row Resizable View Mode", function () {
    var actualResult, expectedResult, response, newSpreadJs, activeSheet, rowCount;
    response = jQuery("body").Excellentable({excellentableId: 1, type: "view", view: {resizableRow: "false"}});
    newSpreadJs = response.getSpreadObject();
    activeSheet = newSpreadJs.getActiveSheet();
    rowCount = activeSheet.getRowCount();
    var expectedResult = false;
    for (var i = 0; i < rowCount; i++) {
        actualResult = activeSheet.getRow(i).resizable();
        equal(actualResult, expectedResult, "Removed Row resizable on" + i + "Row in view mode !!!");
    }
});

test("Apply Row Resizable Edit Mode", function () {
    var actualResult, expectedResult, response, newSpreadJs, activeSheet, rowCount;
    response = jQuery("body").Excellentable({excellentableId: 1, type: "edit", edit: {resizableRow: "true"}});
    newSpreadJs = response.getSpreadObject();
    activeSheet = newSpreadJs.getActiveSheet();
    rowCount = activeSheet.getRowCount();
    var expectedResult = true;
    for (var i = 0; i < rowCount; i++) {
        actualResult = activeSheet.getRow(i).resizable();
        equal(actualResult, expectedResult, "Applied Row resizable on" + i + "Row in Edit mode !!!");
    }
});

test("Remove Row Resizable Edit Mode", function () {
    var actualResult, expectedResult, response, newSpreadJs, activeSheet, rowCount;
    response = jQuery("body").Excellentable({excellentableId: 1, type: "edit", edit: {resizableRow: "false"}});
    newSpreadJs = response.getSpreadObject();
    activeSheet = newSpreadJs.getActiveSheet();
    rowCount = activeSheet.getRowCount();
    var expectedResult = false;
    for (var i = 0; i < rowCount; i++) {
        actualResult = activeSheet.getRow(i).resizable();
        equal(actualResult, expectedResult, "Removed Row resizable on" + i + "Row in Edit mode !!!");
    }
});

module("Minimal Count", {
  beforeEach: function() {
    jQuery('body').append('<div class="eui-view-spread"></div><div class="eui-edit-spread"></div>');
  },
  afterEach: function() {
    jQuery('.eui-view-spread,.eui-edit-spread,.wijspread-popup,.gcStringWidthSpanStyle').remove();
  }
});
test("Get Minimal Data View Mode ", function () {
    var actualResult, expectedResult, response, activeSheet;
    response = jQuery("body").Excellentable({excellentableId: 1, type: "view", view: {showMinimalData: "true"}});  
    activeSheet = response.getSpreadObject().getActiveSheet();    
    actualResult = activeSheet.getRowCount();
    expectedResult = response.getRowCount();
    notEqual(actualResult, expectedResult, "Successfully applied Max Row Count !!!");
    actualResult = activeSheet.getColumnCount();
    expectedResult = response.getColumnCount();
    notEqual(actualResult, expectedResult, "Successfully applied Max Column Count !!!");
});

