/**
 * Test function to make sure, procedures bundle is loaded
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

getProjectById = function(board, projectId){
    var project
    board.workflows.forEach(function(workflow){
        if(workflow._id.toString() == projectId){
            project = workflow
        }
    })
    return project
}

getTierById = function(board, tierId){
    var tier
    board.tiers.forEach(function(t){
        if(t._id.toString() == tierId.toString()){
            tier = t
        }
    })
    print('tier: ' + tier)
    return tier
}

getTaskCreationDate = function(taskId){
    return db.taskupdatefacts.find({
        'task._id' : taskId
        }, {
        updateDate:1
        }).sort({
        updateDate:1
        }).limit(1)[0].updateDate
}

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
 * Builds basic report model
 * @param boardId
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
 * Sets new order to the tier, and reorder other tiers appropriately
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
 * Gets array of tier identifiers associated with order of the tier on the board
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
 * Adds new point to the history chart model
 * @param chartModel chart model
 * @param tierIds board model tiers
 * @param projectId project identifier
 * @param lowerBound lower date filtering bound
 * @param middleBound upper date filtering bound
 **/
addHistoryChartPoint = function(chartModel, tierIds, projectId, lowerBound, middleBound){
    chartModel.points[chartModel.points.length] = {
        date : toISO(new Date(middleBound))
    }
    chartModel.points[chartModel.points.length - 1].tiers = new Array()
    var tiers = chartModel.points[chartModel.points.length - 1].tiers
    tierIds.forEach(function(tier){
        var tierId = tier._id
        tiers[tiers.length] = {
            tierId: tierId,
            tierName: tier.name
        }
        var searchMask = {
            tierId: tierId,
            updateDate : {
                $gt:toISO(lowerBound),
                $lt:toISO(middleBound)
            }
        }
        if(projectId){
            searchMask.workflowId = ObjectId(projectId)
        }
        tiers[tiers.length - 1].count = db.taskupdatefacts.find(searchMask).length()
    })
}

/**
 * Builds model for the history chart
 * @param boardId board identifier the chart will be built for
 * @param scale chart scale (0=day/1=week/2=month)
 * @param projectId identifier of the project the chart will be built for
 * (for all projects, of not specified)
 * @return chart model
 */
buildHistoryChartModel = function(boardId, scale, projectId) {
    var chartModel = new Object()
    var searchMask = projectId?{
        'workflowId': ObjectId(projectId)
    }:{}

    var board = db.dashboardmodels.findOne({
        _id: ObjectId(boardId)
    }, {
        tiers: true
    })

    var tierIds = board.tiers

    chartModel.lowerBound = convertToJSDate(db.taskupdatefacts.find(searchMask, {
        updateDate: 1
        }).sort({
        updateDate: 1
        }).limit(1)[0].updateDate)
    chartModel.upperBound = new Date()
    chartModel.points = new Array()
    var lowerBound = new Date(chartModel.lowerBound)
    var middleBound = new Date(chartModel.lowerBound)
    while(middleBound < chartModel.upperBound){
        addChartPoint(chartModel, tierIds, projectId, lowerBound, middleBound)
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
    addChartPoint(chartModel, tierIds, projectId, lowerBound, chartModel.upperBound)
    chartModel.lowerBound = toISO(chartModel.lowerBound)
    chartModel.upperBound = toISO(chartModel.upperBound)
    return chartModel
}