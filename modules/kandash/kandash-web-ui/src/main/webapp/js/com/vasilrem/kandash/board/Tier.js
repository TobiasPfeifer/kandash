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
                msg: 'Please enter the tier name (notice, the tier will be updated for all projects):',
                width: 300,
                buttons: Ext.MessageBox.OKCANCEL,
                prompt: true,
                fn: function(btn, text){
                    if (btn == 'ok'){
                        debugger
                        var tierId = tierCell.id.substr(
                            tierCell.id.indexOf('_') + 1, tierCell.id.length)
                        tierCell.ownerCt.ownerCt.updateTier(tierId, text)
                        PUT(RESOURCES + RS_TIER, tierCell.toJSON(tierId))
                    }
                },
                value: tierCell.title
            });
        }
    },{
        id: 'close',
        qtip: 'Delete',
        handler: function(event, toolEl, tierCell){
            debugger
            if(!tierCell.isDeletable){
                Ext.Msg.alert('This tier can not be deleted!', 'Default tier can not be deleted!');
            }else{
                Ext.Msg.show({
                    title:'Delete tier?',
                    msg: 'Are you sure, you want to delete this tier? All tasks will be lost!',
                    buttons: Ext.Msg.YESNO,
                    fn: function(btn){
                        if(btn == 'yes'){
                            debugger
                            var tierId = tierCell.id.substr(
                                tierCell.id.indexOf('_') + 1, tierCell.id.length)
                            tierCell.ownerCt.ownerCt.removeTier(tierId)
                            DELETE(RESOURCES + RS_TIER + '/' + tierId)
                        }
                    },
                    animEl: 'elId'
                });
            }
        }
    }],

    /**
     * Converts tier to lightweight JSON object
     */
    toJSON: function(id){
        return {
            '_id': id? id : this.id,
            'name': this.title,
            'order': this.order,
            'wipLimit': this.wipLimit
        }
    }
});

Ext.reg('kandash.tier', com.vasilrem.kandash.board.Tier);

