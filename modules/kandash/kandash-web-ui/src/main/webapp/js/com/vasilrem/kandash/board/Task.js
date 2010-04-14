/**
 * Represents single task on the board
 */
com.vasilrem.kandash.board.Task = Ext.extend(Ext.Panel, {
    frame : true,
    collapsible : true,
    width:300,
    autoScroll:true,
    layout: 'fit',
    tools: [{
        id: 'gear',
        qtip: 'Update',
        handler: function(event, toolEl, task){
            showTaskDialog(false, task.id, task.title, task.assignedTo,
                task.estimation, task.priority)
        }
    },{
        id: 'close',
        qtip: 'Delete',
        handler: function(event, toolEl, task){
            Ext.Msg.show({
                title:'Delete task?',
                msg: 'Are you sure, you want to delete this task?',
                buttons: Ext.Msg.YESNO,
                fn: function(btn){
                    if(btn == 'yes'){
                        var cell = task.ownerCt
                        cell.remove(task)
                        DELETE(RESOURCES + RS_TASK + '/' + task.id)
                    }
                },
                animEl: 'elId'
            });
        }
    }],

    /**
     * Initializes component
     **/
    initEvents : function(){
        com.vasilrem.kandash.board.Task.superclass.initEvents.call(this);
        this.dd = new com.vasilrem.kandash.board.Task.DropZone(this.id, 'dropdownGroup_' + this.getProject());
    },

    /**
     * Gets identifier of the project the task belong to
     **/
    getProject: function(){
        return this.ownerCt.ownerCt.id
    },

    getForm: function(){
        return this.items.items[0]
    },

    getFormTitle: function(){
        return this.title
    },

    setFormTitle: function(title){
        this.setTitle(title)
        this.getForm().items.items[0].update(title)
    },

    setFormAssignedTo: function(assignedTo){
        this.getForm().items.items[1].update(assignedTo)
        this.assignedTo = assignedTo
    },

    setFormEstimation: function(estimation){        
        this.getForm().items.items[2].update('' + estimation)
        this.estimation = estimation
    },

    setFormPriority: function(priority){
        this.getForm().items.items[3].update(getPriorityById(priority))
        this.el.dom.getElementsByClassName(
            'x-panel-tc-'+
            getPriorityById(this.priority).toLowerCase()
            )[0].className=
        'x-panel-tc-'+getPriorityById(priority).toLowerCase()
        this.priority = priority
    },

    /**
     * Converts task to lightweight JSON object
     */
    toJSON: function(){
        return {
            '_id': this.id,
            'assigneeId': this.assignedTo, // TO CHANGE: assignee name should be replaced with assignee ID
            'description': this.title,
            'estimation': this.estimation,
            'offsetLeft': this.x, // TO CHANGE: relative offset left
            'offsetTop': this.y, // TO CHANGE: relative offset top
            'priority': this.priority,
            'tierId': this.ownerCt.id.substr(
                this.ownerCt.id.indexOf('_') + 1, this.ownerCt.id.length),
            'workflowId': this.ownerCt.ownerCt.id
        }
    }

});

/**
 * Represents task's drop-down behavior
 */
com.vasilrem.kandash.board.Task.DropZone = Ext.extend(Ext.dd.DDProxy, {

    onDragOver: function(e, targetId) {
        var currentOwner = Ext.getCmp(this.getEl().id).ownerCt.id
        if(currentOwner != targetId &&
            targetId.indexOf("task")<0){
            var target = Ext.get(targetId);
            this.lastTarget = target;
        }
    },

    onInvalidDrop: function() {
        this.invalidDrop = true;
    },

    onDragOut: function(e, targetId) {
        this.lastTarget = null;
    },

    endDrag: function() {
        if(!this.invalidDrop){
            var dragEl = Ext.get(this.getDragEl());
            var el = Ext.get(this.getEl());
            var task = Ext.getCmp(el.id)
            if(this.lastTarget) {
                
                var tierId = Ext.getCmp(this.lastTarget.id).getTierId()
                if(getBoard().getTasksPerTier(tierId).length >=
                    getBoard().boardGrid[Ext.getCmp(this.lastTarget.id).ownerCt.id][tierId].getWipLimit()){
                    Ext.Msg.alert('Task cannot be assigned to the tier!', 'WiP limit is reached!');
                    return
                }
                Ext.getCmp(this.lastTarget.id).add(task)
                Ext.getCmp(this.lastTarget.id).doLayout()
            }
            el.applyStyles({
                position:'absolute'
            });
            task.x += dragEl.getXY()[0] - el.getXY()[0]
            task.y += dragEl.getXY()[1] - el.getXY()[1]
            if(task.x < 0){
                task.x = 0
            }
            if(task.y < 0){
                task.y = 0
            }
            el.setXY(dragEl.getXY());
            el.setWidth(dragEl.getWidth());
            PUT(RESOURCES + RS_TASK, task.toJSON())
            this.lastTarget = null;
        }else{
            this.invalidDrop = false
        }
    }
}
)

Ext.reg('kandash.task', com.vasilrem.kandash.board.Task);