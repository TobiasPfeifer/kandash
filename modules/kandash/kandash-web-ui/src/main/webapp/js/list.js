Ext.onReady(function(){

    Ext.QuickTips.init();

    new Ext.Viewport({
        id:'viewport',
        layout:'border',
        items:[{
            region: 'north',
            baseCls: 'x-plain',
            height: 150,
            minSize: 75,
            maxSize: 250,
            cmargins: '5 0 0 0'
        },{
            baseCls: 'x-plain',
            region: 'center'
        },{
            region: 'south',
            baseCls: 'x-plain',
            height: 200,
            minSize: 75,
            maxSize: 250,
            layout: 'anchor',
            margins: '40 40 40 40',
            items: [{
                 baseCls: 'x-plain',
                 bodyStyle: 'font:normal 20px tahoma, arial, helvetica, sans-serif',
                 html: 'Powered with:'
            },new Ext.form.FormPanel({
                baseCls: 'x-plain',
                layout:'hbox',
                margins: '20 20 20 20',
                defaultType: 'label',
                items:[
                {
                    xtype: 'button',
                    width: 100,
                    height: 33,
                    margins: '10 10 10 10',
                    iconCls:'product-mongo-icon'
                },{
                    xtype: 'button',
                    width: 100,
                    height: 29,
                    margins: '10 10 10 10',
                    iconCls:'product-scala-icon'
                },{
                    xtype: 'button',
                    width: 100,
                    height: 64,
                    margins: '10 10 10 10',
                    iconCls:'product-ext-icon'
                }
                ]
            })]
        },new Ext.form.FormPanel({
            id: 'boardList',
            region: 'west',
            baseCls: 'x-plain',
            labelWidth: 100,
            width: 620,
            layout:'vbox',
            defaultType: 'textfield',
            margins: '20 20 20 20',
            standardSubmit: true,
            url: 'board.jsp',
            method: 'GET',
            items: [{
                html:'<i>Kanban is based on a very simple idea. Work-in-progress should be\n\
limited and something new should be started only when an existing piece\n\
of work is delivered or pulled by a downstream function. The kanban (or\n\
signal card) implies that a visual signal is produced to indicated that new\n\
work can be pulled because current work does not equal the agreed limit.\n\
This doesn’t sound very revolutionary nor does it sound like it would\n\
profoundly affect the performance, culture, capability and maturity of a\n\
team and its surrounding organization. What’s amazing is that it does!\n\
Kanban seems like such a small change and yet it changes everything\n\
about a business.</i>\n\
<p align="right"><b>David Anderson</b></p><br/>\n\
Kandash is a free open-source tool for Kanban.\n\
The project is currently hosted at <a href=http://code.google.com/p/kandash/>Google Code</a>.',
                margins: '20 20 20 20',
                width: 600,
                xtype:'label'
            },new Ext.form.ComboBox({
                fieldLabel: 'Board name',
                hiddenName:'board',
                valueField:'boardId',
                width: 600,
                id: 'boardCombo',
                margins: '20 20 20 20',
                displayField:'board',
                store: new Ext.data.ArrayStore({
                    fields: ['boardId', 'board']
                }),
                typeAhead: true,
                mode: 'local',
                triggerAction: 'all',
                emptyText:'Choose an existing board or start with a new one...',
                selectOnFocus:true
            })],
            buttons:[
            {
                text: 'Choose board',
                xtype: 'button',
                type: 'submit',
                margins: '5 5 5 20',
                iconCls: 'taskbar-useboard-icon',
                handler: function(){
                    Ext.getCmp('boardList').getForm().submit()
                }
            }, {
                text: 'Create board',
                xtype: 'button',
                margins: '5 5 5 20',
                iconCls: 'taskbar-addboard-icon',
                handler: function(){
                    var boardName = Ext.getCmp('boardCombo').value
                    var boardId = POST(RESOURCES + RS_BOARD + '/' + boardName)
                    Ext.getCmp('boardCombo').store.add(
                        new Ext.data.Record({
                            'boardId': boardId,
                            'board': boardName
                        }))
                    Ext.getCmp('boardCombo').setValue(boardId)
                    Ext.getCmp('boardList').getForm().submit()
                }
            }
            ]
        })
        ]
    });

    Ext.Ajax.request({
        headers : {
            'X-HTTP-Method-Override' : 'GET'
        },
        method: 'GET',
        url: 'resources/boards',
        success: function(response) {

            var boards = Ext.decode(response.responseText)
            for(var i=0; i<boards.length; i++){
                var board = boards[i]
                Ext.getCmp('boardCombo').store.add(
                    new Ext.data.Record({
                        'boardId': board._id,
                        'board': board.name
                    }))
            }
        }
    });

});

