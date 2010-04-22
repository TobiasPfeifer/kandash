/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

var filterForm = new Ext.form.FormPanel({
    id: 'filter',
    baseCls: 'x-plain',
    labelWidth: 100,
    defaultType: 'textfield',
    items: [{
        fieldLabel: 'Task',
        name: 'task.name',
        anchor: '100%'
    },new Ext.form.ComboBox({
        fieldLabel: 'Project',
        hiddenName:'taskProject',
        valueField:'taskProjectId',
        displayField:'taskProject',
        name: 'task.workflowId',
        typeAhead: true,
        mode: 'local',
        triggerAction: 'all',
        emptyText:'Choose the project...',
        selectOnFocus:true,
        anchor: '100%'
    }),new Ext.form.ComboBox({
        fieldLabel: 'State',
        hiddenName:'tier',
        valueField:'tierId',
        displayField:'tier',
        name: 'task.tierId',
        typeAhead: true,
        mode: 'local',
        triggerAction: 'all',
        emptyText:'Choose the state...',
        selectOnFocus:true,
        anchor: '100%'
    }),{
        fieldLabel: 'Assignee',
        name: 'task.assigneeId',
        name: 'assignee',
        anchor: '100%'
    }]
})

var filterDialog = new Ext.Window({
    title: 'Filter',
    width: 300,
    height:200,
    minWidth: 300,
    minHeight: 200,
    layout: 'fit',
    plain:true,
    bodyStyle:'padding:5px;',
    buttonAlign:'center',
    items: filterForm,

    buttons: [{
        text: 'Filter',
        type: 'submit',
        handler: function(){
            var filter
            debugger
            Ext.getCmp('filter').items.items.forEach(function(item){
                debugger
                if(item.getValue())
                    filter[item.name] = item.getValue()
            })
            /*filter['task.name'] = new RegExp(items[0].getValue())
            if(items[1].getValue())
            filter['task.workflowId'] = items[1].getValue()*/
            filterDialog.callbackFunction(filter)
            filterDialog.hide()
        }
    },{
        text: 'Cancel',
        handler: function(){            
            filterDialog.hide()
        }
    }]
});

showFilterDialog = function(board, callbackFunction){
    debugger
    filterDialog.callbackFunction = callbackFunction
    filterForm.items.items[1].store = new Ext.data.SimpleStore({
        fields: ['taskProjectId', 'taskProject']
    })
    filterForm.items.items[2].store = new Ext.data.SimpleStore({
        fields: ['tierId', 'tier']
    })
    board.workflows.forEach(function(project){
        filterForm.items.items[1].store.add(
            new Ext.data.Record({
                'taskProjectId': project._id,
                'taskProject': project.name
            }))
    })
    board.tiers.forEach(function(tier){
        filterForm.items.items[2].store.add(
            new Ext.data.Record({
                'tierId': tier._id,
                'tier': tier.name
            }))
    })
    filterDialog.show()
}

