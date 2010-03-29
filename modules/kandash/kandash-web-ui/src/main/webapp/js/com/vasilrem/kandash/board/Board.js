/**
 * Represents kanban board
 */
com.vasilrem.kandash.board.Board = Ext.extend(Ext.Panel, {
    id: 'projectboard',
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
     *@param id project identifier
     *@param name project name
     **/
    addProject: function(id, name){
        var projectPanel = this.add({
            xtype:'kandash.project',
            id:id,
            title:name
        })
        this.updateProjectWidths()
        this.boardGrid[id] = new Array(this.tiers.length)
        for(var j=0;j<this.tiers.length; j++){
            var boardCell = projectPanel.add({
                xtype: 'kandash.tier',
                id: id+'_'+this.tiers[j].id,
                title: this.tiers[j].name,
                height:this.getHeight()/this.tiers.length - 20
            })
            boardCell.on("collapse", this.updateTiersHeight)
            boardCell.on("expand", this.updateTiersHeight)
            this.boardGrid[id][this.tiers[j].id] = boardCell
        }
        this.doLayout()
        var projectPaneDefaultHeightIsSet = this.defaultProjectPaneHeight>0
        for(j=0;j<this.tiers.length; j++){
            this.boardGrid[id][this.tiers[j].id].dd = new Ext.dd.DDProxy(id+'_'+this.tiers[j].id, 'dropdownGroup_' + id);
            this.boardGrid[id][this.tiers[j].id].dd.endDrag = function() {
            }
            if(!projectPaneDefaultHeightIsSet){
                this.defaultProjectPaneHeight += this.boardGrid[id][this.tiers[j].id].body.getHeight()
            }
        }
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
        var board = Ext.getCmp('projectboard')
        var projectId
        if(projectIdentifier){
            projectId = projectIdentifier
        }else{
            projectId = this.getId().substring(0,this.getId().indexOf('_'))
        }
        var tiers = board.boardGrid[projectId]
        var tiersCount = board.tiers.length
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
                if(tiers[tier].body){
                    tiers[tier].body.setHeight(board.defaultProjectPaneHeight/(tiersCount-collapsedTiers))
                }
            }
        }
        board.doLayout()
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
     * @param id tier ideantifier
     * @param name tier name
     * @param isDeletable defines if the tier can be deleted fom the board
     */
    addTier: function(id, name, isDeletable){
        this.tiers[this.tiers.length] = {
            'id': id,
            'name': name,
            'isDeletable': isDeletable
        }
    },

    /**
     * Adds a new task to the board
     * @param projectId identifier of the project the task should be assigned to
     * @param tierId tier of the task
     * @param taskId task identifier
     * @param taskName task summary to be displayed
     * @param assignedTo team-member responsible for the task
     * @param estimation time required to complete the task
     * @param priority task priority
     * @param offsetLeft offset of the task from the left of board
     * cell(project/tier)
     * @param offsetTop offset of the task from the top of board cell(project/tier)
     */
    addTask: function(projectId, tierId, taskId, taskName, assignedTo, estimation,
        priority, offsetLeft, offsetTop){
        var boardCell = this.boardGrid[projectId][tierId]
        boardCell.expand(false)
        boardCell.add({
            xtype: 'kandash.task',
            id:taskId,
            title: taskName,
            assignedTo: assignedTo,
            estimation: estimation,
            priority: priority,
            x: offsetLeft,
            y: offsetTop,
            items: new com.vasilrem.kandash.board.TaskForm(taskName,
                assignedTo,estimation,priority)
        })
        boardCell.doLayout()
        Ext.getCmp(taskId).el.dom.getElementsByClassName('x-panel-tc')[0].className='x-panel-tc-'+getPriorityById(priority).toLowerCase()
    },

    /**
     * Updates the tier across all projects on the board
     * @param tierId identifier of the tire to be updated
     * @param tierName tier name
     */
    updateTier: function(tierId, tierName){
        var projects = this.getProjects()
        for(project in projects){
            var projectTierCell = this.boardGrid[project][tierId]
            if(projectTierCell){
                projectTierCell.setTitle(tierName)
            }
        }
        for(var i =0; i<this.tiers.length; i++){
            if(this.tiers[i].id == tierId){
                this.tiers[i].name = tierName
                return
            }
        }
    },

    /**
     * Adds new tier to all projects
     * @param tierId tier identifier
     * @param tierName name of the new tier
     * @param position order of the tier ('0' - last tier)
     */
    createTier: function(tierId, tierName, position){
        var projects = this.getProjects()
        this.tiers.splice(position, 0, {
            'id': tierId,
            'name': tierName,
            'isDeletable': true
        }) 
        for(project in projects){
            if(projects[project].insertTier){
                var tier = projects[project].insertTier(project + '_' + tierId, tierName, position)
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
     * Removes tier from all project on the boaêd
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

