/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Form of the project creation pop-up
 **/
var createProjectDialogForm = new Ext.form.FormPanel({
    id: 'createProjectDialogForm',
    baseCls: 'x-plain',
    labelWidth: 100,
    monitorValid:true,
    defaultType: 'textfield',
    items: [{
        fieldLabel: 'Project Name',
        allowBlank: false,
        name: 'projectname',
        anchor:'100%'
    }]
})

/**
 * New projects can be added to the board via this pop-up
 */
var createProjectDialog = new Ext.Window({
    id: createProjectDialog,
    title: 'Create project',
    width: 300,
    height:110,
    minWidth: 300,
    minHeight: 110,
    layout: 'fit',
    plain:true,
    bodyStyle:'padding:5px;',
    buttonAlign:'center',
    items: createProjectDialogForm,

    buttons: [{
        text: 'Create',
        type: 'submit',
        handler: function(){
            var form = Ext.getCmp('createProjectDialogForm').getForm()
            if(!form.isValid()){
                return
            }
            var projectName = form.items.items[0].getValue()
            getBoard().addProject(null, projectName)
            createProjectDialog.hide()
        }
    },{
        text: 'Cancel',
        handler: function(){
            createProjectDialog.hide()
        }
    }]
});
