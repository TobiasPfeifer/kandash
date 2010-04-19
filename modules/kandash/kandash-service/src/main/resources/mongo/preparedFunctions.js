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


/**
 * Gets task history for limited period
 * @param taskIdString task identifier
 * @param upperDateBound upper date bound for filtering
 * @param lowerDateBound upper date bound for filtering
 * @return list of history records
 */
getTaskHistory = function(taskIdString, upperDateBound, lowerDateBound){
    var taskHistory = new Object()
    taskHistory.taskFacts = new Array()
    var taskId = ObjectId(taskIdString)
    var facts = db.taskupdatefacts.find({
        taskId : taskId,
        updateDate:{
            $gt: lowerDateBound,
            $lt: upperDateBound
        }
    }).sort({
        updateDate:1
    })
    taskHistory.dateCreated = db.taskupdatefacts.find({
        taskId : taskId
        }, {
        updateDate:1
        }).sort({
        updateDate:1
        }).limit(1)[0].updateDate
    taskHistory.dateUpdated = db.taskupdatefacts.find({
        taskId : taskId
        }, {
        updateDate:1
        }).sort({
        updateDate:-1
        }).limit(1)[0].updateDate
    var backlogTier
    var doneTier
    var board = db.dashboardmodels.findOne({
        'tasks._id' : taskId
    })
    board.tasks.forEach(function(task){
        if(task._id.toString == taskId.toString){
            taskHistory.task = task
        }
    })
    board.tiers.forEach(function(tier){
        if(tier.order == 0){
            doneTier = tier._id
        }
        if(tier.order == (board.tiers.length - 1)){
            backlogTier = tier._id
        }
    })
    taskHistory.timeActive = 0
    var prevDate
    for(var i=0; i<facts.length(); i++){
        var fact = facts[i]
        taskHistory.taskFacts[i] = fact
        if(prevDate){
            taskHistory.timeActive += (convertToJSDate(fact.updateDate).getTime() - convertToJSDate(prevDate).gteTime())
        }
        if(fact.tierId.toString() != backlogTier && fact.tierId.toString() != doneTier){
            prevDate = fact.updateDate
        }else{
            prevDate = null
        }
    }
    return taskHistory
}

/**
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
 * Adds new point to the chart model
 * @param chartModel chart model
 * @param tierIds board model tiers
 * @param projectId project identifier
 * @param lowerBound lower date filtering bound
 * @param middleBound upper date filtering bound
 **/
addChartPoint = function(chartModel, tierIds, projectId, lowerBound, middleBound){
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
        print('Counting for ' + toISO(lowerBound) + ' - ' + toISO(middleBound) + '. Tier ' + tier.name)
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
 * Builds model for the cumulative flow chart
 * @param boardId board identifier the chart will be built for
 * @param scale chart scale (0=day/1=week/2=month)
 * @param projectId identifier of the project the chart will be built for
 * (for all projects, of not specified)
 * @return chart model
 */
buildChartModel = function(boardId, scale, projectId) {
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
