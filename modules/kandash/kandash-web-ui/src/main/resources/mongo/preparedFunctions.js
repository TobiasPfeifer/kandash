/**
 * Test function to make sure stored code is loaded
 * @param string any string
 * @return 'test $passed string$'
 */
test = function(string){
    return 'test ' + string
}

/**
 * Converts UTC date string into Javascript date
 * @param date UTC date string
 * @return javascript date
 */
convertToJSDate = function(date){
    var d = date.match(/^(\d{4})-(\d{2})-(\d{2})[T ](\d{2}):(\d{2}):(\d{2}(?:\.\d+)?)(Z|(([+-])(\d{2}):(\d{2})))$/i);
    return new Date(Date.UTC(d[1],d[2] - 1,d[3],d[4],d[5],d[6]|0,(d[6]*1000-((d[6]|0)*1000))|0,d[7]) + (d[7].toUpperCase() ==="Z" ? 0 : (d[10]*3600 + d[11]*60) * (d[9]==="-" ? 1000 : -1000)))
}

/**
 * Converts JS date to UTC array
 * @param D Javascript date
 * @return UTC array
 **/
toUTCArray= function(D){
    return [D.getUTCFullYear(), D.getUTCMonth(), D.getUTCDate(), 0,
    0, 0];
}

/**
 * Converts Javascript date to UTC string
 * @param date Javascript date
 * @return formatted UTC-date
 **/
toISO= function(date){
    var tem, A= toUTCArray(date), i= 0;
    A[1]+= 1;
    while(i++<7){
        tem= A[i];
        if(tem<10) A[i]= '0'+tem;
    }
    return A.splice(0, 3).join('-')+'T'+A.join(':') + '.000Z';
}

/**
 * Finds project (workflow) by ID one the board
 * @param board dashboard model JSON object
 * @param projectId project identifier
 **/
getProjectById = function(board, projectId){
    var project
    board.workflows.forEach(function(workflow){
        if(workflow._id.toString() == projectId){
            project = workflow
        }
    })
    return project
}

/**
 * Finds tier (state) by ID one the board
 * @param board dashboard model JSON object
 * @param tierId tier identifier
 **/
getTierById = function(board, tierId){
    for(tId in board.tiers){
        var t = board.tiers[tId]
        if(t._id && t._id.toString() == tierId.toString()){
            return t
        }
    }
    return null
}

/**
 * Gets the date, when the task was created (from task update facts)
 * @param taskId task identifier
 */
getTaskCreationDate = function(taskId){
    return db.taskupdatefacts.find({
        'task._id' : taskId
        }, {
        updateDate:1
        }).sort({
        updateDate:1
        }).limit(1)[0].updateDate
}

/**
 * Gets the date of the last task update (from task update facts)
 * @param taskId task identifier
 */
getTaskLastUpdateDate = function(taskId){
    return db.taskupdatefacts.find({
        'task._id' : taskId
        }, {
        updateDate:1
        }).sort({
        updateDate:-1
        }).limit(1)[0].updateDate
}

/**
 * Builds basic report model based on the passed Mongo/JSON query
 * @param boardId board identifier
 * @param query search query
 */
