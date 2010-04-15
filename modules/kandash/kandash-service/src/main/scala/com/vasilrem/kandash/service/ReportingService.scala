/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vasilrem.kandash.service

import com.eltimn.scamongo._;
import com.vasilrem.kandash.model._;
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST._
import net.liftweb.json._
import com.mongodb._

trait ReportingService extends JObjectBuilder{

  /** mongo host */
  val host:String
  /** mongo port */
  val port:Int
  /** mongo database name */
  val database:String

  /** instantiates new connection to mongo */
  MongoDB.defineDb(DefaultMongoIdentifier, MongoAddress(MongoHost(host, port), database))

  /**
   * Gets list of history records related to the tasks of the specified tier
   * @val tierId identifier of the tier tasks are related to
   * @return list of history records
   */
  def getTaskHistoryByTier(tierId: String): List[TaskHistory] = {
    DashboardModel.find(("tiers._id" -> tierId)).get.tasks.filter(_.tierId == tierId).foldLeft(List[TaskHistory]()){
      (list, task)=>
      getTaskHistory(task._id)::list
    }
  }

  /**
   * Gets history record related to the task
   * @val taskId task identifier
   * @return history record
   */
  def getTaskHistory(taskId: String): TaskHistory = {
    var ret:Object = null
    MongoDB.use(DefaultMongoIdentifier) ( db => {
        ret = db.eval("""
            function(){

                function getMillis(date){
                        var d = date.match(/^(\d{4})-(\d{2})-(\d{2})[T ](\d{2}):(\d{2}):(\d{2}(?:\.\d+)?)(Z|(([+-])(\d{2}):(\d{2})))$/i);
                        return new Date(Date.UTC(d[1],d[2]-1,d[3],d[4],d[5],d[6]|0,(d[6]*1000-((d[6]|0)*1000))|0,d[7]) + (d[7].toUpperCase() ==="Z" ? 0 : (d[10]*3600 + d[11]*60) * (d[9]==="-" ? 1000 : -1000))).getTime()
                }

                var taskHistory = new Object()
                taskHistory.taskFacts = new Array()
                var taskId = ObjectId('"""+ taskId +"""')
                var facts = db.taskupdatefacts.find({taskId : taskId}).sort({updateDate:1})
                taskHistory.dateCreated = facts[0].updateDate
                taskHistory.dateUpdated = facts[facts.length()-1].updateDate
                var backlogTier
                var doneTier
                var board = db.dashboardmodels.findOne({'tasks._id' : taskId})
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
        """)
      })
    Serialization.read[TaskHistory](ret.toString)
  }

}
