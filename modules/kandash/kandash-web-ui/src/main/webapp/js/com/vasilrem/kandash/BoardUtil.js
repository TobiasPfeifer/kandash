/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Gets disaplayed board
 */
function getBoard(){
    return Ext.getCmp('viewport').findByType('kandash.board')[0]
}

/**
 * Initializes board from JSON object recieved from server
 * @param boardId board identifier
 */
function initBoard(boardId){
    var boardModel = Ext.decode(GET(RESOURCES + RS_BOARD + '/' + boardId))
    var board = getBoard()
    board.id = boardId
    board.name = boardModel.name
    for(var i=0; i<boardModel.tiers.length; i++){
        var tier = boardModel.tiers[i]
        board.addTier(tier._id, tier.name, true, tier.order, tier.wipLimit)
    }
    for(i=0; i<boardModel.workflows.length; i++){
        var workflow = boardModel.workflows[i]
        board.addProject(workflow._id, workflow.name)
    }
    for(i=0; i<boardModel.tasks.length; i++){
        var task = boardModel.tasks[i]
        board.addTask(task._id, task.workflowId, task.tierId, 
            task.description, task.assigneeId, task.estimation,
            task.priority, task.offsetLeft, task.offsetTop)
    }
}
