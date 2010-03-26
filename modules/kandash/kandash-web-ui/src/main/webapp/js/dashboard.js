//////
//// THIS CODE IS A PROTOTYPE AND WILL BE RAFACTORED SOON
//////
Ext.onReady(function(){

    Ext.QuickTips.init();

    var tiers = []
    var boardGrid = new Array()
    var projects = new Array()

    var viewport = new Ext.Viewport({
        layout:'border',
        items:[{
            region: 'north',
            layout:'fit',
            tbar: [{
                text: 'Add Task',
                iconCls: 'add16',
                handler: function(){

                    showTaskDialog()
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
        },
        /*{
            region:'west',
            id:'west-panel',
            title:'West',
            split:true,
            width: 200,
            minSize: 175,
            maxSize: 400,
            collapsible: true,
            margins:'35 0 5 5',
            cmargins:'35 5 5 5',
            layout:'accordion',
            layoutConfig:{
                animate:true
            }*//*,
            items: [{
                html: '',
                title:'Navigation',
                autoScroll:true,
                border:false,
                iconCls:'nav'
            },{
                title:'Settings',
                html: '',
                border:false,
                autoScroll:true,
                iconCls:'settings'
            }]
        },*/{
            region:'center',
            margins:'5 5 5 5',
            layout:'column',
            autoScroll:true
        }]
    });

    function addProject(id, name){
        projects[id] = name
        var projectsLayout = viewport.items.items[1]
        var projectPanel = projectsLayout.add({
            id: id,
            columnWidth:.1,
            bodyStyle:'border:none',
            items:[{
                title: name,
                tools: [{
                    id: 'gear',
                    qtip: 'Update',
                    handler: function(event, toolEl, panel){
                    }
                },{
                    id: 'close',
                    qtip: 'Delete',
                    handler: function(event, toolEl, panel){

                        var projectId = panel.ownerCt.id
                        projectsLayout.remove(panel.ownerCt)
                        projects[projectId]=null
                        // recalcualte project columns width
                        for(var i=0;i<projectsLayout.items.length; i++){
                            projectsLayout.items.items[i].columnWidth = 1/projectsLayout.items.length
                        }
                        projectsLayout.doLayout()
                    }
                }],
                items:{
                    layout:'anchor'
                }
            }]
        })
        // recalcualte project columns width
        for(var i=0;i<projectsLayout.items.length; i++){
            projectsLayout.items.items[i].columnWidth = 1/projectsLayout.items.length
        }
        // add tiers to columns
        boardGrid[id] = new Array(tiers.length)
        for(var j=0;j<tiers.length; j++){
            var boardCell = projectPanel.items.items[0].add({
                id: id+'_'+tiers[j].id,
                title: tiers[j].name,
                height:projectsLayout.getHeight()/tiers.length - 20,
                collapsible : true,
                bodyStyle:'border:none',
                autoScroll:true,
                layout:'absolute',
                tools: [{
                    id: 'gear',
                    qtip: 'Update',
                    handler: function(event, toolEl, panel){
                    }
                },{
                    id: 'close',
                    qtip: 'Delete',
                    handler: function(event, toolEl, panel){

                    }
                }],
                items:{
                    layout:'anchor'
                }
            })
            boardCell.on("collapse", onTierCollapse)
            boardCell.on("expand", onTierCollapse)
            boardGrid[id][tiers[j].id] = boardCell
        }
        projectsLayout.doLayout()
        for(j=0;j<tiers.length; j++){
            boardGrid[id][tiers[j].id].dd = new Ext.dd.DDProxy(id+'_'+tiers[j].id, 'dropdownGroup_' + id);
            boardGrid[id][tiers[j].id].dd.endDrag = function() {
            }
            defaultTierHeight = boardGrid[id][tiers[j].id].body.getHeight()
        }
    }

    var defaultTierHeight

    function onTierCollapse(){
        var projectsLayout = viewport.items.items[1]
        var projectId = this.getId().substring(0,this.getId().indexOf('_'))
        var tiers = boardGrid[projectId]
        var collapsedTiers = 0
        for(tier in tiers){
            if(tiers[tier].collapsed){
                collapsedTiers++
            }
        }
        for(tier in tiers){      
            if(!tiers[tier].collapsed && tiers[tier].setHeight){
                tiers[tier].body.setHeight(defaultTierHeight*tiers.length/(tiers.length-collapsedTiers))
            }
        }
        projectsLayout.doLayout()
    }

    function addTask(projectId, tierId, taskId, taskName, offestLeft, offsetTop){
        var boardCell = boardGrid[projectId][tierId]
        var taskForm = new Ext.form.FormPanel({
            baseCls: 'x-plain',
            labelWidth: 100,
            defaultType: 'label',
            items: [{
                fieldLabel: 'Summary',
                name: 'description',
                html: 'Quite short task summary (~140 symbols)',
                anchor: '100%'
            },{
                fieldLabel: 'Assigned To',
                name: 'assignee',
                html: 'John Smith',
                anchor:'100%'
            },{
                fieldLabel: 'Estimated (m/d)',
                name: 'estimation',
                html: '5',
                anchor:'100%'
            },{
                fieldLabel: 'Priority',
                name: 'priority',
                html: 'High',
                anchor:'100%'
            }]
        })
        boardCell.add(
        {
            id:taskId,
            title:taskName,
            frame : true,
            collapsible : true,
            x: offestLeft,
            y: offsetTop,
            width:250,
            height: 150,
            autoScroll:true,
            items:taskForm,
            tools: [{
                id: 'gear',
                qtip: 'Update',
                handler: function(event, toolEl, panel){
                    updateTaskDialog.show()
                }
            },{
                id: 'close',
                qtip: 'Delete',
                handler: function(event, toolEl, panel){

                }
            }]
        })
        boardCell.doLayout()
        var task = Ext.get(taskId);
        task.dd = new Ext.dd.DDProxy(taskId, 'dropdownGroup_' + projectId);
        task.dd.onDragOver = function(e, targetId) {
            var currentOwner = Ext.getCmp(this.getEl().id).ownerCt.id
            //console.log('dragOver: ' + targetId);
            //console.log('currentOwner: ' + currentOwner);
            if(currentOwner != targetId &&
                targetId.indexOf("task")<0){
                var target = Ext.get(targetId);
                lastTarget = target;
            }
        },
        task.dd.onInvalidDrop = function() {
            //console.log('invalid drop');
            invalidDrop = true;
        },

        task.dd.onDragOut = function(e, targetId) {
            //console.log('dragOut: ' + targetId);
            //console.log('currentOwner: ' + Ext.getCmp(this.getEl().id).ownerCt.id);
            lastTarget = null;
        }
        task.dd.endDrag = function() {
            if(!invalidDrop){
                var dragEl = Ext.get(this.getDragEl());
                var el = Ext.get(this.getEl());
                if(lastTarget) {
                    //console.log('dropping ' + el.id + ' to ' + lastTarget.id);
                    Ext.getCmp(lastTarget.id).add(Ext.getCmp(el.id))
                    Ext.getCmp(lastTarget.id).doLayout()
                }
                el.applyStyles({
                    position:'absolute'
                });
                el.setXY(dragEl.getXY());
                el.setWidth(dragEl.getWidth());
                lastTarget = null;
            }else{
                invalidDrop = false
            }
        }
    }

    var lastTarget
    var invalidDrop

    function addTier(id, name){
        tiers[tiers.length] = {
            'id': id,
            'name': name
        }
    }

    ///////
    /// ADD PROJECT DIALOG
    ///////
    var addProjectForm = new Ext.form.FormPanel({
        id: 'addProjectForm',
        baseCls: 'x-plain',
        labelWidth: 100,
        defaultType: 'textfield',
        items: [{
            fieldLabel: 'Project Name',
            name: 'projectname',
            anchor:'100%'
        }]
    })

    var createProjectDialog = new Ext.Window({
        title: 'Create project',
        width: 300,
        height:100,
        minWidth: 300,
        minHeight: 100,
        layout: 'fit',
        plain:true,
        bodyStyle:'padding:5px;',
        buttonAlign:'center',
        items: addProjectForm,

        buttons: [{
            text: 'Create',
            type: 'submit',
            handler: function(){
                var form = Ext.getCmp('addProjectForm').getForm()
                var projectName = form.items.items[0].getValue()
                /// CALL SERVICE >> Project ID
                addProject(projectName.replace(' ', ''), projectName)
                createProjectDialog.hide()
            }
        },{
            text: 'Cancel',
            handler: function(){
                createProjectDialog.hide()
            }
        }]
    });
    ///////
    /// TASK UPDATE
    ///////

    var updateTaskForm = new Ext.form.FormPanel({
        id: 'updateTaskForm',
        baseCls: 'x-plain',
        labelWidth: 100,
        defaultType: 'textfield',
        items: [new Ext.form.ComboBox({
            fieldLabel: 'Project',
            hiddenName:'taskProject',
            valueField:'taskProjectId',
            displayField:'taskProject',
            typeAhead: true,
            mode: 'local',
            triggerAction: 'all',
            emptyText:'Choose the project...',
            selectOnFocus:true,
            anchor: '100%'
        }),{
            fieldLabel: 'Summary',
            name: 'description',
            anchor:'100%'
        },{
            fieldLabel: 'Assigned to',
            name: 'assignee',
            anchor: '100%'
        }, {
            fieldLabel: 'Estimated (m/d)',
            name: 'estimation',
            anchor: '100%'
        }, new Ext.form.ComboBox({
            fieldLabel: 'Priority',
            hiddenName:'priority',
            store: new Ext.data.ArrayStore({
                fields: ['priorityId', 'priority'],
                data: [[1, 'Low'], [2, 'Medium'], [3, 'High']]
            }),
            valueField:'priorityId',
            displayField:'priority',
            typeAhead: true,
            mode: 'local',
            triggerAction: 'all',
            emptyText:'Specify task priority...',
            selectOnFocus:true,
            anchor: '100%'
        })]
    });

    var updateTaskDialog = new Ext.Window({
        title: 'Update task',
        width: 300,
        height:200,
        minWidth: 300,
        minHeight: 200,
        layout: 'fit',
        plain:true,
        bodyStyle:'padding:5px;',
        buttonAlign:'center',
        items: updateTaskForm,

        buttons: [{
            text: 'Save',
            type: 'submit',
            handler: function(){

                var form = Ext.getCmp('updateTaskForm').getForm()
                var projectId = form.items.items[0].getValue()
                var description = form.items.items[1].getValue()
                /// CALL SERVICE >> Task ID
                addTask(projectId, tiers[tiers.length-1].id, description.replace(' ',''), description, 20, 20)
                updateTaskDialog.hide()
            }
        },{
            text: 'Cancel',
            handler: function(){
                updateTaskDialog.hide()
            }
        }]
    });

    function showTaskDialog(){
        var data = []
        for(project in projects){
            if(project!='remove')
                data[data.length] = [project, projects[project]]
        }
        updateTaskForm.items.items[0].store = new Ext.data.ArrayStore({
            fields: ['taskProjectId', 'taskProject'],
            data: data
        })
        updateTaskDialog.show()
    }
    /////////////////////
    /// API USAGE EXAMPLE

    addTier('tier3', 'Done')
    addTier('tier2', 'In Progress')
    addTier('tier1', 'TO-DO')

    addProject('project1', 'Project 1')
    addProject('project2', 'Project 2')
    addProject('project3', 'Project 3')

    addTask('project1', 'tier1', 'task1', 'Task 1', 20, 20)
    addTask('project2', 'tier1', 'task2', 'Task 2', 20, 20)


});


