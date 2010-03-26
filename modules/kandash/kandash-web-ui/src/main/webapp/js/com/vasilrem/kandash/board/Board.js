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
    defaultTierHeight:'-1',

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
            boardCell.on("collapse", this.onTierCollapse)
            boardCell.on("expand", this.onTierCollapse)
            this.boardGrid[id][this.tiers[j].id] = boardCell
        }
        this.doLayout()
        for(j=0;j<this.tiers.length; j++){
            this.boardGrid[id][this.tiers[j].id].dd = new Ext.dd.DDProxy(id+'_'+this.tiers[j].id, 'dropdownGroup_' + id);
            this.boardGrid[id][this.tiers[j].id].dd.endDrag = function() {
            }
            this.defaultTierHeight = this.boardGrid[id][this.tiers[j].id].body.getHeight()
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
     */
    onTierCollapse: function(){
        var board = Ext.getCmp('projectboard')
        var projectId = this.getId().substring(0,this.getId().indexOf('_'))
        var tiers = board.boardGrid[projectId]
        var collapsedTiers = 0
        for(tier in tiers){
            if(tiers[tier].collapsed){
                collapsedTiers++
            }
        }
        for(tier in tiers){
            if(!tiers[tier].collapsed && tiers[tier].setHeight){
                tiers[tier].body.setHeight(board.defaultTierHeight*tiers.length/(tiers.length-collapsedTiers))
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
     * Adds a new tier to the board
     * @param id tier ideantifier
     * @param name tier name
     */
    addTier: function(id, name){
        this.tiers[this.tiers.length] = {
            'id': id,
            'name': name
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
    }
});

Ext.reg('kandash.board', com.vasilrem.kandash.board.Board);

