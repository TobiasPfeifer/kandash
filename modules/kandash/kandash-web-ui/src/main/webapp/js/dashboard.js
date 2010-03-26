Ext.onReady(function(){

    Ext.QuickTips.init();

    new Ext.Viewport({
        layout:'border',
        items:[{
            region: 'north',
            layout:'fit',
            tbar: [{
                text: 'Add Task',
                iconCls: 'add16',
                handler: function(){
                    showTaskDialog(true)
                }
            },{
                text: 'Add Project',
                iconCls: 'add16',
                handler: function(){
                    createProjectDialog.show()
                }
            },{
                text: 'Add Tier',
                iconCls: 'add16'
            }]
        },{
            xtype:'kandash.board',
            region: 'center'
        }]
    });
  
    var projectBoard = Ext.getCmp('projectboard')

    projectBoard.addTier('tier3', 'Done')
    projectBoard.addTier('tier2', 'In Progress')
    projectBoard.addTier('tier1', 'TO-DO')

    projectBoard.addProject('project1', 'Project 1')
    projectBoard.addProject('project2', 'Project 2')
    projectBoard.addProject('project3', 'Project 3')

    projectBoard.addTask('project1', 
        'tier1', 'task1',
        'Short task description (preferrable, not more than 140 symbol)',
        'John Smith',
        5, 1, 20, 20)

});


