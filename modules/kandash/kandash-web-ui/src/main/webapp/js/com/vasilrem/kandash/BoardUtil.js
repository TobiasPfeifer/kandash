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
 * @return initialized board
 */
function initBoard(boardId){
    setProgressBarText(0, "Loading board model...")
    var boardModel = Ext.decode(GET(RESOURCES + RS_BOARD + '/' + boardId))
    setProgressBarText(1, "Board model is loaded!")
    var board = getBoard()
    board.removeAll()
    board.id = boardId
    board.name = boardModel.name
    setProgressBarText(0, "Adding tiers...")
    for(var i=0; i<boardModel.tiers.length; i++){
        var tier = boardModel.tiers[i]
        board.addTier(tier._id, tier.name, true, tier.order, tier.wipLimit)
        var tiersLoadedPercent = (i + 1)/boardModel.tiers.length
        setProgressBarText(tiersLoadedPercent, "Added " + (i + 1) + " tiers of " + boardModel.tiers.length)
    }
    setProgressBarText(1, "All tiers are loaded!")
    setProgressBarText(0, "Adding projects...")
    for(i=0; i<boardModel.workflows.length; i++){
        var workflow = boardModel.workflows[i]
        board.addProject(workflow._id, workflow.name)
        var projectsLoadedPercent = (i + 1)/boardModel.workflows.length
        setProgressBarText(projectsLoadedPercent, "Added " + (i + 1) + " projects of " + boardModel.workflows.length)
    }
    setProgressBarText(1, "All projects are loaded!")
    if(boardModel.tasks.length > 0){
        initTasksInBackground(boardModel.tasks, board)
    }    
    return board
}

/**
 * Initializes tasks in background
 * @param tasksArray array of tasks to be displayed
 * @param board board board to display tasks at
 */
function initTasksInBackground(tasksArray, board){
    var pos = 0;
    var numToProcess = Math.round(Math.sqrt(tasksArray.length))
    var iterationNum = 0
    function iteration() {
        iterationNum++
        var j = Math.min(pos + numToProcess, tasksArray.length);
        initTasks(tasksArray, board, pos, j)        
        setTaskLoadProgress(j, tasksArray.length)
        board.refreshBoardGrid()
        pos += numToProcess;
        if (pos < tasksArray.length)
            setTimeout(iteration, 10);
    }
    iteration();
}

/**
 * Displays tasks on the board
 * @param tasksArray array of tasks to be displayed
 * @param board board board to display tasks at
 * @param from start index in tasks array
 * @param to end index in tasks array
 */
function initTasks(tasksArray, board, from, to){
    for(i=from; i<to; i++){
        var task = tasksArray[i]
        board.addTask(task._id, task.workflowId, task.tierId,
            task.description, task.assigneeId, task.estimation,
            task.priority, task.offsetLeft, task.offsetTop)        
    }
}

/**
 * Sets progress bar text
 * @param percentLoaded
 * @param text text to display on progress bar
 */
function setProgressBarText(percentLoaded, text){
    var progressBar = Ext.getCmp('taskLoadProgress')
    progressBar.updateProgress(percentLoaded, text);
}

/**
 * displays progress of tasks initialization
 * @param loaded numer of tasks loaded at the moment
 * @param total total numer of tasks to load
 */
function setTaskLoadProgress(loaded, total){
    if(loaded != total){
        setProgressBarText(loaded/total, 'Loaded '+loaded+' tasks of '+total+'...');
    }else{
        setProgressBarText(1, 'Loaded '+loaded+' tasks!');
    }
}
