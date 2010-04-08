Ext.onReady(function(){

    Ext.QuickTips.init();

    /* (UNCOMMENT) EXAMPLE OF API USAGE TO ADD PROJECTS, TIERS AND TASKS
    var boardId = POST(RESOURCES + RS_BOARD + '/projectboard')
    */

    new Ext.Viewport({
        id:'viewport',
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
            xtype: 'kandash.board',
            region: 'center'
        },{
            id: 'projectbar',
            region: 'south',
            layout:'fit',
            height:30,
            tbar: []
        }]
    });

    //USE ANY EXISTING BOARD
    initBoard('4bbdc76c83071da6edf96825')

/* (UNCOMMENT) EXAMPLE OF API USAGE TO ADD PROJECTS, TIERS AND TASKS
    var projectBoard = getBoard()        
    projectBoard.addTier(null, 'Done', false, 0)
    projectBoard.addTier(null, 'In Progress', false, 1)
    var backlogTierId = projectBoard.addTier(null, 'TO-DO', false, 2)
    
    var project1id = projectBoard.addProject(null, 'Project 1')
    projectBoard.addProject(null, 'Project 2')
    projectBoard.addProject(null, 'Project 3')

    projectBoard.addTask(null, project1id,
        backlogTierId,
        'Short task description (preferrable, not more than 140 symbol)',
        'John Smith',
        5, 1, 20, 20)
        */

});


