Ext.apply(Ext.form.VTypes, {
    daterange : function(val, field) {
        var date = field.parseDate(val);

        if(!date){
            return;
        }
        if (field.startDateField && (!this.dateRangeMax || (date.getTime() != this.dateRangeMax.getTime()))) {
            var start = Ext.getCmp(field.startDateField);
            start.setMaxValue(date);
            start.validate();
            this.dateRangeMax = date;
        }
        else if (field.endDateField && (!this.dateRangeMin || (date.getTime() != this.dateRangeMin.getTime()))) {
            var end = Ext.getCmp(field.endDateField);
            end.setMinValue(date);
            end.validate();
            this.dateRangeMin = date;
        }
        /*
         * Always return true since we're only using this vtype to set the
         * min/max allowed values (these are tested for after the vtype test)
         */
        return true;
    }
});

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
                text: 'Go To Board',
                iconCls: 'taskbar-board-icon',
                handler: function(){
                    window.location = 'board.jsp?board=' + boardFromRequest
                }
            },{
                text: 'Choose Another',
                iconCls: 'taskbar-useboard-icon',
                handler: function(){
                    window.location = './'
                }
            }]
        },{
            region: 'center',
            layout:'hbox',
            layoutConfig: {
                align : 'stretch',
                pack  : 'start'
            },
            items:[{
                title: 'Cumulative Flowchart',
                bodyStyle:'border:none',
                style: {
                    'padding': '20px 20px 20px 20px'
                },
                items:[new Ext.chart.LineChart({
                    width:600,
                    height:500,
                    anchor:'absolute',
                    id:'flowChart',
                    store: [],
                    style: 'margin-bottom: 10px; margin-top: 10px;',
                    xField: 'date',
                    tipRenderer: function(chart, record, index, series)
                    {
                        var wip = record.data[series.yField]
                        var fields = chart.store.fields.keys
                        var seriesIndex = fields.indexOf(series.yField)
                        if(seriesIndex > 1){
                            if(fields[seriesIndex - 1] != 'leadTime')
                                wip -= record.data[fields[seriesIndex - 1]]
                        }
                        if(series.yField == 'leadTime')
                            return  'Lead time: ' + wip + '\r\n'
                            + record.data.date
                        else
                            return  series.displayName + '\r\n'
                            + record.data.date + '\r\n'
                            + 'Work-in-progress: ' + wip
                    },
                    extraStyle: {
                        padding: 10,
                        animationEnabled: true,
                        legend:{
                            display:'bottom'
                        },
                        xAxis: {
                            color: 0x3366cc,
                            majorGridLines: {
                                size: 1,
                                color: 0xdddddd
                            }
                        },
                        yAxis: {
                            color: 0x3366cc,
                            majorTicks: {
                                color: 0x3366cc,
                                length: 4
                            },
                            minorTicks: {
                                color: 0x3366cc,
                                length: 2
                            },
                            majorGridLines: {
                                size: 1,
                                color: 0xdddddd
                            }
                        }
                    },
                    series: []
                }),new Ext.form.ComboBox({
                    id: 'scaleCombo',
                    width:600,
                    hiddenName:'scale',
                    valueField:'scaleId',
                    emptyText:'Select a scale...',
                    typeAhead: true,
                    mode: 'local',
                    forceSelection: true,
                    triggerAction: 'all',
                    displayField:'scale',
                    store: new Ext.data.ArrayStore({
                        fields: ['scaleId', 'scale'],
                        data: [['day', 'Day'],
                        ['week', 'Week'],
                        ['month', 'Month']]
                    })
                }),{
                    style: {
                        'padding-top': '5px'
                    }
                }, new Ext.form.ComboBox({
                    id: 'projectCombo',
                    width:600,
                    hiddenName:'project',
                    valueField:'projectId',
                    emptyText:'Select a project...',
                    typeAhead: true,
                    mode: 'local',
                    forceSelection: true,
                    triggerAction: 'all',
                    displayField:'project'
                })]
            }, {
                title: 'Activities',
                bodyStyle:'border:none',
                style: {
                    'padding': '20px 20px 20px 20px'
                },
                flex: 1,
                items: [
                new Ext.FormPanel({
                    labelWidth: 125,
                    bodyStyle:'padding:5px 5px 0; border:none',
                    defaultType: 'datefield',
                    buttons: [{
                        text: 'Filter',
                        type: 'filter',
                        iconCls: 'taskbar-filter-icon',
                        handler: function(){
                            showFilterDialog(board, function(query){
                                if(query){
                                    initReportForRange(board, query)
                                }
                            })
                        }
                    }],
                    items: [{
                        fieldLabel: 'Start Date',
                        name: 'startdt',
                        id: 'startdt',
                        vtype: 'daterange',
                        endDateField: 'enddt'
                    },{
                        fieldLabel: 'End Date',
                        name: 'enddt',
                        id: 'enddt',
                        vtype: 'daterange',
                        startDateField: 'startdt'
                    },new Ext.grid.GridPanel({
                        columns: [
                        {
                            header: 'Task ID',
                            width: 0,
                            hidden: true,
                            id:'taskId'
                        }, {
                            header: 'Task',
                            width: 160,
                            id:'task'
                        },
                        {
                            header: 'Project',
                            width: 75,
                            hidden: true
                        },
                        {
                            header: 'State',
                            width: 75
                        },
                        {
                            header: 'Assignee',
                            width: 75
                        },
                        {
                            header: 'Estimated Time',
                            width: 75
                        },
                        {
                            header: 'Time Active',
                            width: 75
                        },
                        {
                            header: 'Date Created',
                            width: 75,
                            hidden: true
                        },
                        {
                            header: 'Date Updated',
                            width: 80
                        }
                        ],
                        stripeRows: true,
                        autoExpandColumn: 'task',
                        stateful: true,
                        stateId: 'grid',
                        id: 'historygrid',
                        height:500,
                        store: new Ext.data.ArrayStore({
                            fields: [
                            {
                                name: 'taskId'
                            },
                            {
                                name: 'taskName'
                            },

                            {
                                name: 'projectName'
                            },

                            {
                                name: 'state'
                            },

                            {
                                name: 'assignee'
                            },

                            {
                                name: 'estimation'
                            },

                            {
                                name: 'timeActive'
                            },

                            {
                                name: 'dateCreated'
                            },

                            {
                                name: 'dateUpdated'
                            }
                            ]
                        })
                    })]
                })
                ]
            }]
        }]
    })



    /**
     * Draws workflow chart for the specified board, project and scale
     * @param boardId board identifier
     * @param scale month, week or day
     * @param projectId project identifier. if not specified, chart is built for all projects
     */
    function drawWorkflowChart(boardId, scale, projectId){
        var url = 'resources/chartmodel/workflow/'
        + boardId + '/'
        + scale
        if(projectId){
            url += '/' + projectId
        }
        Ext.Ajax.request({
            headers : {
                'X-HTTP-Method-Override' : 'GET'
            },
            method: 'GET',
            url: url,
            success: function(response) {
                var chartModel = Ext.decode(response.responseText)
                var fields = ['date', 'leadTime']
                var data = [new Object()]
                var series = []
                chartModel.chartGroups.forEach(function(pointGroup){
                    var dataRecord = new Object()
                    data[data.length] = dataRecord
                    dataRecord['date'] = pointGroup.date.substring(0, 10)
                    dataRecord['leadTime'] = pointGroup.leadTime
                    var pointsSum = 0
                    pointGroup.tiers.forEach(function(point, i){
                        pointsSum += point.count
                        if(!series[i]){
                            fields[i + 2] = 'id' + point.tierId
                            series[i] = {
                                displayName: point.tierName,
                                yField: 'id' + point.tierId
                            }
                        }
                        dataRecord['id' + point.tierId] = pointsSum
                    })
                })
                var startRecord = new Object()
                fields.forEach(function(field){
                    startRecord[field] = ''
                })
                data[0] = startRecord
                series[series.length] = {
                    displayName: 'Lead Time',
                    yField: 'leadTime',
                    style:
                    {
                        lineColor:0xB5BAC8,
                        lineAlpha:.5,
                        borderColor:0xB5BAC8,
                        fillColor:0xffffff 
                    }
                }
                var chartCmp = Ext.getCmp('flowChart')
                chartCmp.store = new Ext.data.JsonStore({
                    fields: fields,
                    data: data
                });
                chartCmp.series = series
                chartCmp.refresh()
            }
        });
    }

    function initComboboxHandlers(){
        Ext.getCmp('scaleCombo').on('select', function(combo, selection){
            drawWorkflowChart(boardFromRequest, selection.data.scaleId)
        })
        Ext.getCmp('projectCombo').on('select', function(combo, selection){
            var scale = Ext.getCmp('scaleCombo').getValue()
            if(!scale) scale = scaleFromRequest
            drawWorkflowChart(boardFromRequest, scale, selection.data.projectId)
        })
    }

    function initDateRange(startDate, endDate){
        updateReport = function(){
            if(board.lastQuery)
                initReportForRange(board, board.lastQuery)
            else
                initReport(board, Ext.getCmp('startdt').getValue(), Ext.getCmp('enddt').getValue())
        }
        Ext.getCmp('startdt').setValue(startDate)
        Ext.getCmp('startdt').on('select', updateReport)
        Ext.getCmp('enddt').setValue(endDate)
        Ext.getCmp('enddt').on('select', updateReport)
    }

    function initChartControls(board){
        var data = [['', 'All Projects']]
        var workflows = board.workflows
        for(var i = 0; i < workflows.length; i++){
            if(workflows[i]._id)
                data[data.length] = [workflows[i]._id, workflows[i].name]
        }
        Ext.getCmp('projectCombo').store = new Ext.data.ArrayStore({
            fields: ['projectId', 'project'],
            data: data
        })
    }

    function initReportForRange(board, query){
        initReport(board,
            Ext.getCmp('startdt').getValue(),
            Ext.getCmp('enddt').getValue(),
            query)
        board.lastQuery = query
    }

    function setCurrentTime(date){
        var copy = new Date(date)
        var currentDate = new Date()
        copy.setHours(currentDate.getHours())
        copy.setMinutes(currentDate.getMinutes())
        copy.setSeconds(currentDate.getSeconds())
        return copy
    }

    function initReport(board, fromDate, toDate, query){
        var data = []
        if(!query) query = new Object()
        query['updateDate'] = {
            $gt: fromDate,
            $lt: setCurrentTime(toDate)
        }
        var encodedQuery = Ext.encode(query).replace(/"ObjectId\(\'([a-z0-9]+)\'\)\"/gi, 'ObjectId(\'$1\')')
        var reportModel = Ext.decode(GET(RESOURCES + RS_REPORTMODEL
            + '/' + board._id
            + '/' + encodedQuery))        
        reportModel.taskHistoryEntries.forEach(function(entry, i){
            var task = entry.taskFact.task
            data[data.length] = [task._id,
            task.description,
            entry.workflow.name,
            entry.tier.name,
            task.assigneeId,
            task.estimation,
            Math.round(entry.daysActive*1000)/1000,
            entry.taskCreated.substring(0, 10),
            entry.taskFact.updateDate.substring(0, 10)]
        })
        Ext.getCmp('historygrid').store.loadData(data)
    }

    initComboboxHandlers()
    var board = Ext.decode(GET(RESOURCES + RS_BOARD + '/' + boardFromRequest))
    var today = new Date()
    var weekAgo = new Date()    
    weekAgo.setDate(weekAgo.getDate() - 7)
    initDateRange(weekAgo, today)
    initChartControls(board)
    initReport(board, weekAgo, today)
    drawWorkflowChart(boardFromRequest, scaleFromRequest)

});

