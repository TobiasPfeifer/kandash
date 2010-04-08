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
        anchor: '100%'
    }, new Ext.form.ComboBox({
        fieldLabel: 'Add after',
        hiddenName:'tierName',
        valueField:'tierPosition',
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
    height:170,
    minWidth: 300,
    minHeight: 170,
    layout: 'fit',
    plain:true,
    bodyStyle:'padding:5px;',
    buttonAlign:'center',
    items: addTierForm,

    buttons: [{
        text: 'Save',
        type: 'submit',
        handler: function(){
            debugger
            var tierId = POST(RESOURCES + RS_TIER + '/' + getBoard().id, {
                'name': addTierForm.getTierName(),
                'order': addTierForm.getTierPosition(),
                'wipLimit': addTierForm.getWipLimit()
            })
            var board = getBoard()
            board.createTier(tierId ,addTierForm.getTierName(), addTierForm.getTierPosition(), addTierForm.getWipLimit())
            addTierDialog.hide()
        }
    },{
        text: 'Cancel',
        handler: function(){
            addTierDialog.hide()
        }
    }]
});

showAddTierDialog = function(){
    var tiers = getBoard().tiers
    if(addTierForm.items.items[1].store){
        addTierForm.items.items[1].store.removeAll()
    }else{
        addTierForm.items.items[1].store = new Ext.data.SimpleStore({
            fields: ['tierPosition', 'tierName']
        })
    }
    for(var i = 1; i< tiers.length; i++){
        addTierForm.items.items[1].store.add(
            new Ext.data.Record({
                'tierPosition': i,
                'tierName': tiers[i].name
            }))
    }
    addTierDialog.show()
}

