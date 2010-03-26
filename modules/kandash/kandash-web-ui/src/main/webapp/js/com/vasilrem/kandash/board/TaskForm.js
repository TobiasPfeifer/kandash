/**
 * Represents data displayed on the task
 * @param taskName task name
 * @param assignedTo task assignee
 * @param estimation time estimated to complete the task
 * @param priority task priority
 */
com.vasilrem.kandash.board.TaskForm = function(taskName,
    assignedTo,estimation,priority){
    var panel = getTaskFormPanel()
    panel.items.items[0].html = taskName
    panel.items.items[1].html = assignedTo
    panel.items.items[2].html = '' + estimation
    panel.items.items[3].html = priorityCodes[priority]
    return panel
}

/**
 *To be refactored: priority level names
 **/
const priorityCodes = ['Low', 'Medium', 'High']

/**
 * Task from template
 */
getTaskFormPanel = function(){
    return new Ext.form.FormPanel({
        baseCls: 'x-plain',
        labelWidth: 95,
        defaultType: 'label',
        items: [{
            fieldLabel: 'Summary',
            name: 'title',
            anchor: '100%'
        },{
            fieldLabel: 'Assigned To',
            name: 'assignedTo',
            anchor:'100%'
        },{
            fieldLabel: 'Estimated (m/d)',
            name: 'estimation',
            anchor:'100%'
        },{
            fieldLabel: 'Priority',
            name: 'priority',
            anchor:'100%'
        }]
    })
}

getPriorityById = function(id){
    return priorityCodes[id]
}

Ext.reg('kandash.taskform', com.vasilrem.kandash.board.TaskForm);