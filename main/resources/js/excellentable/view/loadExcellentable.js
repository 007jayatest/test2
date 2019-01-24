jQuery(document).ready(function(){
    jQuery('.eui-exc-container[excellentable-id]').each(function(){
        var tableId= jQuery(this).attr('excellentable-id');
        jQuery(this).Excellentable({ excellentableId : tableId });
    });
});