getReportModel = function(boardId, query){
    var reportModel = []
    var board = db.dashboardmodels.findOne({
        '_id': ObjectId(boardId)
    })
    query.boardId = board._id
    for(criterion in query){
        if(query[criterion].match && !query[criterion].match(/(ObjectId\(\'([a-z0-9]+)\'\)|([0-9]+-[0-9]+-[0-9]+T[0-9][0-9]))/gi)){
            query[criterion] = new RegExp(query[criterion], "gi")
        }
    }
    db.taskupdatefacts.find(query).forEach(
        function(taskFact){
            var task = taskFact.task
            var creationDate = getTaskCreationDate(task._id)
            var updateDate = taskFact.updateDate
            var active = (convertToJSDate(updateDate).getTime() - convertToJSDate(creationDate).getTime())/(1000*60*60*24)
            reportModel[reportModel.length] = {
                taskFact: taskFact,
                workflow: getProjectById(board, task.workflowId),
                tier: getTierById(board, task.tierId),
                taskCreated: creationDate,
                daysActive: active
            }
        })
    return {
        taskHistoryEntries:reportModel
    }
}


/*
 * Sets the new order number of the tier, and reorders other tiers appropriately
 * @param tierId tier identifier
 * @param order new tier order
 */
changeTierOrder = function(tierId, order) {
    db.dashboardmodels.find({
        'tiers._id' : ObjectId(tierId)
    }).forEach(
        function(o){
            var sourceIndex
            var targetIndex
            var sourceOrder
            for(var i=0;i<o.tiers.length;i++){
                var tier=o.tiers[i];
                if(tier._id.toString() == tierId){
                    sourceIndex = i
                    sourceOrder = tier.order
                }
                if(tier.order == order){
                    targetIndex = i
                }
            }
            o.tiers[targetIndex].order = sourceOrder
            o.tiers[sourceIndex].order = order
            db.dashboardmodels.save(o);
        })
}

/**
 * Reorders tiers after tier update
 * @param boardId identifier of the board
 * @param startingFromOrder reordering should start from the specified order
 * @param incrementor order of tiers will be incremented by specified value
 */
updateTiersOrder = function(boardId, startingFromOrder, incrementor) {
    db.dashboardmodels.find({
        '_id' : ObjectId(boardId)
    }).forEach(
        function(o){
            for(var i=0;i<o.tiers.length;i++){
                var tier=o.tiers[i];
                if(tier.order>startingFromOrder){
                    tier.order+=incrementor;
                }
            }
            db.dashboardmodels.save(o);
        })
}

/**
 * Gets workflow chart point group for the specified date
 * @param projectIds project identifiers
 * @param date the date chartpoint group will be fetched for
 * @param tierOrders array of tier identifiers associated with order of the tier on the board
 * @return chart point group
 **/
getWorkflowChartPointGroup = function(projectIds, date, tierOrders){
    var yesterday = new Date(date)
    yesterday.setDate(yesterday.getDate() - 1)
    var chartPointGroup = db.chartpointgroups.findOne({
        "workflowId" : {
            $in : projectIds
        },
        date : {
            $gt:toISO(yesterday),
            $lt:toISO(date)
        }
    })
    if(chartPointGroup){
        chartPointGroup.tiers.sort(function(tierA, tierB){
            if(tierOrders[tierA.tierId] < tierOrders[tierB.tierId]) return -1
            if(tierOrders[tierA.tierId] > tierOrders[tierB.tierId]) return 1
            return 0
        })
    }
    return chartPointGroup
}

/**
 * Gets array of tier identifiers associated with tier order
 * @param boardId board identifier
 **/
getTierOrders = function(boardId) {
    var tierOrders = new Object()
    db.dashboardmodels.findOne({
        _id: ObjectId(boardId)
    }).tiers.forEach(function(tier){
        tierOrders[tier._id] = tier.order
    })
    return tierOrders
}

/**
 * Builds model for the cumulative flow chart
 * @param boardId board identifier the chart will be built for
 * @param scale chart scale (0=day/1=week/2=month)
 * @param projectId identifier of the project the chart will be built for
 * (for all projects, of not specified)
 * @return chart model
 */
getWorkflowChartModel = function(boardId, scale, projectId) {
    var chartPointGroups = []
    var projectIds = []
    var pointGroup
    if(projectId) projectIds[0] = ObjectId(projectId)
    else{
        db.dashboardmodels.findOne({
            _id: ObjectId(boardId)
        }, {
            workflows: true
        }).workflows.forEach(function(project){
            projectIds[projectIds.length] = project._id
        })
    }

    var lowerBound = convertToJSDate(db.chartpointgroups.find({
        "workflowId" : {
        $in : projectIds
        }
        }, {
        date: 1
        }).sort({
        date: 1
        }).limit(1)[
        0].date)

    var upperBound = new Date()
    var middleBound = new Date(lowerBound)
    var tierOrders = getTierOrders(boardId)

    while(middleBound < upperBound){
        pointGroup = getWorkflowChartPointGroup(projectIds, middleBound, tierOrders)
        if(pointGroup)
            chartPointGroups[chartPointGroups.length] = pointGroup
        lowerBound = new Date(middleBound)
        switch(scale){
            case 0:
                middleBound.setDate(middleBound.getDate() + 1)
                break;
            case 1:
                middleBound.setDate(middleBound.getDate() + 7)
                break;
            case 2:
                middleBound.setMonth(middleBound.getMonth() + 1)
                break;
        }
    }
    pointGroup = getWorkflowChartPointGroup(projectIds, upperBound, tierOrders)
    if(pointGroup)
        chartPointGroups[chartPointGroups.length] = pointGroup
    return {
        chartGroups: chartPointGroups
    }
}

/**
 * Gets chart point group for the current date
 * @param workflowId project identifier
 * @return chartPointGroup
 */
getTodayChartPointGroup = function(workflowId){
    var query = {
        workflowId: workflowId,
        date: toISO(new Date()),
        leadTime: calculateLeadTime(workflowId, toISO(new Date()))
    }
    var chartPointGroup = db.chartpointgroups.findOne(query)
    if(!chartPointGroup){
        query.tiers = []
        db.chartpointgroups.insert(query)
        chartPointGroup = db.chartpointgroups.findOne(query)
    }
    return chartPointGroup
}

/**
 * Stores tier statistics
 * @param chartPointGroupId identifier of the chart point to update
 * @param tier tier JSON object
 * @param taskCount of tasks assigned to the tier
 */
storeTierStatistics = function(chartPointGroupId, tier, taskCount){
    var chartPoint = {
        _id: ObjectId(),
        tierId: tier._id,
        tierName: tier.name,
        count: taskCount
    }
    var isPointExists = db.chartpointgroups.findOne({
        _id: chartPointGroupId,
        'tiers.tierId': tier._id
    }, {
        _id: 1
    })
    if(isPointExists)
        db.chartpointgroups.update({
            _id: chartPointGroupId,
            'tiers.tierId': tier._id
        },{
            $set:{
                'tiers.$':chartPoint
            }
        })
    else
        db.chartpointgroups.update({
            _id: chartPointGroupId
        }, {
            $push:{
                tiers: chartPoint
            }
        })
}

/**
 * Tracks state of all the boards/projects for chart modeling. Regularly called
 * from the backend in order to track usage and gather statistics
 */
trackBoardsState = function(){
    db.dashboardmodels.find().forEach(function(board){
        board.workflows.forEach(function(workflow){
            var chartPointGroup = getTodayChartPointGroup(workflow._id)
            board.tiers.forEach(function(tier){
                storeTierStatistics(chartPointGroup._id, tier,
                    board.tasks.filter(function(task) {
                        return task.tierId.toString() == tier._id.toString()
                    }).length
                    )
            })
        })
    })
}

/**
 * Gets "done" tier of the specifier board
 * @param workflowId workflow identifier
 * @return "done" tier
 */
getDoneTier = function(workflowId){
    var doneTier
    db.dashboardmodels.findOne({
        'workflows._id': workflowId
    }, {
        tiers: true
    }).tiers.forEach(function(tier){
        if(tier.order == 0){
            doneTier = tier
            return
        }
    })
    return doneTier
}

/**
 * Gets count of done and not finished tasks assigned to the project
 * at the specified date
 * @param workflowId identifier of the project
 * @param date count of tasks is calcualted for the specified date
 * @return count of done and not finished tasks assigned to the project
 * at the specified date. Format: {notdoneCount: <number>, doneCount: <number>}
 */
getTaskCount = function(workflowId, date){
    var doneTierId = getDoneTier(workflowId)._id
    var notdoneCount = 0
    var doneCount = 0
    var chartpointgroup = db.chartpointgroups.findOne({
        date: new RegExp(date.substring(0, 10)),
        workflowId: workflowId
    })
    if(chartpointgroup){
        chartpointgroup.tiers.forEach(function(tier){
            if(tier.tierId.toString() == doneTierId.toString())
                doneCount = tier.count
            notdoneCount += tier.count
        })
    }
    return {
        notdoneCount: notdoneCount,
        doneCount: doneCount
    }
}

/**
 * Calculates lead time for the given date
 * @param workflowId irdentifier of the project
 * @param date lead time will be calculated for the specified date
 */
calculateLeadTime = function(workflowId, date){
    var count = getTaskCount(workflowId, date)
    var doneCount = count.doneCount
    var leadDate = convertToJSDate(date)
    while(count.notdoneCount > doneCount){
        leadDate.setDate(leadDate.getDate() - 1)
        count = getTaskCount(workflowId, toISO(leadDate))
    }
    return (convertToJSDate(date).getTime() - leadDate.getTime())/(1000*60*60*24)
}

/**
 * Removes all tasks assigned to the specified container type
 * @param collectionType task/tier/board or any other container type
 * @param containerId container identifier
 */
removeTasksFromContainer = function(containerId, collectionType) {
    var containerRefId = collectionType.substring(0, collectionType.length - 1) + "Id"
    var query = new Object()
    query[collectionType + '._id'] = ObjectId(containerId)
    db.dashboardmodels.find(query).forEach(
        function(board){
            for(var i=(board.tasks.length - 1);i>=0;i--){
                if(board.tasks[i][containerRefId].toString() == containerId){
                    board.tasks.splice(i, 1);
                }
            }
            db.dashboardmodels.save(board);
        })
}