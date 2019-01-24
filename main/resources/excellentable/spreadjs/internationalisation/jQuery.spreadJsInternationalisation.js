/*
 * This file used for the spreadjs internationalisation purpose.
 * Transalate the spreadjs messages into different languages whose resources file not provided by spreadJS.
 */
AJS.toInit(function () {
    //Load the I18n of the plugin.
    AJS.I18n.get("Addteq.Excellentable", function () {
        jQuery(this).SpreadJsInternationalisation();
    });
});
(function($){
    $.fn.SpreadJsInternationalisation = function(){
        var $strResource,$self = this;
        
        this.init = function(){
            $self = this;
            $strResource = GcSpread.Sheets._ENStringResource;
            $self.setStrResources();
            $self.formulaDescription();
            $self.tableFunctionDescription();
        };
        //This function set the spreadJs Exception,lable,dialog messages
        this.setStrResources = function(){
            
            $strResource.Exp_InvalidArgument = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.invalidArgument");
            $strResource.Exp_InvalidCast = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.invalidCast");
            $strResource.Exp_NotSupport = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.notSupport");
            $strResource.Exp_FormulaInvalid = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.formulaInvalid");
            $strResource.Exp_InvalidTokenAt = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.invalidTokenAt");
            $strResource.Exp_InvalidArrayAt = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.invalidArrayAt");
            $strResource.Exp_InvalidCellReference = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.invalidCellReference");
            $strResource.Exp_InvalidFunctionName = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.invalidFunctionName");
            $strResource.Exp_InvalidOverrideFunction = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.invalidOverrideFunction");
            $strResource.Exp_OverrideNotAllowed = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.overrideNotAllowed");
            $strResource.Exp_NoSyntax = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.noSyntax");
            $strResource.Exp_IsValid = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.isValid");
            $strResource.Exp_InvalidArray = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.invalidArray");
            $strResource.Exp_InvalidParameters = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.invalidParameters");
            $strResource.Exp_InvalidArrayColumns = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.invalidArrayColumns");
            $strResource.Exp_ExprIsNull = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.exprIsNull");
            $strResource.Exp_RuleIsNull = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.ruleIsNull");
            $strResource.CopyCells = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.copyCells");
            $strResource.FillSeries = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.fillSeries");
            $strResource.FillFormattingOnly = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.fillFormattingOnly");
            $strResource.FillWithoutFormatting = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.fillWithoutFormatting");
            $strResource.Exp_NumberOnly = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.numberOnly");
            $strResource.Exp_RangeContainsMergedCell = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.rangeContainsMergedCell");
            $strResource.Exp_ChangeMergedCell = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.changeMergedCell");
            $strResource.Exp_TargetContainsMergedCells = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.targetContainsMergedCells");
            $strResource.Exp_MergedCellsIdentical = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.mergedCellsIdentical");
            $strResource.SortAscending = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.sortAscending");
            $strResource.SortDescending = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.sortDescending");
            $strResource.OK = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.oK");
            $strResource.Cancel = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.cancel");
            $strResource.Search = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.search");
            $strResource.CheckAll = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.checkAll");
            $strResource.UncheckAll = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.uncheckAll");
            $strResource.Blanks = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.blanks");
            $strResource.Exp_FilterItemIsNull = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.filterItemIsNull");
            $strResource.Exp_InvalidColumnIndex = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.invalidColumnIndex");
            $strResource.Exp_TokenIsNull = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.tokenIsNull");
            $strResource.Exp_InvalidBackslash = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.invalidBackslash");
            $strResource.Exp_FormatIllegal = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.formatIllegal");
            $strResource.Exp_ValueIsNull = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.valueIsNull");
            $strResource.Exp_PartIsNull = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.partIsNull");
            $strResource.Exp_DuplicatedDescriptor = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.duplicatedDescriptor");
            $strResource.Exp_TokenIllegal = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.tokenIllegal");
            $strResource.Exp_ValueIllegal = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.valueIllegal");
            $strResource.Exp_StringIllegal = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.stringIllegal");
            $strResource.Exp_InvalidNull = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.invalidNull");
            $strResource.Exp_InvalidOperation = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.invalidOperation");
            $strResource.Exp_ArgumentNull = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.argumentNull");
            $strResource.Exp_CriteriaIsNull = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.criteriaIsNull");
            $strResource.Exp_InvalidString = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.invalidString");
            $strResource.Exp_InvalidDateFormat = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.invalidDateFormat");
            $strResource.Exp_InvalidExponentFormat = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.invalidExponentFormat");
            $strResource.Exp_InvalidSemicolons = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.invalidSemicolons");
            $strResource.Exp_InvalidNumberGroupSize = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.InvalidNumberGroupSize");
            $strResource.Exp_BadFormatSpecifier = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.badFormatSpecifier");
            $strResource.Exp_InvalidNumberFormat = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.invalidNumberFormat");
            $strResource.Exp_InvalidIndex = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.invalidIndex");
            $strResource.Exp_InvalidCount = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.invalidCount");
            $strResource.Exp_InvalidLevel = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.invalidLevel");
            $strResource.Exp_GroupInfoIsNull = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.groupInfoIsNull");
            $strResource.Exp_SheetIsNull = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.sheetIsNull");
            $strResource.Exp_DestSheetIsNull = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.destSheetIsNull");
            $strResource.Exp_PasteExtentIsNull = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.pasteExtentIsNull");
            $strResource.Exp_InvalidPastedArea = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.invalidPastedArea");
            $strResource.Exp_ChangePartOfArray = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.changePartOfArray");
            $strResource.Exp_ColumnReadOnly = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.columnReadOnly");
            $strResource.Exp_RowReadOnly = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.rowReadOnly");
            $strResource.Exp_CellReadOnly = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.cellReadOnly");
            $strResource.Exp_FillRangeContainsMergedCell = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.fillRangeContainsMergedCell");
            $strResource.Exp_FillCellsReadOnly = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.fillCellsReadOnly"); 
            $strResource.Exp_OverlappingSpans = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.overlappingSpans"); 
            $strResource.Exp_InvalidAndSpace = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.invalidAndSpace"); 
            $strResource.Exp_SrcIsNull = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.srcIsNull"); 
            $strResource.Exp_DestIsNull = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.destIsNull"); 
            $strResource.Exp_InvalidCustomFunction = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.invalidCustomFunction"); 
            $strResource.Exp_InvalidCustomName = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.invalidCustomName"); 
            $strResource.Exp_IndexOutOfRange = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.indexOutOfRange"); 
            $strResource.Exp_InvalidRange = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.invalidRange"); 
            $strResource.Exp_RangeIsNull = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.rangeIsNull"); 
            $strResource.Exp_NotAFunction = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.notAFunction"); 
            $strResource.Exp_Format = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.format"); 
            $strResource.Exp_BraceMismatch = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.braceMismatch"); 
            $strResource.Exp_InvalidFormat = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.invalidFormat"); 
            $strResource.Exp_ArgumentOutOfRange = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.argumentOutOfRange"); 
            $strResource.Exp_DragDropShiftTableCell = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.dragDropShiftTableCell");
            $strResource.Exp_DragDropChangePartOfTable = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.dragDropChangePartOfTable"); 
            $strResource.Exp_TableEmptyNameError = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.tableEmptyNameError"); 
            $strResource.Exp_TableInvalidRow = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.tableInvalidRow"); 
            $strResource.Exp_TableInvalidColumn = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.tableInvalidColumn"); 
            $strResource.Exp_TableIntersectError = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.tableIntersectError"); 
            $strResource.Exp_TableHasSameNameError = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.tableHasSameNameError"); 
            $strResource.Exp_TableDataSourceNullError = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.tableDataSourceNullError"); 
            $strResource.Exp_TableStyleAddCustomStyleError = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.tableStyleAddCustomStyleError"); 
            $strResource.Exp_TableMoveOutOfRange = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.tableMoveOutOfRange"); 
            $strResource.Exp_TableResizeOutOfRange = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.tableResizeOutOfRange"); 
            $strResource.Exp_PasteSourceCellsLocked = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.pasteSourceCellsLocked"); 
            $strResource.Exp_InvalidCopyPasteSize = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.invalidCopyPasteSize "); 
            $strResource.Exp_PasteDestinationCellsLocked = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.pasteDestinationCellsLocked"); 
            $strResource.Exp_PasteChangeMergeCell = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.pasteChangeMergeCell"); 
            $strResource.Tip_Row = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.tip.row"); 
            $strResource.Tip_Column = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.tip.column"); 
            $strResource.Tip_Height = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.tip.height"); 
            $strResource.Tip_Width = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.tip.width"); 
            $strResource.NewTab = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.newTab"); 
            $strResource.Exp_EmptyNamedStyle = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.emptyNamedStyle"); 
            $strResource.Exp_FloatingObjectHasSameNameError =AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.floatingObjectHasSameNameError");  
            $strResource.Exp_FloatingObjectNameEmptyError = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.floatingObjectNameEmptyError"); 
            $strResource.ToolStrip_PasteText = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.toolStrip.pasteText"); 
            $strResource.ToolStrip_CutText = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.toolStrip.cutText"); 
            $strResource.ToolStrip_CopyText = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.toolStrip.copyText"); 
            $strResource.ToolStrip_AutoFillText = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.toolStrip.autoFillText"); 
            $strResource.Exp_ArrayFromulaPart = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.arrayFromulaPart"); 
            $strResource.Exp_ArrayFromulaSpan = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.arrayFromulaSpan"); 
            $strResource.Exp_ArrayFormulaTable = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp_arrayFormulaTable"); 
            $strResource.Fbx_Summary = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.fbx.summary"); 
            $strResource.Fbx_TableName_Description = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.fbx.tableName.description"); 
            $strResource.Fbx_CustomName_Description = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.fbx.customName.description"); 
            $strResource.Exp_TableResizeInvalidRange = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.tableResizeInvalidRange"); 
            $strResource.Blank = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.blank"); 
            $strResource.Exp_Separator = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.separator"); 
            $strResource.Exp_SheetNameInvalid = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.sheetNameInvalid"); 
            $strResource.Exp_SlicerNameInvalid = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.slicerNameInvalid"); 
            $strResource.Exp_SlicerNameExist = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.spreadjs.exp.slicerNameExist"); 
         
        };
        // set the spreadjs formula description messages.
        this.formulaDescription = function(){
            var formulas = GcSpread.Sheets.FormulaTextBoxResource_EN.Functions;
            for(var i = 0;i<formulas.length;i++){
                var name = formulas[i].name.toLowerCase().trim();
                formulas[i].description  = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.formula."+name+".description");
            }
        };
        //set the spreadjs table function description messages.
        this.tableFunctionDescription = function(){
            var functionDescriptions = GcSpread.Sheets.FormulaTextBoxResource_EN.Table_Functions;
            for(var i =0;i<functionDescriptions.length;i++ ){
                var functionName = functionDescriptions[i].name.toLowerCase();
                functionDescriptions[i].description = AJS.I18n.getText("com.addteq.confluence.plugin.excellentable.tableFunction."+functionName+".description");
            }
        };
        this.init();
        return this;
    };
})(jQuery);


