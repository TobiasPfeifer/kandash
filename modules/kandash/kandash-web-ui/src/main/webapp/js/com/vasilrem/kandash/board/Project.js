/**
 * Represents project pane (column) on the board
 */
com.vasilrem.kandash.board.Project = Ext.extend(Ext.Panel, {
    layout:'anchor',
    bodyStyle:'border:none',
    tools: [{
        id: 'minimize',
        qtip: 'Minimize to toolbar',
        handler: function(event, toolEl, project){
            project.hide()
            project.ownerCt.resizeProjectColumns()
            project.addToProjectBar()
        }
    },{
        id: 'gear',
        qtip: 'Update',
        /**
         * Updates project. <br/>
         * @param event
         * @param toolEl
         * @param panel
         */
        handler: function(event, toolEl, project){
            Ext.Msg.show({
                title: 'Project name',
                msg: 'Please enter the project name:',
                width: 300,
                buttons: Ext.MessageBox.OKCANCEL,
                prompt: true,
                fn: function(btn, text){
                    if (btn == 'ok'){
                        project.setTitle(text)
                        PUT(RESOURCES + RS_PROJECT, project.toJSON())
                    }
                },
                value: project.title
            });
        }
    },{
        id: 'close',
        qtip: 'Delete',
        /**
         * Deletes project from the board.<br/>
         * @param event
         * @param toolEl
         * @param panel
         */
        handler: function(event, toolEl, project){
            Ext.Msg.show({
                title:'Delete project?',
                msg: 'Are you sure, you want to delete this project? All tasks will be lost!',
                buttons: Ext.Msg.YESNO,
                fn: function(btn){
                    if(btn == 'yes'){
                        var board = project.ownerCt
                        board.removeProject(project)
                        DELETE(RESOURCES + RS_PROJECT + '/' + project.id)
                    }
                },
                animEl: 'elId'
            });
        }
    }],

    /**
     * Inserts tier to the project pane at specified position
     * @param tierId tier identifier
     * @param tierName tier name
     * @param position tier position on the board ('0' - last tier)
     * @param wipLimit work in progress limit (maximum number of tasks that can
     * @return instance of a new tier added to the project
     */
    insertTier: function(tierId, tierName, position, wipLimit){
        return this.insert(position, {
            xtype: 'kandash.tier',
            id: this.id + '_' + tierId,
            title: tierName,
            isDeletable: true,
            order: position,
            wipLimit: wipLimit,
            height:100
        })
    },

    /**
     * Removes tier form the project
     * @param tierId identifier of the tier to be removed
     **/
    removeTier: function(tierId){
        var tierCell = Ext.getCmp(this.id + '_' + tierId)
        if(tierCell){
            this.remove(tierCell)
        }
    },

    /**
     * Adds project shrtcut to the project bar
     */
    addToProjectBar: function(){        
        var project = this
        var projectBar = Ext.getCmp('projectbar').getTopToolbar()
        projectBar.add({
            text: project.title,
            iconCls: 'taskbar-project-icon',
            handler: function(){
                project.show()
                project.ownerCt.resizeProjectColumns()
                project.ownerCt.updateTiersHeight(null, null, null, project.id)
                this.ownerCt.remove(this)
            }
        })
        projectBar.ownerCt.doLayout()
    },

    /**
     * Converts project to lightweight JSON object
     */
    toJSON: function(){
        return {
            '_id': this.id,
            'name': this.title
        }
    }
});

Ext.reg('kandash.project', com.vasilrem.kandash.board.Project);
