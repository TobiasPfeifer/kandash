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
        handler: function(event, toolEl, project){
            Ext.Msg.show({
                title: 'Project name',
                msg: 'Please enter the project name:',
                width: 300,
                buttons: Ext.MessageBox.OKCANCEL,
                prompt: true,
                fn: function(btn, text){
                    if (btn == 'ok'){
                        project.setTitle(text)
                    }
                },
                value: project.title
            });
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
            Ext.Msg.show({
                title:'Delete project?',
                msg: 'Are you sure, you want to delete this project? All tasks will be lost!',
                buttons: Ext.Msg.YESNO,
                fn: function(btn){
                    if(btn == 'yes'){
                        var board = project.ownerCt
                        board.remove(project)
                        for(var i=0;i<board.items.length; i++){
                            board.items.items[i].columnWidth = 1/board.items.length
                        }
                        board.doLayout()
                    }
                },
                animEl: 'elId'
            });
        }
    }]        
});

Ext.reg('kandash.project', com.vasilrem.kandash.board.Project);
