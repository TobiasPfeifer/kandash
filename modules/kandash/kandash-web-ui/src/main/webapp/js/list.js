Ext.onReady(function(){

    Ext.QuickTips.init();

    new Ext.Viewport({
        id:'viewport',
        layout:'border',
        items:[new Ext.form.FormPanel({
            id: 'boardList',
            layout: 'absolute',
            region: 'center',
            baseCls: 'x-plain',
            labelWidth: 100,
            defaultType: 'textfield',
            margins: '20 20 20 20',
            standardSubmit: true,
            url: 'board.jsp',
            method: 'GET',
            items: [new Ext.form.ComboBox({
                x: 30,
                y: 30,
                fieldLabel: 'Board name',
                hiddenName:'board',
                valueField:'boardId',
                displayField:'board',
                store: new Ext.data.ArrayStore({
                    fields: ['boardId', 'board']
                }),
                typeAhead: true,
                mode: 'local',
                triggerAction: 'all',
                emptyText:'Choose an existing board...',
                selectOnFocus:true,
                anchor: '100%'
            })],
            buttons: [{
                text: 'Choose board',
                type: 'submit',
                x: 30,
                y: 80,
                handler: function(){                    
                    Ext.getCmp('boardList').getForm().submit()
                }
            }, {
                text: 'Create board',
                x: 30,
                y: 130,
                handler: function(){
                    var boardName = Ext.getCmp('boardList').items.items[0].value
                    var boardId = POST(RESOURCES + RS_BOARD + '/' + boardName)
                    Ext.getCmp('boardList').items.items[0].store.add(
                        new Ext.data.Record({
                            'boardId': boardId,
                            'board': boardName
                        }))
                    Ext.getCmp('boardList').items.items[0].setValue(boardId)
                    Ext.getCmp('boardList').getForm().submit()
                }
            }]
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
                Ext.getCmp('boardList').items.items[0].store.add(
                    new Ext.data.Record({
                        'boardId': board._id,
                        'board': board.name
                    }))
            }
        }
    });

});

