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
getMillis = function(date){
    var d = date.match(/^(\d{4})-(\d{2})-(\d{2})[T ](\d{2}):(\d{2}):(\d{2}(?:\.\d+)?)(Z|(([+-])(\d{2}):(\d{2})))$/i);
    return new Date(Date.UTC(d[1],d[2]-1,d[3],d[4],d[5],d[6]|0,(d[6]*1000-((d[6]|0)*1000))|0,d[7]) + (d[7].toUpperCase() ==="Z" ? 0 : (d[10]*3600 + d[11]*60) * (d[9]==="-" ? 1000 : -1000))).getTime()
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
            taskHistory.timeActive += (getMillis(fact.updateDate) - getMillis(prevDate))
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