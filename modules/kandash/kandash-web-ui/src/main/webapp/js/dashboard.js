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

    initBoard(boardFromRequest)

});


