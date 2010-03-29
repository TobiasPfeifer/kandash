Ext.onReady(function(){

    Ext.QuickTips.init();

    new Ext.Viewport({
        layout:'border',
        items:[{
            region: 'north',
            layout:'fit',
            id: 'boardbar',
            tbar: [{
                text: 'Add Task',
                iconCls: '',
                handler: function(){
                    showTaskDialog(true)
                }
            },{
                text: 'Add Project',
                iconCls: '',
                handler: function(){
                    createProjectDialog.show()
                }
            },{
                text: 'Add Tier',
                iconCls: '',
                handler: function(){
                    showAddTierDialog()
                }
            }]
        },{
            xtype:'kandash.board',
            region: 'center'
        },{
            id: 'projectbar',
            region: 'south',
            layout:'fit',
            height:30,
            tbar: []
        }]
    });
  
    var projectBoard = Ext.getCmp('projectboard')

    // default tiers!!! should always present
    projectBoard.addTier('tier3', 'Done', false)
    projectBoard.addTier('tier2', 'In Progress', false)
    projectBoard.addTier('tier1', 'TO-DO', false)

    projectBoard.addProject('project1', 'Project 1')
    projectBoard.addProject('project2', 'Project 2')
    projectBoard.addProject('project3', 'Project 3')

    projectBoard.addTask('project1', 
        'tier1', 'task1',
        'Short task description (preferrable, not more than 140 symbol)',
        'John Smith',
        5, 1, 20, 20)

});


