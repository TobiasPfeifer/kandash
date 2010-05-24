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
                text: 'Add',
                iconCls: 'taskbar-add-icon',
                menu: {
                    xtype: 'menu',
                    plain: true,
                    items: {
                        xtype: 'buttongroup',
                        autoWidth: true,
                        columns: 1,
                        defaults: {
                            scale: 'large',
                            width: '100%',
                            iconAlign: 'left'
                        },
                        items: [{
                            text: 'Task',
                            iconCls: 'taskbar-task-icon',
                            handler: function(){
                                showTaskDialog(true)
                            }
                        },{
                            text: 'Project',
                            iconCls: 'taskbar-project-icon',
                            handler: function(){
                                createProjectDialog.show()
                            }
                        },{
                            text: 'Tier',
                            iconCls: 'taskbar-tier-icon',
                            handler: function(){
                                showAddTierDialog()
                            }
                        }]
                    }
                }
            },{
                text: 'Board',
                iconCls: 'taskbar-board-icon',
                menu: {
                    xtype: 'menu',
                    plain: true,
                    items: {
                        xtype: 'buttongroup',
                        autoWidth: true,
                        columns: 1,
                        defaults: {
                            scale: 'large',
                            width: '100%',
                            iconAlign: 'left'
                        },
                        items: [{
                            text: 'Choose',
                            iconCls: 'taskbar-useboard-icon',
                            handler: function(){
                                window.location = './'
                            }
                        },{
                            text: 'Delete',
                            iconCls: 'taskbar-deleteboard-icon',
                            handler: function(){
                                Ext.Msg.show({
                                    title:'Delete board?',
                                    msg: 'Are you sure, you want to delete the board?',
                                    buttons: Ext.Msg.YESNO,
                                    fn: function(btn){
                                        if(btn == 'yes'){
                                            DELETE(RESOURCES + RS_BOARD + '/' + boardFromRequest)
                                            window.location = './'
                                        }
                                    },
                                    animEl: 'elId'
                                });
                            }
                        }]
                    }
                }
            },{
                text: 'Open Report',
                iconCls: 'taskbar-report-icon',
                handler: function(){
                    window.location = 'chart.jsp?scale=week&board=' + boardFromRequest
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
            items: new Ext.ProgressBar({
                id:'taskLoadProgress',
                width:300
            }),
            tbar: []
        }]
    });

    initBoard(boardFromRequest)
    startMonitoring(boardFromRequest)

});


