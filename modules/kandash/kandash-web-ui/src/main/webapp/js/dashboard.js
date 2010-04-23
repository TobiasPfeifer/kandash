Ext.onReady(function(){

    Ext.QuickTips.init();

    new Ext.Viewport({
        id:'viewport',
        layout:'border',
        items:[{
            region: 'north',
            layout:'fit',
            id: 'boardbar',
            tbar: [{
                text: 'Choose Board',
                iconCls: 'taskbar-board-icon',
                handler: function(){
                    window.location = './'
                }
            },{
                text: 'Open Report',
                iconCls: 'taskbar-report-icon',
                handler: function(){
                    window.location = 'chart.jsp?scale=week&board=' + boardFromRequest
                }
            },{
                text: 'Add Task',
                iconCls: 'taskbar-task-icon',
                handler: function(){
                    showTaskDialog(true)
                }
            },{
                text: 'Add Project',
                iconCls: 'taskbar-project-icon',
                handler: function(){
                    createProjectDialog.show()
                }
            },{
                text: 'Add Tier',
                iconCls: 'taskbar-tier-icon',
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

    initBoard(boardFromRequest)

});


