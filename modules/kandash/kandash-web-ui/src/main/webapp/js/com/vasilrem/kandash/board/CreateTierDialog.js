/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

var addTierForm = new Ext.form.FormPanel({
    id: 'addTierForm',
    baseCls: 'x-plain',
    labelWidth: 100,
    defaultType: 'textfield',
    items: [{
        fieldLabel: 'Name',
        name: 'tiername',
        allowBlank: false,
        anchor: '100%'
    }, new Ext.form.ComboBox({
        fieldLabel: 'Starts after/swap with',
        hiddenName:'tierName',
        valueField:'tierPosition',
        allowBlank: false,
        displayField:'tierName',
        typeAhead: true,
        mode: 'local',
        triggerAction: 'all',
        emptyText:'Choose the tier...',
        selectOnFocus:true,
        anchor: '100%'
    }),{
        fieldLabel: 'Work-in-Progress',
        name: 'wiplimit',
        anchor: '100%'
    }],

    getTierName: function(){
        return this.items.items[0].getValue()
    },

    getTierPosition: function(){
        return this.items.items[1].getValue()
    },

    getWipLimit: function(){
        return this.items.items[2].getValue()
    }
})

var addTierDialog = new Ext.Window({
    title: 'Add tier',
    width: 300,
    height:175,
    minWidth: 300,
    minHeight: 175,
    layout: 'fit',
    plain:true,
    bodyStyle:'padding:5px;',
    buttonAlign:'center',
    items: addTierForm,

    buttons: [{
        text: 'Save',
        type: 'submit',
        handler: function(){
            var tierId
            if(!addTierForm.getForm().isValid()){
                return
            }
            if(addTierDialog.isUpdate){                
                var tierCell = addTierDialog.isUpdate
                tierId = tierCell.id.substr(
                    tierCell.id.indexOf('_') + 1, tierCell.id.length)
                var isTierUpdated = tierCell.ownerCt.ownerCt.updateTier(tierId, addTierForm.getTierName(), addTierForm.getWipLimit(), addTierForm.getTierPosition())
                PUT(RESOURCES + RS_TIER, tierCell.toJSON(tierId))
                if(isTierUpdated){
                    initBoard(boardFromRequest)
                }
            }else{
                tierId = POST(RESOURCES + RS_TIER + '/' + getBoard().id, {
                    'name': addTierForm.getTierName(),
                    'order': addTierForm.getTierPosition(),
                    'wipLimit': addTierForm.getWipLimit()
                })
                var board = getBoard()
                board.createTier(tierId ,addTierForm.getTierName(), addTierForm.getTierPosition(), addTierForm.getWipLimit())
            }
            addTierDialog.hide()
        }
    },{
        text: 'Cancel',
        handler: function(){
            addTierDialog.hide()
        }
    }]
});

showAddTierDialog = function(tier){
    var tiers = getBoard().tiers
    if(addTierForm.items.items[1].store){
        addTierForm.items.items[1].store.removeAll()
    }else{
        addTierForm.items.items[1].store = new Ext.data.SimpleStore({
            fields: ['tierPosition', 'tierName']
        })
    }
    if(tier){
        for(var i = 0; i< tiers.length; i++){
            //if(i != tier.getTierOrder())
                addTierForm.items.items[1].store.add(
                    new Ext.data.Record({
                        'tierPosition': i,
                        'tierName': tiers[i].name
                    }))
        }
        addTierForm.items.items[0].setValue(tier.getTierName())
        addTierForm.items.items[1].setValue(null)
        addTierForm.items.items[2].setValue(tier.wipLimit)
        addTierDialog.setTitle('Update Tier')
        addTierDialog.isUpdate = tier
    }else{
        for(var i = 1; i< tiers.length; i++){
            addTierForm.items.items[1].store.add(
                new Ext.data.Record({
                    'tierPosition': i,
                    'tierName': tiers[i].name
                }))
        }        
        addTierForm.items.items[0].setValue('')
        addTierForm.items.items[1].setValue(null)
        addTierForm.items.items[2].setValue('')
        addTierDialog.setTitle('Add Tier')
        addTierDialog.isUpdate = null
    }
    addTierDialog.show()
}

