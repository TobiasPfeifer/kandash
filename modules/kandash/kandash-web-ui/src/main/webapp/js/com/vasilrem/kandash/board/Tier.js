/**
 * Represents tier (stage) of the task (To-Do, In progress, Done, etc.)
 */
com.vasilrem.kandash.board.Tier = Ext.extend(Ext.Panel, {
    collapsible : true,
    bodyStyle:'border:none',
    autoScroll:true,
    layout:'absolute',
    MAX_WIP_LIMIT:1000,
    tools: [{
        id: 'gear',
        qtip: 'Update',
        handler: function(event, toolEl, tierCell){            
            showAddTierDialog(tierCell)
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
     * Returns name to be disaplyed on UI
     * @param name optional parameter to override name in the displayed string
     * @param wipLimit optional parameter to override WiP-limit in the displayed string
     */
    getDisplayableName: function(name, wipLimit){
        var _wipLimit = wipLimit?wipLimit: this.getWipLimit()
        return (name?name:this.getTierName()) + (_wipLimit?' (' + _wipLimit + ')':'')
    },

    /**
     * Gets order of the tier on the board
     **/
    getTierOrder: function(){     
        for(var i=0; i<getBoard().tiers.length; i++){
            if(this.id.indexOf(getBoard().tiers[i].id) > -1)
                return i
        }
        return -1
    },

    /**
    * Gets tier identifier
    */
    getTierId: function(){
        return this.id.substr(
            this.id.indexOf('_') + 1, this.id.length)
    },

    /**
     * Gets name of the tier on the board
     **/
    getTierName: function(){
        for(var i=0; i<getBoard().tiers.length; i++){
            if(this.id.indexOf(getBoard().tiers[i].id) > -1)
                return getBoard().tiers[i].name
        }
        return ''
    },

    /**
     * Gets WiP-limit of the tier on the board
     **/
    getWipLimit: function(){
        for(var i=0; i<getBoard().tiers.length; i++){
            if(this.id.indexOf(getBoard().tiers[i].id) > -1)
                return parseInt(getBoard().tiers[i].wipLimit)
        }
        return this.MAX_WIP_LIMIT
    },

    /*
     * Gets list of tasks, located in the cell
     **/
    getTasks: function(){
        return this.items.items
    },

    /**
     * Converts tier to lightweight JSON object
     */
    toJSON: function(id){
        
        return {
            '_id': id? id : this.id,
            'name': this.getTierName(),
            'order': this.getTierOrder(),
            'wipLimit': this.wipLimit
        }
    }
});

Ext.reg('kandash.tier', com.vasilrem.kandash.board.Tier);

