/**
* Version          :           1.0.0
* Author           :           Karthik
*/
"use strict";

var actionExcludeList = 'exportasxlsx exportascsv save exit cut copy deleterow deletecolumn import importexcel export excellentableprintarea table picture addlink rangeSort comment help bug feature functionlist keyboardshortcuts about ';

// Used so that operationDiff sends Publish but does not make updated to content -> spreadJSON.
var actionExcludeFromUpdatingContent = 'Publish'

var templateMessage = {
  'importfile' : {
    'title' : AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.collaborative.editor.liveediting.templateMessage.importfile.title"),
    'message' : AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.collaborative.editor.liveediting.templateMessage.importfile.message")
  },
  'Publish' :{
    'title' : AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.collaborative.editor.liveediting.templateMessage.Publish.title"),
    'message' : AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.collaborative.editor.liveediting.templateMessage.Publish.message")
  },
  'VersionRestore':{
    'title' : AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.collaborative.editor.liveediting.templateMessage.VersionRestore.title"),
    'message' : AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.collaborative.editor.liveediting.templateMessage.VersionRestore.message")
  }
}

/**
* @desc Main live editing function that initializes object params
* @param excellentableObject - The Object of excellentable where live editing has to be initialized
* @param options
* @param options.spreadObject - The excellentable spread Object
* @param options.excellentableId - Id of excellentable
* @param options.fbContextPath - The subpath in firebase which is used for communication
* @param options.userId - Unique Id to identify each user / session
* @param options.userName - Name of the user
* @param options.userAvatarUrl - Avatar of the user
* @param options.sessionId - Randomly generated sessionID
* @param options.restoreOptions - In case of restore information to override firebase content
* @return None
*/

var ExcellentLiveOperations = function (options,excellentableObject){
  var self = this;

  self.excellentableObject = excellentableObject;
  self.spreadObject = options.spreadObject;
  self.excellentableId = options.excellentableId;
  self.fbContextPath = options.fbContextPath;
  self.userId =  options.userId;
  self.userName = options.userName;
  self.userAvatarUrl = options.userAvatarUrl;
  self.sessionId = options.sessionId;

  // check if last saved version is same as firebase content
  // Value is set to true when save is performed, and reset when changes are pushed or received
  self.lastSavedSpreadJSON = false;

  // Check if firebase is initiazed successfully before starting live editing
  self.state = false;

  self.containerDiv = $(options.spreadObject._getContainerDiv()).parent();

  /**
  * Some action which are excuted after receiving from firebase through
  * we triggering new action which send out same action back to firebase which causes a loop.
  * In order to avoid , the last action performed is saved and new actions received as check against this stack before executing
  * self.operationsPerformed.lastDeleteAction - holds all delete actions
  */
  self.operationsPerformed = {};
  self.operationsPerformed.lastDeleteAction = [];

  self.restoreOptions = options.restoreOptions

  /**
   * ContentRef will not be updated for changes made thru operationDiff
   * Instead we will use a counter, for every 20 simulateneous ( No spreadDiff in between made by current user and other users)
   * operationDiff we will update contentRef once
  */
  self.operationDiffCounter = 0;
  self.operationDiffCounterLimit = 200;
}

