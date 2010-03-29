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
        handler: function(event, toolEl, tierCell){
            Ext.Msg.show({
                title: 'Tier name',
                msg: 'Please enter the tier name (please notice, the tier will be updated for all projects):',
                width: 300,
                buttons: Ext.MessageBox.OKCANCEL,
                prompt: true,
                fn: function(btn, text){
                    if (btn == 'ok'){
                        var tierId = tierCell.id.substr(
                            tierCell.id.indexOf('_') + 1, tierCell.id.length)
                        tierCell.ownerCt.ownerCt.updateTier(tierId, text)
                    }
                },
                value: tierCell.title
            });
        }
    },{
        id: 'close',
        qtip: 'Delete',
        handler: function(event, toolEl, tierCell){
            if(!tierCell.isDeletable){
                Ext.Msg.alert('This tier can not be deleted!', 'Default tier can not be deleted!');
            }else{
                Ext.Msg.show({
                    title:'Delete tier?',
                    msg: 'Are you sure, you want to delete this tier? All tasks will be lost!',
                    buttons: Ext.Msg.YESNO,
                    fn: function(btn){
                        if(btn == 'yes'){
                            var tierId = tierCell.id.substr(
                                tierCell.id.indexOf('_') + 1, tierCell.id.length)
                            Ext.getCmp('projectboard').removeTier(tierId)
                        }
                    },
                    animEl: 'elId'
                });
            }
        }
    }]
});

Ext.reg('kandash.tier', com.vasilrem.kandash.board.Tier);

