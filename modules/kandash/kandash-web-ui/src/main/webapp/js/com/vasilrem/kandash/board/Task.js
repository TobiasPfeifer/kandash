/**
 * Represents single task on the board
 */
com.vasilrem.kandash.board.Task = Ext.extend(Ext.Panel, {
    frame : true,
    collapsible : true,
    width:250,
    height: 150,
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
        handler: function(event, toolEl, panel){

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
        this.priority = priority
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
            if(this.lastTarget) {
                Ext.getCmp(this.lastTarget.id).add(Ext.getCmp(el.id))
                Ext.getCmp(this.lastTarget.id).doLayout()
            }
            el.applyStyles({
                position:'absolute'
            });
            el.setXY(dragEl.getXY());
            el.setWidth(dragEl.getWidth());
            this.lastTarget = null;
        }else{
            this.invalidDrop = false
        }
    }
}
)

Ext.reg('kandash.task', com.vasilrem.kandash.board.Task);