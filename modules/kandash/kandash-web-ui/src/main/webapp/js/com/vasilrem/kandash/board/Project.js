/**
 * Represents project pane (column) on the board
 */
com.vasilrem.kandash.board.Project = Ext.extend(Ext.Panel, {
    layout:'anchor',
    bodyStyle:'border:none',
    tools: [{
        id: 'gear',
        qtip: 'Update',
        /**
         * Updates project. <br/>
         * @param event
         * @param toolEl
         * @param panel
         */
        handler: function(event, toolEl, panel){

        }
    },{
        id: 'close',
        qtip: 'Delete',
        /**
         * Deletes project from the board.<br/>
         * @param event
         * @param toolEl
         * @param panel
         */
        handler: function(event, toolEl, project){
            var board = project.ownerCt
            board.remove(project)
            for(var i=0;i<board.items.length; i++){
                board.items.items[i].columnWidth = 1/board.items.length
            }
            board.doLayout()
        }
    }]        
});

Ext.reg('kandash.project', com.vasilrem.kandash.board.Project);
