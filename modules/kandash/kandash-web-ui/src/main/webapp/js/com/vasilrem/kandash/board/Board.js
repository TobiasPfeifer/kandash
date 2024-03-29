/**
 * Represents kanban board
 */
com.vasilrem.kandash.board.Board = Ext.extend(Ext.Panel, {
    margins: '5 5 5 5',
    layout: 'column',
    autoScroll: true,

    /** List tier identifiers/names that present on the board */
    tiers: [],

    /** Default height of the tier-cell body */
    defaultProjectPaneHeight:0,

    /** Cache of the cells (projects/tiers) on the board */
    boardGrid: new Array(),

    /**
     *Add new project to the board
     *@param projectId project identifier (if null, new project is created on backend)
     *@param name project name
     *@return project ID
     **/
    addProject: function(projectId, name){
        if(!projectId){
            projectId = POST(RESOURCES + RS_PROJECT + '/' + this.id,
            {
                'name': name
            })
        }
        var projectPanel = this.add({
            id: projectId,
            xtype:'kandash.project',
            title:name
        })
        this.updateProjectWidths()
        this.boardGrid[projectId] = new Array(this.tiers.length)
        for(var j=0;j<this.tiers.length; j++){
            var boardCell = projectPanel.add({
                xtype: 'kandash.tier',
                id: projectId+'_'+this.tiers[j].id,
                title: this.tiers[j].name,
                isDeletable: this.tiers[j].isDeletable,
                order: j,
                wipLimit: this.tiers[j].wipLimit,
                height:this.getHeight()/this.tiers.length - 20
            })
            boardCell.setTitle(boardCell.getDisplayableName())
            boardCell.on("collapse", this.updateTiersHeight)
            boardCell.on("expand", this.updateTiersHeight)
            this.boardGrid[projectId][this.tiers[j].id] = boardCell
        }
        this.doLayout()
        var projectPaneDefaultHeightIsSet = this.defaultProjectPaneHeight>0
        for(j=0;j<this.tiers.length; j++){
            this.boardGrid[projectId][this.tiers[j].id].dd = new Ext.dd.DDProxy(projectId+'_'+this.tiers[j].id, 'dropdownGroup_' + projectId);
            this.boardGrid[projectId][this.tiers[j].id].dd.endDrag = function() {
            }
            if(!projectPaneDefaultHeightIsSet){
                this.defaultProjectPaneHeight += (this.boardGrid[projectId][this.tiers[j].id].body.getHeight())
            }
        }
        return projectId
    },

    /**
 * Updates widths of project columns when a new column is added / existing
 * column is deleted
 */
    updateProjectWidths: function(){
        for(var i=0;i<this.items.length; i++){
            this.items.items[i].columnWidth = 1/this.items.length
        }
    },

    /**
 * Handles collapse of a tier cell, in order to resize other expanded tiers
 * @param projectIdentifier identifier of the project (pane) that should
 * be updated
 */
    updateTiersHeight: function(var1, var2, var3, projectIdentifier){
        var projectId
        
        if(projectIdentifier){
            projectId = projectIdentifier
        }else{
            projectId = this.getId().substring(0,this.getId().indexOf('_'))
        }
        var tiers = getBoard().boardGrid[projectId]
        var tiersCount = getBoard().tiers.length
        var collapsedTiers = 0
        for(tier in tiers){
            if(tiers[tier] && tiers[tier].collapsed){
                collapsedTiers++
            }
        }
        for(tier in tiers){
            if(tiers[tier] && !tiers[tier].collapsed && tiers[tier].setHeight){
                if(!tiers[tier].body){
                    tiers[tier].ownerCt.doLayout()
                }
                try{
                    if(tiers[tier].body){
                        tiers[tier].body.setHeight(getBoard().defaultProjectPaneHeight/(tiersCount-collapsedTiers))
                    }
                }catch(e){
                    print(e)
                }
            }
        }
        getBoard().doLayout()
    },

    /**
 * Gets projectid-to-project map
 * @return projectid-to-project map
 */
    getProjects: function(){
        var identifiers = new Array()
        for(var i=0; i<this.items.length; i++){
            identifiers[this.items.items[i].id] =this.items.items[i]
        }
        return identifiers
    },

    /**
 * Adds a new tier to the tier cache (private method!)
 * @param tierId tier identifier (if null, new tier is created on backend)
 * @param name tier name
 * @param isDeletable defines if the tier can be deleted fom the board
 * @param order tier position (from top)
 * @param wipLimit work in progress limit (maximum number of tasks that can
 * be assigned to the tier)
 * @return tier identifier
 */
    addTier: function(tierId, name, isDeletable, order, wipLimit){
        if(!tierId){
            tierId = POST(RESOURCES + RS_TIER + '/' + getBoard().id, {
                'name': name,
                'order': order,
                'wipLimit': wipLimit
            })
        }
        this.tiers[order] = {
            'id': tierId,
            'name': name,
            'isDeletable': isDeletable,
            'wipLimit': wipLimit
        }
        return tierId
    },

    /**
 * Adds a new task to the board
 * @param taskId task identifier (if null, new task is created on backend)
 * @param projectId identifier of the project the task should be assigned to
 * @param tierId tier of the task
 * @param taskName task summary to be displayed
 * @param assignedTo team-member responsible for the task
 * @param estimation time required to complete the task
 * @param priority task priority
 * @param offsetLeft offset of the task from the left of board
 * cell(project/tier)
 * @param offsetTop offset of the task from the top of board cell(project/tier)
 * @param doRefresh force tier cell refresh (true/false)
 * @return task identifier
 */
    addTask: function(taskId, projectId, tierId, taskName, assignedTo, estimation,
        priority, offsetLeft, offsetTop, doRefresh){
        if(!taskId){
            taskId = POST(RESOURCES + RS_TASK + '/' + getBoard().id, {
                'assigneeId': assignedTo, // TO CHANGE: assignee name should be replaced with assignee ID
                'description': taskName,
                'estimation': estimation,
                'offsetLeft': offsetLeft, // TO CHANGE: relative offset left
                'offsetTop': offsetTop, // TO CHANGE: relative offset top
                'priority': priority,
                'tierId': tierId,
                'workflowId': projectId
            })
        }
        var boardCell = this.boardGrid[projectId][tierId]
        boardCell.expand(false)
        var task = {
            xtype: 'kandash.task',
            id: taskId,
            title: taskName,
            assignedTo: assignedTo,
            estimation: estimation,
            priority: priority,
            x: offsetLeft,
            y: offsetTop,
            listeners : {
                'render' : function(thisCmp) {
                    thisCmp.on("expand", function(){
                        var form = new com.vasilrem.kandash.board.TaskForm(this.title,
                            this.assignedTo,this.estimation,this.priority)
                        this.add(form)
                        this.doLayout()
                    })
                    thisCmp.addClass("task-description")
                    thisCmp.on("collapse", function(){
                        this.remove(this.items.items[0])
                    })
                    thisCmp.el.dom.getElementsByClassName('x-panel-tc')[0].className='x-panel-tc-'+getPriorityById(task.priority).toLowerCase()
                    thisCmp.collapse()
                }
            }
        }
        boardCell.add(task)
        if(doRefresh) boardCell.doLayout()
        return taskId
    },

    /**
 * Updates the tier across all projects on the board
 * @param tierId identifier of the tire to be updated
 * @param tierName tier name
 * @param wipLimit WiP limit
 * @param order absolute order of the tier
 * @return returns true, if tier order was updated
 */
    updateTier: function(tierId, tierName, wipLimit, order){
        var originalTierOrder = -1
        var projects = this.getProjects()
        var orderUpdated = false
        for(project in projects){
            var projectTierCell = this.boardGrid[project][tierId]
            if(projectTierCell){
                originalTierOrder = projectTierCell.getTierOrder()
                projectTierCell.setTitle(projectTierCell.getDisplayableName(tierName, wipLimit))
                projectTierCell.wipLimit = wipLimit
            }
        }
        
        var tier = this.tiers[originalTierOrder]
        if(tier){
            tier.name = tierName
            tier.wipLimit = wipLimit
            if(order != null){
                var targetTier = this.tiers[order]
                this.tiers[order] = this.tiers[originalTierOrder]
                this.tiers[originalTierOrder] = targetTier
                orderUpdated = true
            }
        }
        return orderUpdated
    },

    /**
 * Adds new tier to all projects
 * @param tierId tier identifier
 * @param tierName name of the new tier
 * @param position order of the tier ('0' - last tier)
 * @param wipLimit work in progress limit (maximum number of tasks that can
 * be assigned to the tier)
 */
    createTier: function(tierId, tierName, position, wipLimit){
        
        var projects = this.getProjects()
        this.tiers.splice(position, 0, {
            'id': tierId,
            'name': tierName,
            'isDeletable': true,
            'wipLimit': wipLimit
        })
        for(project in projects){
            if(projects[project].insertTier){
                var tier = projects[project].insertTier(tierId, tierName, position, wipLimit)
                this.boardGrid[project][tierId] = tier
                this.updateTiersHeight(null, null, null, project)
            }
        }
    },
        
    /**
 * Removes tier from cache
 * @param tierId identifier of the tier to be removed from cache
 */
    _removeTierFromCache: function(tierId){
        for(var i=0; i<this.tiers.length; i++){
            if(this.tiers[i].id == tierId){
                this.tiers.remove(this.tiers[i])
                return
            }
        }
    },

    /**
 * Removes tier from all project on the boa�d
 * @param tierId identifier of the tier to be removed
 **/
    removeTier: function(tierId){
        var projects = this.getProjects()
        for(project in projects){
            if(projects[project].removeTier){
                projects[project].removeTier(tierId)
                this.boardGrid[project][tierId] = null
                this._removeTierFromCache(tierId)
            }
        }
    },

    /**
 * Removes project from the board
 * @param project to be removed
 */
    removeProject: function(project){
        this.remove(project)
        this.resizeProjectColumns()
    },

    /**
 * Gets list of tasts assigned to the tier across all projects
 * @param tierId tier identifier
 */
    getTasksPerTier: function(tierId){
        
        var tasks = new Array()
        for(var i=0; i<this.items.length; i++){
            tasks.push(this.boardGrid[this.items.items[i].id][tierId].getTasks())
        }
        return tasks.flatten()
    },

    /**
     * Refreshes content of grid cells
     */
    refreshBoardGrid: function(){
        var projects = this.getProjects()
        for(project in projects)
            for(tier in this.tiers)
                if(this.boardGrid[project][this.tiers[tier].id]){
                    var cell = this.boardGrid[project][this.tiers[tier].id]
                    cell.doLayout()
                }
    },

    /**
 * Gets count of visible projects
 **/
    getVisibleProjectsCount: function(){
        var count = 0
        for(var i=0;i<this.items.length; i++){
            if(!this.items.items[i].hidden){
                count++
            }
        }
        return count
    },

    /**
 * Resizes project columns on the board
 */
    resizeProjectColumns: function(){
        for(var i=0;i<this.items.length; i++){
            if(!this.items.items[i].hidden){
                this.items.items[i].columnWidth = 1/this.getVisibleProjectsCount()
            }
        }
        this.doLayout()
    }
});

Ext.reg('kandash.board', com.vasilrem.kandash.board.Board);