ExcellentLiveOperations.prototype = {

  /**
  * @desc Send notification to user using the AJS messages module
  * @param title - Title of the notification message
  * @param body - Body of the notification message
  * @param status - Type of notification message - error , info , warning ...
  * @return none
  */

  _sendNotifcation : function(title,body,status,options){
    var msgstatus = status ? status : 'error';
    var methodName = { error: 'showErrorMsg', warning: 'showWarningMsg', success: 'showSuccessMsg', info: 'showInfoMsg' };
    var fadeOut = options !== undefined && options.fadeOut ? options.fadeOut : 'true';
    var selector = "#euiMessagePanel";

    jQuery(selector)
      .ExcellentableNotification({
        title: title,
        body: "<p>" + body + "</p>",
        target: selector,
        fadeout: fadeOut
      })[methodName[msgstatus]]();
  },

  /**
  * @desc Initialize firebase connection
  * @param options
  * @param options.apiKey - Firebase API key
  * @param options.databaseURL - Firebase database URL
  * @param options.firebaseToken - Firebase token to authenticate user
  * @return none
  */

  firebaseInit: function(options){
    var self = this;
    // Set the configuration for your app
    var config = {
      apiKey: options.apiKey,
      databaseURL: options.databaseURL
    };

    // Returns a promise so that object waits for firebase initialization before starting live editing
    return new Promise(function (resolve, reject) {
      try{
        if( firebase.apps.length === 0){
          firebase.initializeApp(config);
        }

        // Authenticate the user using the token
        firebase.auth().signInWithCustomToken(options.firebaseToken).then(function(){
          console.log("Authentication successful")
          self.database = firebase.database();
          self.database.goOnline();
          self.state = true;

          resolve();
        }).catch(function(error) {
          var body;

          // Error if authenication fails
          if(error){
            console.log(error.code + "  " + error.message);
            self.state = false;
            body = error.message;
          }else{
            body = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.collaborative.editor.liveediting.firebase.connection.failed.unknown");
          }

          self._sendNotifcation(AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.collaborative.editor.liveediting.failed.title"),body,'error');
          reject();
        });
      }catch(err){
        // Error if firebase fails to initialize
        console.log(err);
        self.state = false;

        self._sendNotifcation(AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.collaborative.editor.liveediting.failed.title"),AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.collaborative.editor.liveediting.firebase.connection.failed.message"),'error');
        reject();
      }
    });
  },

  /**
  * @desc Block user from editing since , not able to register for live editing
  * remove firebase connections if any
  * can be improved over time
  */

  _liveEditingFailed : function(){
    var self = this;
    //either block user or attempt to get context and firebase information again
    self._reInitFirebaseRef()
    //or
    self._closeFBConnections();
  },

  /**
  * @desc if reference in FB doesnt exsits , make call to confluence to reinit
  * can be improved over time
  */

  _reInitFirebaseRef: function(){
    console.log("Make rest call to confluence to get re initialized information")
  },

  // ------------------------------------------------*************-------------------------------------------------
  // Listen for new operation diffs and perform operation on the spread object
  // ------------------------------------------------*************-------------------------------------------------

  /**
  * @desc Listen for new operations from firebase and apply the changes in the sheet
  */

  _listenForNewOperationDiffFromFB : function(){
    var self = this;

    self.operationDiffRef.orderByChild('created').startAt(Date.now()).on('child_added', function(snapshot) {
      if(snapshot.val().userId !== self.userId || snapshot.val().sessionId !== self.sessionId ){
        self._performOperationOnSheet(snapshot);
      }
    });
  },

  /**
  * @desc perform action on the sheet based on the operations received from firebase
  * Using the args and parameter redo the same operation performed by other user
  */

  _performOperationOnSheet : function(snapshot){
    var self = this;

    var opContext = snapshot.val();
    var args = JSON.parse(opContext.args);
    var operationSheet = self.spreadObject.getSheet(opContext.sheetIndex);
    self.lastSavedSpreadJSON = false;

    if( opContext.eventType === 'system' && operationSheet ){
      switch(opContext.sEvent){
        case 'ValueChanged' :
          operationSheet.setValue(args.row,args.col,args.newValue);
          break;
        case 'UserFormulaEntered' :
          operationSheet.setFormula(args.row,args.col,args.formula);
          break;
        case 'DragDropBlock':
          operationSheet.moveTo(args.fromRow,args.fromCol,args.toRow,args.toCol,args.rowCount,args.colCount,args.copyOption);
          break;
        case 'DragFillBlock':
          var rCount;
          var cCount
          if(args.fillDirection == 2 || args.fillDirection == 3){
            rCount = args.location.rowCount + args.fillRange.rowCount;
            cCount = args.fillRange.colCount;
          }else{
            rCount = args.fillRange.rowCount;
            cCount = args.location.colCount + args.fillRange.colCount;
          }
          var start = new GcSpread.Sheets.Range(args.location.row , args.location.col , args.location.rowCount, args.location.colCount);
          var r3 = new GcSpread.Sheets.Range( Math.min(args.location.row,args.fillRange.row) , Math.min( args.location.col,args.fillRange.col) , rCount, cCount);
          operationSheet.fillAutobyDirection(start,r3, args.fillDirection );

          // when spreadJS 10 is enabled
          // operationSheet.fillAutobyDirection(start,r3, { fillType : args.autoFillType , fillDirection: args.fillDirection} );
          break;
        case 'RowChanged':
          if(args.propertyName === 'deleteRows'){
            // this is used to avoid loop ,since the delete event catches this and again triggers a delte to other users
            self.operationsPerformed.lastDeleteAction.push(args);

            operationSheet.getRows(args.row,(args.row+args.count-1) ).backColor('Red');
            setTimeout(function(){
              operationSheet.deleteRows(args.row,args.count);
            },5000);
          }
          break;
        case 'ColumnChanged':
          if(args.propertyName === 'deleteColumns'){
            // this is used to avoid loop ,since the delete event catches this and again triggers a delte to other users
            self.operationsPerformed.lastDeleteAction.push(args);

            operationSheet.getColumns(args.col,(args.col+args.count-1)).backColor('Red');
            setTimeout(function(){
              operationSheet.deleteColumns(args.col,args.count);
            },5000);
          }
          break
        case 'DeleteContent':
          operationSheet.clear(args.range[0].row,args.range[0].col,args.range[0].rowCount,args.range[0].colCount,GcSpread.Sheets.SheetArea.viewport,GcSpread.Sheets.StorageType.Data);
          break;
        case 'SheetNameChanged':
          operationSheet.setName(args.newValue);
          break;
        case 'Publish':
          self.lastSavedSpreadJSON = true;
          self._notifyUserOnChanges(opContext)
          break;

        // The following code is disabled temporarily until sheet replacement can be fixed
        // case 'ClipboardPasted' :
        // case 'UndoOperation' :
        // case 'RedoOperation' :
        //   console.log(opContext.sEvent)
        //   console.log(args.sheet);
        //
        //   if( opContext.sheetIndex === self.spreadObject.getActiveSheetIndex() ){
        //     self._applyNewSheetFromFB(args.sheetJSON)
        //   }else{
        //     operationSheet.fromJSON(args.sheetJSON);
        //   }
        //
        //   break;
        default:
          console.log("System event not defined");
      }
    }else{
      switch(opContext.sEvent){
        case 'FormatChanged' :
          console.log("Custom FormatChanged not defined");
        break;
        default:
          console.log("Custom Event not defined");
      }
    }

    //last completed event
    self.lastCompletedEvent = snapshot.key;
  },

  // ------------------------------------------------*************-------------------------------------------------
  // Listen for spread diff , apply and replace entire spread and listen for operation diff from new timestamp
  // ------------------------------------------------*************-------------------------------------------------

  /**
  * @desc restore user session after the spread is replaced
  * Replacing spread removes editor and cell selection.
  * Restore user content and cursor location if spread is replaced when user is editing
  */

  _restoreUserSession : function(){
    var self = this;

    function setSelectionRange(input, selectionStart, selectionEnd) {
      if (input.setSelectionRange) {
        input.focus();
        input.setSelectionRange(selectionStart, selectionEnd);
      } else if (input.createTextRange) {
        var range = input.createTextRange();
        range.collapse(true);
        range.moveEnd('character', selectionEnd);
        range.moveStart('character', selectionStart);
        range.select();
      }
    }

    function setCaretToPos(input, pos) {
      setSelectionRange(input, pos, pos);
    }

    self.spreadObject.setActiveSheetIndex(self.userSelection.activeSheetIndex);
    var sheet  = self.spreadObject.getSheet(self.userSelection.location.sheet);

    if(sheet){
      sheet.setSelection( self.userSelection.location.row , self.userSelection.location.col, self.userSelection.location.rowCount,self.userSelection.location.colCount );

      if( self.userSelection.editStatus  ){
        sheet.startEdit(false, self.userSelection.content );

        // set cursor location within edit
        var editTextArea = self.containerDiv.find("textarea[gcuielement='gcEditingInput']");
        setCaretToPos( editTextArea[0], self.userSelection.cursorLoc )
      }else{
        //sheet.setActiveCell(self.userSelection.location.row, self.userSelection.location.col);
        // This is a hack since setActiveCell is not focusing on the required cell
        sheet.startEdit();
        sheet.endEdit();
        sheet.setSelection( self.userSelection.location.row , self.userSelection.location.col, self.userSelection.location.rowCount,self.userSelection.location.colCount );
      }
      sheet.showCell( self.userSelection.topViewPort, self.userSelection.leftViewPort );
    }
  },

  /**
  * @desc Notify users on keys changes to the spread sheet
  * @param The args of the diff changes performed by other users
  **/

  _notifyUserOnChanges : function(lastDiffChanges){
    var self = this;
    var event = lastDiffChanges.action || lastDiffChanges.sEvent;
    // IMPROVENT show notificaton to all tabs --  lastDiffChanges.sessionId !== self.userRefContent.sessionId
    if( templateMessage[event] !== undefined && lastDiffChanges.userId !== self.userRefContent.userId ){
      self._sendNotifcation(templateMessage[event].title, templateMessage[event].message.replace('{user}', lastDiffChanges.userName) ,'info',{'fadeOut':false});
    }
  },

  /**
  * @desc Every Spead replacement has an field
  * @param lastOperationKey which stores the last operationDiff that was performed on the sheet.
  * This function applies all the operation that are not applied ever since
  */

  _applyPrevOperationalChanges  : function(lastOperationKey){
    var self = this;

    self.operationDiffRef.orderByKey().startAt(lastOperationKey).once('value').then(function(snapshot){
      // SUSPEND sheet operation if temporary data consistency vs performance
      // Make sure to skip the first operation which is redundant

      var skip = 1;
      snapshot.forEach(function(operation){
        // Skip lastAppliedOperation and apply everything since then
        if(skip){ skip--; return; }
        self._performOperationOnSheet(operation);
      })
    })
  },

  /**
  * @desc Every Spead replacement has an field
  * @param lastOperationKey which stores the last operationDiff that was performed on the sheet.
  * If lastOperationKey is Null then apply all operations from the operationDiff
  */

  _applyAllOperationalChanges : function(){
    var self = this;

    self.operationDiffRef.orderByKey().once('value').then(function(snapshot){
      // SUSPEND sheet operation if temporary data consistency vs performance
      // Make sure to skip the first operation which is redundant

      var skip = 1;
      snapshot.forEach(function(operation){
        if(skip){ skip--; return; }
        self._performOperationOnSheet(operation);
      })
    })
  },

  /**
  * @desc Replace content of the current spread without loosing user location information
  * @param newSpread is hash which consists of the spread JSON that will replace the current spread sheet content
  */

  _applyNewSpreadFromFB : function(newSpread){
    var self = this;

    var activeSelection = self._getCurrentUserSelection();
    var activeSheet = self.spreadObject.getActiveSheet();
    var editTextArea = self.containerDiv.find("textarea[gcuielement='gcEditingInput']");

    // save user location information before replacing spread
    self.userSelection = {
      location :  activeSelection,
      content : editTextArea.val()  ,
      editStatus : activeSheet.editorStatus(),
      cursorLoc : editTextArea.prop("selectionStart"),
      topViewPort : activeSheet.getViewportTopRow(1),
      leftViewPort : activeSheet.getViewportLeftColumn(1),
      activeSheetIndex : self.spreadObject.getActiveSheetIndex()
    }

    self.spreadObject.isPaintSuspended(true);
    self.excellentableJSON = self._unzipContent( newSpread.spread );

    self.spreadObject.fromJSON( JSON.parse( self.excellentableJSON ) );
    self._fixSpreadAfterLoading();
    self.spreadObject.isPaintSuspended(false);

    // Notify other user about restore
    self._notifyUserOnChanges(newSpread);

    self._initAllUserPosition();
    self._restoreUserSession();

    //init mouse click and drag event to pause updates from spread
    self._initPauseSpreadUpdateWhenDraggingAndDropping();

    if( newSpread.lastCompletedEvent ){
      self._applyPrevOperationalChanges( newSpread.lastCompletedEvent );
    }
  },

  /**
  * @desc Replace content of the specific sheet in the spread without loosing user location information
  * @param newSheet is hash which consists of the sheet JSON that will replace specific sheet in the spread
  */

  _applyNewSheetFromFB : function(newSheet){
    var self = this;

    var activeSelection = self._getCurrentUserSelection();
    var activeSheet = self.spreadObject.getActiveSheet();
    var editTextArea = self.containerDiv.find("textarea[gcuielement='gcEditingInput']");

    self.userSelection = {
      location :  activeSelection,
      content : editTextArea.val()  ,
      editStatus : activeSheet.editorStatus(),
      cursorLoc : editTextArea.prop("selectionStart"),
      topViewPort : activeSheet.getViewportTopRow(1),
      leftViewPort : activeSheet.getViewportLeftColumn(1),
      activeSheetIndex : self.spreadObject.getActiveSheetIndex()
    }

    newSheet.selections["0"] = self.userSelection.location;
    newSheet.activeRow = self.userSelection.location.row;
    newSheet.activeCol = self.userSelection.location.col;

    activeSheet.isPaintSuspended(true);
    activeSheet.fromJSON( newSheet );
    self._fixSpreadAfterLoading();
    activeSheet.isPaintSuspended(false);

    self.excellentableJSON = self.spreadObject.toJSON();
    self._initExternalUserPosition();
  },

  /**
  * @desc Listen for new spread sheet updated from firebase
  * if pauseSpread is enable then pause all spread replacement
  */

  _listenForNewSpreadDiffFromFB : function(){
    var self = this;

    self.lastSavedSpreadJSON = false;
    self.spreadDiffRef.orderByChild('created').startAt(Date.now()).on('child_added', function(snapshot) {
      if(snapshot.val().userId !== self.userId || snapshot.val().sessionId !== self.sessionId ){
        if(self.pauseSpread){
          self.lastSpreadUpdate = snapshot.val();
        }else{
          self._applyNewSpreadFromFB(snapshot.val());
        }

        // reset operationDiff counter
        self.operationDiffCounter = 0;
      }
    });
  },

  /**
  * @desc In order to make sure the user experience doesnt break when replacing spread content
  * Pause spread replacement action when user is in the middle following Events
  * autocompleting
  * User in the middle of draging and dropping
  * User in the middle of autofiling
  * Selection changing
  */

  _initPauseSpreadUpdateDuringContinuousOperations : function(){
    var self = this;

    // Autocomplete
    var EditChange = GcSpread.Sheets.Events.EditChange;
    self.spreadObject.bind(EditChange, function(e,args) {
      if( args.editingText && args.editingText.match(/^={1,2}[(0-9)(a-z)]*/) ){
        self.pauseSpread = true;
      }else{
        self.pauseSpread = false;
        self._resumeSpreadPausedDuringContinuousOperations();
      }
    });

    var EditEnd = GcSpread.Sheets.Events.EditEnd;
    self.spreadObject.bind(EditEnd, function(e,args) {
      //wait for EditEnd event to complete before replacing the spread object
      setTimeout(function(){
        self.pauseSpread = false;
        self._resumeSpreadPausedDuringContinuousOperations();
      },100);
    });

    // Drag and drop event completed
    var DragDropBlockCompleted = GcSpread.Sheets.Events.DragDropBlockCompleted;
    self.spreadObject.bind(DragDropBlockCompleted, function(e,args) {
      setTimeout(function(){
        self.pauseSpread = false;
        self._resumeSpreadPausedDuringContinuousOperations();
      },100);
    });

    // Drag and fill event completed
    var DragFillBlockCompleted = GcSpread.Sheets.Events.DragFillBlockCompleted;
    self.spreadObject.bind(DragFillBlockCompleted, function(e,args) {
      setTimeout(function(){
        self.pauseSpread = false;
        self._resumeSpreadPausedDuringContinuousOperations();
      },10);
    });


    // Pause on while selection changing
    var SelectionChanging = GcSpread.Sheets.Events.SelectionChanging;
    self.spreadObject.bind(SelectionChanging, function(e,args) {
      self.pauseSpread = true;
    });

    var SelectionChanged = GcSpread.Sheets.Events.SelectionChanged;
    self.spreadObject.bind(SelectionChanged, function(e,args) {
      setTimeout(function(){
        self.pauseSpread = false;
        self._resumeSpreadPausedDuringContinuousOperations();
      },100);
    });
  },

  /**
  * @desc In order to make sure the user experience doesnt break when replacing spread content
  * Pause spread replacement action when user is in the middle following Events
  * Since there are no events to tie into dragdrop action , currently using jquery to track dragdrop
  * adding overridding keyMap of certain events since there are no event available to handle them
  * For each sheet
  * delete ( delete key and backspace key)
  * Undo / Redo Operation
  */

  _initPauseSpreadUpdateWhenDraggingAndDropping : function(){
    var self = this;

    var mouseDownHold = false;
    var timeoutId = 0;
    self.containerDiv.parent().find('canvas:nth-child(1)').on('mousedown', function() {
      timeoutId = setTimeout(function(){
        self.pauseSpread = true;
        mouseDownHold = true;
      }, 100);
    }).on('mouseup mouseleave', function() {
      clearTimeout(timeoutId);
      if(mouseDownHold){
        // self.pauseSpread = false;
        // self._resumeSpreadPausedDuringContinuousOperations();
        mouseDownHold = false;
      }
    })

    // Override original keyMap
    self.spreadObject.sheets.forEach(function(sheet){

      // Listen for delete action using keyMap
      // overriden delete with custom delete
      function overRideWithCustomDeleteMethod(){
        var args = {};

        if (!sheet.isEditing()){
          var ranges = sheet.getSelections();
          var action = new GcSpread.Sheets.UndoRedo.ClearValueUndoAction(sheet, ranges);
          if (action.canExecute(sheet)){
            sheet._doCommand(action);

            //Custom code to perform action and push operation
            args.range = ranges;
            args.sheet = sheet;
            args.sheetName = sheet.getName();
            self._sendUpdateSheetEvent('DeleteContent',args);
          }
        }
      }
      sheet.addKeyMap(GcSpread.Sheets.Key.del, false, false, false, false, overRideWithCustomDeleteMethod);
      // add keymap to map delete with backspace
      sheet.addKeyMap(GcSpread.Sheets.Key.backspace, false, false, false, false, overRideWithCustomDeleteMethod);

      // override undo
      sheet.addKeyMap(GcSpread.Sheets.Key.z, true , false, false, false, function(){
        var args = {};
        console.log("Undo triggered")

        var undoManager = sheet.undoManager();
        if (undoManager.canUndo()){
           undoManager.undo()

          //  args.sheet = self.spreadObject.getActiveSheet();
          //  args.sheetJSON = self.spreadObject.getActiveSheet().toJSON();
          //  self._sendUpdateSheetEvent('UndoOperation',args);
           self._sendUpdateSpreadEvent('UndoOperation');
        }
      });

      // override redo
      sheet.addKeyMap(GcSpread.Sheets.Key.y, true, false, false, false, function(){
        var args = {};
        console.log("Redo triggered")

        var undoManager = sheet.undoManager();
        if (undoManager.canRedo()){
            undoManager.redo()

            // args.sheet = self.spreadObject.getActiveSheet();
            // args.sheetJSON = self.spreadObject.getActiveSheet().toJSON();
            // self._sendUpdateSheetEvent('RedoOperation',args);
            self._sendUpdateSpreadEvent('RedoOperation');
        }
      });
    });

    // Init hyperlink
    self.excellentableObject.ExcInsertHyperlinkEvents(undefined, self.spreadObject);
  },

  _resumeSpreadPausedDuringContinuousOperations : function(){
    var self = this;

    if( self.lastSpreadUpdate ){
      self._applyNewSpreadFromFB(self.lastSpreadUpdate);
      self.lastSpreadUpdate = null;
    }
  },


  // ------------------------------------------------*************-------------------------------------------------
  // Listen for cell content change and push updates to firebase
  // ------------------------------------------------*************-------------------------------------------------

  /**
  * @desc Announce that the content has been published
  */

  annouceSaveOperation : function(){
    var self = this;
    var args = {
      sheet : self.spreadObject.getActiveSheet()
    }

    // update contentRef onSave, since we are not updating contentRef on all operationDiff changes
    // Set counter value over limit to force a push to contentRef
    self.operationDiffCounter = self.operationDiffCounterLimit + 1;

    self._sendUpdateSheetEvent('Publish',args);
  },

  /**
  * @desc various events which are captured and are propagated as either operations or spead replacemen
  * These are sent to firebase and are propagated to other users in the same excellentable
  */

  _initCellContentChange : function(){
    var self = this;

    // Listen for delete action using keystroke
    // is handled as part of _initPauseSpreadUpdateWhenDraggingAndDropping

    var RangeSorted = GcSpread.Sheets.Events.RangeSorted;
    self.spreadObject.bind(RangeSorted, function (e, args) {
       self._sendUpdateSpreadEvent('SortDialog');
    });

    var SheetNameChanged = GcSpread.Sheets.Events.SheetNameChanged;
    self.spreadObject.bind(SheetNameChanged, function(e,args) {
      self._sendUpdateSheetEvent('SheetNameChanged',args);
    });

    var ClipboardPasted = GcSpread.Sheets.Events.ClipboardPasted;
    self.spreadObject.bind(ClipboardPasted, function(e,args) {
      setTimeout(function(){
        // args.sheet = self.spreadObject.getActiveSheet();
        // args.sheetJSON = self.spreadObject.getActiveSheet().toJSON();
        // self._sendUpdateSheetEvent('ClipboardPasted',args);
        self._sendUpdateSpreadEvent('ClipboardPasted');
      },100);
    });

    var DragDropBlockCompleted = GcSpread.Sheets.Events.DragDropBlockCompleted;
    self.spreadObject.bind(DragDropBlockCompleted, function(e,args) {
      self._sendUpdateSheetEvent('DragDropBlock',args);
    });

    var DragFillBlockCompleted = GcSpread.Sheets.Events.DragFillBlockCompleted;
    self.spreadObject.bind(DragFillBlockCompleted, function(e,args) {
      args.location = self._getCurrentUserSelection();
      //self._sendUpdateSheetEvent('DragFillBlock',args);
      self._sendUpdateSpreadEvent('DragFillBlock');
    });

    var RowChanged = GcSpread.Sheets.Events.RowChanged;
    self.spreadObject.bind(RowChanged, function(e,args) {
      if(args.propertyName === 'deleteRows'){
        var eventd = self.operationsPerformed.lastDeleteAction.pop();
        if(!eventd || (eventd.row !== args.row && eventd.count !== args.count)){
          self._sendUpdateSheetEvent('RowChanged',args);
        }
      }
    });

    var ColumnChanged = GcSpread.Sheets.Events.ColumnChanged;
    self.spreadObject.bind(ColumnChanged, function(e,args) {
      if(args.propertyName === 'deleteColumns'){
        var eventd = self.operationsPerformed.lastDeleteAction.pop();
        if(!eventd || (eventd.col !== args.col && eventd.count !== args.count)){
          self._sendUpdateSheetEvent('ColumnChanged',args);
        }
      }
    });

    var UserFormulaEntered = GcSpread.Sheets.Events.UserFormulaEntered;
    self.spreadObject.bind(UserFormulaEntered, function(e,args) {
      self._sendUpdateSheetEvent('UserFormulaEntered',args);
    });

    var ValueChanged = GcSpread.Sheets.Events.ValueChanged;
    self.spreadObject.bind(ValueChanged, function(e,args) {
      // if event triggered because of formatting from menu bar
      if(args.customEventTriggeredFromMenuBar ){
        if(actionExcludeList.indexOf(args.action) === -1){
          self._sendUpdateSpreadEvent(args.action);
        }else{
          // Exclude action either doesnt matter or taken care by other events
        }

      }else{
        self._sendUpdateSheetEvent('ValueChanged',args);
      }
    });

    var PictureRemoved = GcSpread.Sheets.Events.FloatingObjectRemoved;
    self.spreadObject.bind(PictureRemoved, function () {
        self._sendUpdateSpreadEvent('PictureRemoved');
    });
  },

  /**
  * @desc Send operation update to firebase
  * @param sEvent - The event in execllenatbe which triggered the updated
  * @param args - The arguments that are part of / required by other instances to perform the same
  */

  _sendUpdateSheetEvent : function(sEvent,args){
    var self = this;

    var newOp = self.operationDiffRef.push();
    newOp.then(function(){
      var sheetName = args.sheet._name;
      delete args.sheet
      newOp.set({
        userId : self.userId,
        userName : self.userName,
        sessionId : self.sessionId,
        eventType : 'system',
        args : JSON.stringify(args),
        sEvent : sEvent,
        created : firebase.database.ServerValue.TIMESTAMP,
        sheetIndex : self.spreadObject.getSheetIndex(sheetName)
      })

      // Increment operationDiff counter
      self.operationDiffCounter++
      self.lastCompletedEvent = newOp.key;

      // Push Diff only when operation count is more than limit
      if( self.operationDiffCounter > self.operationDiffCounterLimit ){
        self._pushDiffToFB();
      }
    }).catch(function(err){
      console.log(err)
    });
  },

  /**
  * @desc Send spread sheet replacement action to firebase
  * @param sEvent - The event in execllenatbe which triggered the updated
  * @param args - The arguments that are part of / required by other instances to perform the same
  */

  _sendUpdateSpreadEvent : function(action){
    var self = this;

    var newSp = self.spreadDiffRef.push();
    newSp.then(function(){
      var zippedExcellentableJSON = self._zipContent( self._getDiffFromTable() )
      newSp.set({
        userId : self.userId,
        userName : self.userName,
        sessionId : self.sessionId,
        spread : zippedExcellentableJSON,
        created : firebase.database.ServerValue.TIMESTAMP,
        lastCompletedEvent : self.lastCompletedEvent ?  self.lastCompletedEvent : null,
        action : action
      })

      self._pushDiffToFB(zippedExcellentableJSON);
    }).catch(function(err){
      self._closeFBConnections();
      self._sendNotifcation('Spreadsheet too large', 'The current spreadsheet is too large. Collaborative editing will be disabled. We are working diligently to allow this functionality in a future release', 'warning');
    });

  },

  /**
  * @desc get the spread content in JSON format
  * Delete active sheet and user selection information before sending to other users
  * @return JSON of spread sheet
  */

  _getDiffFromTable : function(){
    var self = this;
    var spreadContent = self.spreadObject.toJSON();

    // active selection from spread JSON before pushing content to remote
    for (var prop in spreadContent.sheets ) {
      if(spreadContent.sheets[prop].activeCol){
        delete spreadContent.sheets[prop].activeCol;
      }
      if(spreadContent.sheets[prop].activeRow){
        delete spreadContent.sheets[prop].activeRow;
      }
      if(spreadContent.sheets[prop].selections){
        delete spreadContent.sheets[prop].selections;
      }
    }

    return JSON.stringify(spreadContent);
  },

  /**
  * @desc Push lastest JSON of spread to firebase
  * along with the last completed operation key
  */

  _pushDiffToFB : function(zippedExcellentableJSON){
    var self = this;

    //reset operationDiffCounter
    self.operationDiffCounter = 0;

    self.excellentableJSON = self._getDiffFromTable();
    self.lastSavedSpreadJSON = false;

    if( zippedExcellentableJSON === undefined ){
        zippedExcellentableJSON = self._zipContent( self.excellentableJSON )
    }

    var contentHash;
    if( self.lastCompletedEvent ){
      contentHash = {
        spreadJSON : zippedExcellentableJSON,
        created : firebase.database.ServerValue.TIMESTAMP,
        lastCompletedEvent : self.lastCompletedEvent ?  self.lastCompletedEvent : null
      }
    }else{
      contentHash = {
        spreadJSON : zippedExcellentableJSON,
        created : firebase.database.ServerValue.TIMESTAMP
      }
    }

    try{
      self.contentRef.update(contentHash, function(err, data) {
        if(err){
          console.log(err);
        }
      });
    }
    catch(error){
      self._closeFBConnections();
      self._sendNotifcation('Spreadsheet too large', 'The current spreadsheet is too large. Collaborative editing will be disabled. We are working diligently to allow this functionality in a future release', 'warning');
    }
  },



  // ------------------------------------------------*************-------------------------------------------------
  // User tracking functions
  // ------------------------------------------------*************-------------------------------------------------


  /**
  * @desc Register new users in the user tracking collection
  * and remove user on disconnect
  * assign unique color for each user
  */

  _registerUser : function(){
    var self = this;
    var colorCollection = ['#21A0A0','#DA4167','#144F9E','#96E072','#FE7F2D','#F34213','#71B340','#00ACE5','#28166F','#444444','#FDE74C','#006838','#8A2BE2','#34495E','#CDDC39','#FFC107','#B38100','#4CAF50','#8D6D63','#4DD0E1'];

    self.userRefContent = {
      userId: self.userId,
      sessionId : self.sessionId,
      userColor : colorCollection[Math.floor(Math.random()*colorCollection.length)] ,
      userName : self.userName,
      created : new Date().getTime(),
      userAvatarUrl : self.userAvatarUrl,
      updated : firebase.database.ServerValue.TIMESTAMP
    };
    self.userRef.set( self.userRefContent );

    self.cellPosition = [];
    self.cellPosition["new"] = self._getCurrentUserSelection();
    self.cellPosition["old"] = self.cellPosition["new"];
    self.userLocationRef.set(self.cellPosition);

    // when user disconnects , remove user from user list
    self.userRef.onDisconnect().remove(function(err){
      if(err){
        console.log("Unable to remove user " + err);
      }
    });
  },

  /**
  * @desc return the current user selection
  */

  _getCurrentUserSelection : function(){
    var self = this;

    var sheet = self.spreadObject.getActiveSheet();
    var firstCell = {row:0, col: 0, colCount: 1, rowCount: 1};
    var selectionrange = sheet.getSelections();
    var selectionObj = (selectionrange.length === 0) ? firstCell : sheet.getSelections()[0];

    return({
      sheet : self.spreadObject.getActiveSheetIndex(),
      row : selectionObj.row,
      col : selectionObj.col,
      colCount : selectionObj.colCount,
      rowCount : selectionObj.rowCount
    });
  },

  /**
  * @desc Track current user postion in the sheet and update external users on cell postion change
  */

  _initCellTracking : function(){
    var self = this;

    //At the start editing event update location of user
    var EnterCell = GcSpread.Sheets.Events.EnterCell;
    self.spreadObject.bind(EnterCell, function(e,args) {
      self.cellPosition["old"] = self.cellPosition["new"];

      self.cellPosition["new"] = {
        sheet : self.spreadObject.getActiveSheetIndex(),
        row : args.row,
        col : args.col,
        colCount : 1,
        rowCount : 1
      }

      self.userRef.update(self.userRefContent);
      self.userLocationRef.set(self.cellPosition);
    });
  },

  /**
  * @desc Check in firebase when external user leaves and remove specific user tracking location from the sheet
  */

  _listenForUserLeave : function(){
    var self = this;

    self.userKeysRef.on('child_removed', function(snapshot) {
      if(snapshot.val().userId !== self.userId || snapshot.val().sessionId !== self.sessionId ){
        self._resetUserLocation(snapshot.val());
      }else{
        // make call to excellentable to notify about user leave
      }
      //Remove avatar
      if (typeof self.avatarManager === "undefined") {
        self.avatarInitalizeStatus.done(function () {
          self.avatarManager.removeUser(snapshot.val());
        })
      } else {
          self.avatarManager.removeUser(snapshot.val());
      }
    });
  },

  /**
  * @desc Add new avatar when new external user joins the spread editing
  */

  _listenForUserJoin: function () {
    var self = this;
    self.userKeysRef.on('child_added', function(snapshot) {
      if (typeof self.avatarManager === "undefined") {
        self.avatarInitalizeStatus.done(function () {
          self.avatarManager.addUser(snapshot.val());
        })
      } else {
        self.avatarManager.addUser(snapshot.val());
      }
    });
  },

  /**
  * @desc Get initial position of all users in the sheet and track it the sheet
  */

  _initAllUserPosition : function(){
    var self = this;

    self.userKeysRef.once("value", function(snapshot) {
      var allUser = snapshot.val();
      //Initialize Avatars Manager

      self.avatarManager = new AvatarManagerLive("#euiUserImageArea", 30, self.sessionId);
      self.avatarManager.initialize(allUser, self.avatarInitalizeStatus);

      for (var userSession in allUser) {
        if(allUser[userSession].userId !== self.userId || allUser[userSession].sessionId !== self.sessionId ){
          self._setExternalUserLocation(allUser[userSession]);
        }else{
          self._setInternalUserLocation(allUser[userSession]);
        }
        self._removeExpiredUserSessions(allUser[userSession],userSession);
      }
    });
  },


  /**
  * @desc Remove user session from userKeys if user position has not been updated in 15 mins
  * @param the session to check and remove
  */

  _removeExpiredUserSessions : function(userSessionObj,userSession){
    var self = this;

    if( userSessionObj.sessionId === undefined ||
          userSessionObj.updated === undefined ||
          userSessionObj.updated <  ( new Date().getTime() - ( 15 * 60 * 1000 ) ) ){
      var rmap = {}
      rmap["/" + userSession] = null;
      self.userKeysRef.update(rmap);
    }
  },


  /**
  * @desc Get initial position of external users in the sheet and add it
  */

  _initExternalUserPosition : function(){
    var self = this;

    self.userKeysRef.once("value", function(snapshot) {
      var allUser = snapshot.val();
      for (var userSession in allUser) {
        if(allUser[userSession].userId !== self.userId || allUser[userSession].sessionId !== self.sessionId ){
          self._setExternalUserLocation(allUser[userSession]);
        }
      }
    });
  },

  /**
  * @desc Update external users location in the sheet when cell is changed
  */

  _listenForUserMove : function(){
    var self = this;

    self.userKeysRef.on('child_changed', function(snapshot) {
      if(snapshot.val().userId !== self.userId || snapshot.val().sessionId !== self.sessionId ){
        // remove old location highlight and set new cell location
        self._setExternalUserLocation(snapshot.val());
      }
    });
  },

  /**
  * @desc Create cell highlight to track external users
  * @param userColor - the color of the user , so it can match the cell border color
  * @param userCharacter - the first character of the user to show as part of cell tracking
  */

  _setCellHighlight : function(userColor, userCharacter){
    var self = this;

    var coloredCell = new GcSpread.Sheets.TextCellType();
    coloredCell.paint = function(ctx, value, x, y, w, h, style, options) {
      $.wijmo.wijspread.TextCellType.prototype.paint.apply(this,arguments);

      var rectBox = 20;
      var tmpFillStyle = ctx.fillStyle;
      var tmpFont = ctx.font;
      var tmpStrokeStyle = ctx.strokeStyle;

      //Empty Rect Box around the cell
      ctx.beginPath();
      ctx.lineWidth = 3;
      ctx.strokeStyle = userColor;
      ctx.fillStyle = userColor;
      ctx.rect(x+2,y+2,w-4,h-4);
      ctx.stroke();
      ctx.closePath();


      //Flag code
      //Small rect box with text
      ctx.beginPath();
      ctx.moveTo(x+w,y-rectBox);
      ctx.lineTo(x+w,y);
      ctx.fillRect(x+w, y-rectBox, rectBox, rectBox);
      ctx.stroke();
      ctx.closePath();

      //Text in Rect Box
      ctx.beginPath();
      ctx.fillStyle = "#ffffff";
      ctx.font = "bold 14pt Arial";
      ctx.fillText(userCharacter, x+w+rectBox-3, y-4);//TODO text character not set yet
      ctx.stroke();
      ctx.closePath();

      //Restoring canvas to orignal state
      ctx.font = tmpFont;
      ctx.fillStyle = tmpFillStyle;
      ctx.strokeStyle = tmpStrokeStyle;
    };
    return coloredCell;
  },

  /**
  * @desc mark the external user location on the cell
  * @param userInfo - A hash which contains information about external user
  */

  _setExternalUserLocation : function(userInfo){
    var self = this;

    if( ! userInfo.location ){
      return;
    }

    var newSheet = self.spreadObject.getSheet(userInfo.location.new.sheet);
    var oldSheet = self.spreadObject.getSheet(userInfo.location.old.sheet);

    var oldCell = userInfo.location.old;
    var newCell = userInfo.location.new;

    if ( oldCell !== null && oldSheet){
      oldSheet.getCell(oldCell.row,oldCell.col).cellType(new GcSpread.Sheets.TextCellType());
    }
    if ( newCell !== null && userInfo.userColor && newSheet){
      newSheet.getCell(newCell.row,newCell.col).cellType( self._setCellHighlight(userInfo.userColor , userInfo.userName[0]) );
    }
  },

  /**
  * @desc mark the current user location on the cell as active cell
  * @param userInfo - A hash which contains information about current user
  */

  _setInternalUserLocation : function(userInfo){
    var self = this;

    if( ! userInfo.location ){
      return;
    }

    var sheet  = self.spreadObject.getSheet(userInfo.location.new.sheet);
    if(sheet){
      sheet.setSelection( userInfo.location.new.row , userInfo.location.new.col, userInfo.location.new.rowCount, userInfo.location.new.colCount );
      //sheet.startEdit(false);
      sheet.setActiveCell( userInfo.location.new.row , userInfo.location.new.col);
    }
  },

  /**
  * @desc reset the highlight on the cell which was used to mark an external user
  * @param userInfo - A hash which contains information about external user
  */

  _resetUserLocation : function(userInfo){
    var self = this;

    if( ! userInfo.location ){
      return;
    }

    var newSheet = self.spreadObject.getSheet(userInfo.location.new.sheet);
    var oldSheet = self.spreadObject.getSheet(userInfo.location.old.sheet);

    var oldCell = userInfo.location.old;
    var newCell = userInfo.location.new;

    if ( oldCell !== null && oldSheet){
      oldSheet.getCell(oldCell.row,oldCell.col).cellType(new GcSpread.Sheets.TextCellType());
    }
    if ( newCell !== null && newSheet){
      newSheet.getCell(newCell.row,newCell.col).cellType(new GcSpread.Sheets.TextCellType());
    }
  },

  // ------------------------------------------------*************-------------------------------------------------
  // Initialize excellentable when opened
  // ------------------------------------------------*************-------------------------------------------------

  /**
  * @desc Initialize content of spread based on the content from firebase during the start of live editing
  * @param initContent - A hash which contains information about initial spread JSON content
  * Apply operationDiff based on the lastCompletedEvent param
  */

  _initExcellentableContent : function(initContent){
    var self = this;

    if( initContent !== null && initContent.content != '' && self.excellentableJSON != ''){
      self.spreadObject.isPaintSuspended(true);
      self.spreadObject.fromJSON( JSON.parse( self.excellentableJSON ) );
      self._fixSpreadAfterLoading();
      self.spreadObject.isPaintSuspended(false);

      if( initContent.lastCompletedEvent ){
        self._applyPrevOperationalChanges( initContent.lastCompletedEvent );
      }else{
        // should it apply all changes from the operationDiff when no last action is specified
        self._applyAllOperationalChanges();
      }
    }

    self._listenForNewOperationDiffFromFB();
    self._listenForNewSpreadDiffFromFB();
    self._initPauseSpreadUpdateDuringContinuousOperations();
    self._initPauseSpreadUpdateWhenDraggingAndDropping();
  },

  // ------------------------------------------------*************-------------------------------------------------
  // Close and Exit Operations
  // ------------------------------------------------*************-------------------------------------------------

  /**
  * @desc clear excellentable if the current user is the last person to leave
  * and then call resolve promise to disconnect from firebase
  */

  _removeSpreadContentIfLastUser : function(){
    var self = this;
    var cleanUpDeferred = $.Deferred();

    self.userKeysRef.transaction(function(count){
        if( ( Object.keys(count).length === 1 ) && self.lastSavedSpreadJSON ){
            var oPromise = self.operationDiffRef.remove();
            var sPromise = self.spreadDiffRef.remove();
            var cPromise = self.contentRef.remove();

            $.when(oPromise,sPromise,cPromise).then(function(op,sp,cp){
              cleanUpDeferred.resolve();
            });
        }else{
            cleanUpDeferred.resolve();
        }
    });

    return cleanUpDeferred.promise();
  },


  /**
  * @desc set last saved metadata here so that we can check and remove content when last user exists
  */

  setLastSavedSpreadJSON : function(changeFlag){
      var self = this;
      self.lastSavedSpreadJSON = changeFlag;
  },

  /**
  * @desc Close all association between execellentable and firebase live editing
  */

  closeExcellentableConnection : function(){
    var self = this;

    // clear excellentable if the current user is the last person to leave
    self._removeSpreadContentIfLastUser().then(function(){
      self._closeFBConnections();
    });

    //check for unsaved content - prompt User

  },

  /**
  * @desc Close all connections to firebase as part of live editing
  */

  _closeFBConnections : function(){
    var self = this;

    self.userKeysRef.orderByChild('userName').startAt(self.userName).once('value',function(snapshot){
      self.database.goOffline();

      // NEED TO DIFFERENTIATE BETWEEN USER ACROSS TABS VS SYSTEMS
      if(snapshot.numChildren() === 1){
        firebase.auth().signOut().then(function() {
          console.log("User signed out successfully");
        }).catch(function(error) {
          console.error("User signed out failed " + error);
        });
      }
    });
  },

  // ------------------------------------------------*************-------------------------------------------------
  // On restore force content change to all users
  // ------------------------------------------------*************-------------------------------------------------

  /**
  * @desc During Initialization , if restore is enabled then user content is pushed to firebase to overwrite prev content
  * @param metaData - Consists of the JSON content that will used to restore the current sheet
  * the content is also pushed to firebase to overwrite existing data
  */

  _onRestoreReplaceAllUserContent : function(metaData){
    var self = this;

    if( metaData.trim() !== '' ){
      self._applyNewSpreadFromFB({
         spread : metaData
      });
    }

    metaData = self._zipContent( JSON.stringify(self.spreadObject.toJSON()) );

    var contentHash = {
      spreadJSON : metaData,
      created : firebase.database.ServerValue.TIMESTAMP,
      lastCompletedEvent : 'SkipForVersionRestore' + new Date().getTime()
    }

    self.contentRef.set(contentHash);
    self._sendUpdateSpreadEvent('VersionRestore');

    self._listenForNewOperationDiffFromFB();
    self._listenForNewSpreadDiffFromFB();
    self._initPauseSpreadUpdateDuringContinuousOperations();
    self._initPauseSpreadUpdateWhenDraggingAndDropping();
  },


  // ------------------------------------------------*************-------------------------------------------------
  // Automate users for testing
  // ------------------------------------------------*************-------------------------------------------------

  /**
  * @desc option to add automated users to test live editing
  */

  _automateUsers : function(){
    self = this;

    self.userKeysRef.once('value',function(snapshot){
      if(snapshot.numChildren() > 1){

        var automatedInterval = setInterval(function(){
          var sheet = self.spreadObject.getActiveSheet();

          let randomrow = Math.floor(Math.random(20)*20);
          let randomcol =  Math.floor(Math.random(20)*10);

          sheet.setActiveCell( randomrow , randomcol);
          sheet.setValue(randomrow,randomcol,'value'+randomrow+' '+randomcol)

          self.spreadObject._trigger(GcSpread.Sheets.Events.ValueChanged,{
            col : sheet.getActiveColumnIndex() ,
            newValue  : 'value'+randomrow+' '+randomcol,
            oldValue  : 'value'+randomrow+' '+randomcol,
            row : sheet.getActiveRowIndex() ,
            sheet : sheet,
            sheetName : sheet.getName()
          });

          self.spreadObject._trigger(GcSpread.Sheets.Events.EnterCell,{
            col : sheet.getActiveColumnIndex() ,
            row : sheet.getActiveRowIndex() ,
            sheet : sheet,
            sheetName : sheet.getName()
          });

          // click on bold
          //$('.excellent-iconbar').find('ul:nth-child(4) li:nth-child(1)').click();
        },2000);

        setTimeout(function(){
          clearInterval(automatedInterval);
        }, 150000);
      }
    })
  },

  // ------------------------------------------------*************-------------------------------------------------
  // functions to run after spread is loaded from JSON
  // ------------------------------------------------*************-------------------------------------------------

  /**
  * @desc After loading spread from JSON , run the following functions to fix micellaneous items
  */

  _fixSpreadAfterLoading : function(){
    var self = this;

    self.excellentableObject.adjustMinimumRowsAndColumnsAtEditMode();
    jQuery('body').ExcellentableTabStrip.update();
  },

  // ------------------------------------------------*************-------------------------------------------------
  // Firebase Connection staus
  // ------------------------------------------------*************-------------------------------------------------

  /**
  * @desc Notify users when firebase connection status change
  */

  _checkFirebaseState : function(){
    var self = this;

    var connectedRef = firebase.database().ref(".info/connected");
    connectedRef.on("value", function(snap) {
      if (snap.val() === true) {
        self._sendNotifcation(AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.collaborative.editor.liveediting.firebase.connection.active.title"),AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.collaborative.editor.liveediting.firebase.connection.active.message"),'info');
      } else {
        self._sendNotifcation(AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.collaborative.editor.liveediting.firebase.connection.inactive.title"),AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.collaborative.editor.liveediting.firebase.connection.inactive.message"),'warning');
      }
    });
  },


  // ------------------------------------------------*************-------------------------------------------------
  // Zip / Unzip spreadjs metadata before sending / receiving from FB
  // ------------------------------------------------*************-------------------------------------------------

  /**
  * @desc zip metadata provided and return content
  */

  _zipContent : function(metaData){
      var gZippedInitialData = pako.gzip(metaData, { to: 'string' });
      var gzippedInitialEncode = window.btoa(gZippedInitialData)
      return gzippedInitialEncode
  },

  /**
  * @desc unzip metadata provided and return content
  */

  _unzipContent : function(metaData){
      try{
          var decodeBase64 = window.atob(metaData);
          var unzippedResponse = pako.ungzip(decodeBase64, { to: 'string' });
          return unzippedResponse;
      }catch(err){
          return metaData;
      }
  },


  // ------------------------------------------------*************-------------------------------------------------
  // Initialize Live editing
  // ------------------------------------------------*************-------------------------------------------------

  /**
  * @desc Initialize Live editing on the excellentable
  */

  setUpLiveContext : function(){
    var self = this;

    var excellentableFirebaseSubPath = 'server/';
    var fbSubpath = excellentableFirebaseSubPath + self.excellentableId;
    if( self.fbContextPath ){
      fbSubpath = self.fbContextPath;
    }
    self.sessionRef = self.database.ref( fbSubpath );
    self.operationDiffRef = self.database.ref(fbSubpath + '/operationDiff');
    self.spreadDiffRef = self.database.ref(fbSubpath + '/spreadDiff');
    self.userKeysRef = self.database.ref(fbSubpath + '/userKeys/' );
    self.userRef = self.database.ref(fbSubpath + '/userKeys/' + self.sessionId );
    self.userLocationRef = self.database.ref(fbSubpath + '/userKeys/' + self.sessionId + '/location' );
    self.contentRef = self.database.ref(fbSubpath + '/content/');

    self.contentRef.once('value', function(snapshot) {
        if( snapshot.val() !== null ){
            self.excellentableJSON = self._unzipContent(snapshot.val().spreadJSON) || "";
        }

        self.state = true;

        // Intialize avatars for all external and current users
        self.avatarInitalizeStatus = $.Deferred();

        self._registerUser();
        self._listenForUserLeave();

        // Call Initialization based on restore
        if( self.restoreOptions.restoreFlag ){
          self._onRestoreReplaceAllUserContent(self.restoreOptions.restoreContent);
        }else{
          self._initExcellentableContent(snapshot.val());
        }

        self._initCellTracking();
        self._initCellContentChange();
        self._initAllUserPosition();
        self._listenForUserMove();
        self._listenForUserJoin();
        // self._checkFirebaseState();
    }).catch(function(err){
      // Error while trying to access content from firebase - Permission or something else

      self.state= false;
      self._liveEditingFailed();
      console.log(err);
      self._sendNotifcation(AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.collaborative.editor.liveediting.failed.title"),AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.collaborative.editor.liveediting.firebase.connection.failed.troubleshoot"),'error');
    });
  },

  /**
  * @desc get the status of live editing - enabled / disabled
  */

  getState : function(){
    var self = this;
    return self.state;
  }
}
