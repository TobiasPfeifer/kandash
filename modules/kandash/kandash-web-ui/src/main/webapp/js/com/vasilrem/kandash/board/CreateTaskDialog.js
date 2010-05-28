/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

var dialogTaskForm = new Ext.form.FormPanel({
    id: 'dialogTaskForm',
    baseCls: 'x-plain',
    labelWidth: 100,
    defaultType: 'textfield',
    monitorValid:true,
    items: [new Ext.form.ComboBox({
        fieldLabel: 'Project',
        hiddenName:'taskProject',
        valueField:'taskProjectId',
        displayField:'taskProject',
        typeAhead: true,
        mode: 'local',
        triggerAction: 'all',
        allowBlank: true,
        emptyText:'Choose the project...',
        selectOnFocus:true,
        anchor: '95%'
    }),{
        fieldLabel: 'Summary',
        name: 'description',
        allowBlank: false,
        anchor:'100%'
    },{
        fieldLabel: 'Assigned to',
        name: 'assignee',
        allowBlank: false,
        anchor: '100%'
    }, {
        fieldLabel: 'Estimated (m/d)',
        name: 'estimation',
        allowBlank: false,
        anchor: '100%'
    }, new Ext.form.ComboBox({
        fieldLabel: 'Priority',
        hiddenName:'priority',
        allowBlank: false,
        store: new Ext.data.ArrayStore({
            fields: ['priorityId', 'priority'],
            data: [[0, 'Low'], [1, 'Medium'], [2, 'High']]
        }),
        valueField:'priorityId',
        displayField:'priority',
        typeAhead: true,
        mode: 'local',
        triggerAction: 'all',
        emptyText:'Specify task priority...',
        selectOnFocus:true,
        anchor: '95%'
    })],

    getProject: function(){
        return this.items.items[0].getValue()
    },

    setProject: function(projectId){
        this.items.items[0].setValue(projectId)
    },

    getSubject: function(){
        return this.items.items[1].getValue()
    },

    setSubject: function(subject){
        this.items.items[1].setValue(subject)
    },

    getAssignee: function(){
        return this.items.items[2].getValue()
    },

    setAssignee: function(assignee){
        this.items.items[2].setValue(assignee)
    },

    getEstimation: function(){
        return this.items.items[3].getValue()
    },

    setEstimation: function(estimation){
        this.items.items[3].setValue(estimation)
    },

    getPriority: function(){
        return this.items.items[4].getValue()
    },

    setPriority: function(priority){
        this.items.items[4].setValue(priority)
    }

});

var updateTaskDialog = new Ext.Window({
    title: 'Task',
    width: 300,
    height:250,
    minWidth: 300,
    minHeight: 250,
    layout: 'fit',
    plain:true,
    bodyStyle:'padding:5px;',
    buttonAlign:'center',
    items: dialogTaskForm,
    buttons: [{
        text: 'Save',
        type: 'submit',
        handler: function(){
            var form = Ext.getCmp('dialogTaskForm').getForm()
            
            if(!form.isValid() || !parseInt(form.getEstimation())){
                return
            }            
            var description = form.getSubject()
            var assignedTo = form.getAssignee()
            var estimation = form.getEstimation()
            var priority = form.getPriority()
            var board = getBoard()            
            if(this.ownerCt.ownerCt.taskId){
                var task = Ext.getCmp(this.ownerCt.ownerCt.taskId)
                task.setFormTitle(description)
                task.setFormAssignedTo(assignedTo)
                task.setFormEstimation(estimation)
                task.setFormPriority(priority)
                PUT(RESOURCES + RS_TASK + '/' + board.id, task.toJSON())
            }else{
                
                var projectId = form.getProject()
                var tierId = board.tiers[board.tiers.length-1].id
                if(board.getTasksPerTier(tierId).length >= board.boardGrid[projectId][tierId].getWipLimit()){
                    Ext.Msg.alert('Task cannot be assigned to the tier!', 'WiP limit is reached!');
                }else{
                    board.addTask(null, projectId,
                        tierId,
                        description, assignedTo, parseInt(estimation),
                        priority, 20, 20, true)
                }
            }
            updateTaskDialog.hide()
        }
    },{
        text: 'Cancel',
        handler: function(){
            updateTaskDialog.hide()
        }
    }]
});

function showTaskDialog(isCreateDialog, taskId, description, assignedTo, estimation, priority){

    var projects = getBoard().getProjects()
    dialogTaskForm.setSubject(description)
    dialogTaskForm.setAssignee(assignedTo)
    dialogTaskForm.setEstimation(estimation)
    dialogTaskForm.setPriority(priority)
    if(isCreateDialog){
        dialogTaskForm.items.items[0].show()
        updateTaskDialog.taskId = null
        if(dialogTaskForm.items.items[0].store){
            dialogTaskForm.items.items[0].store.removeAll()
        }else{
            dialogTaskForm.items.items[0].store = new Ext.data.SimpleStore({
                fields: ['taskProjectId', 'taskProject']
            })
        }
        for(project in projects){
            if(projects[project].id)
                dialogTaskForm.items.items[0].store.add(
                    new Ext.data.Record({
                        'taskProjectId': project,
                        'taskProject': projects[project].title
                    }))
        }
    }else{
        dialogTaskForm.items.items[0].hide()
        updateTaskDialog.taskId = taskId        
    }
    updateTaskDialog.show()
}
