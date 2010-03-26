/**
 * Represents tier (stage) of the task (To-Do, In progress, Done, etc.)
 */
com.vasilrem.kandash.board.Tier = Ext.extend(Ext.Panel, {
    collapsible : true,
    bodyStyle:'border:none',
    autoScroll:true,
    layout:'absolute',
    tools: [{
        id: 'gear',
        qtip: 'Update',
        handler: function(event, toolEl, panel){
        }
    },{
        id: 'close',
        qtip: 'Delete',
        handler: function(event, toolEl, panel){

        }
    }]
});

Ext.reg('kandash.tier', com.vasilrem.kandash.board.Tier);

