/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

startMonitoring = function(boardId){
    monitorTasks(boardId)
}

updateTask = function(task1, task2){
    var offsetLeft = task2.offsetLeft - task1.x
    var offsetTop = task2.offsetTop - task1.y
    var animCfgObj = {
        easing   : 'elasticOut',
        duration : 1,
        scope    : task1,
        callback : function() {
            task1.setPosition(task2.offsetLeft,task2.offsetTop)
        }
    };
    if(offsetLeft != 0 || offsetTop != 0)
        task1.el.moveTo(task1.el.getXY()[0] + offsetLeft,task1.el.getXY()[1] + offsetTop, animCfgObj);
    if(task1.getFormTitle() != task2.description)
        task1.setFormTitle(task2.description)
    if(task1.assignedTo != task2.assignedTo)
        task1.setFormAssignedTo(task2.assignedTo)
    if(task1.estimation != task2.estimation)
        task1.setFormEstimation(task2.estimation)
    if(task1.priority != task2.priority)
        task1.setFormPriority(task2.priority)
    var cellId = task2.workflowId + '_' + task2.tierId
    if(task1.ownerCt.id != cellId){        
        task1.changeTier(cellId)
    }
}

removeTask = function(taskId){
    var task = Ext.getCmp(taskId)
    if(task){
        task.ownerCt.remove(task)
    }
}

createTask = function(task){
    getBoard().addTaskOnUI(task._id, task.workflowId,
        task.tierId,
        task.description, task.assignedTo, task.estimation,
        task.priority, 20, 20)
}

monitorTasks = function(boardId){
    ajaxCall(RESOURCES + RS_TASK + '/' + boardId, function(response){
        setTimeout("monitorTasks('" + boardId + "')", 100)
        var taskResponse = eval("(" + response.responseText.substr(0, response.responseText.length / 2) + ")")
        if(taskResponse.remove){
            removeTask(taskResponse.remove)
            return
        }
        var taskOnBoard = Ext.getCmp(taskResponse._id)
        if(!taskOnBoard){
            createTask(taskResponse)
        } else{
            updateTask(taskOnBoard, taskResponse)
        }
    }, function(){
        setTimeout("monitorTasks('" + boardId + "')", 100)
    })
}

ajaxCall = function(url, callback, failure){
    Ext.Ajax.request({
        headers : {
            'X-HTTP-Method-Override' : 'GET'
        },
        method: 'GET',
        async: true,
        url: url,
        success: callback,
        failure: failure
    });
}