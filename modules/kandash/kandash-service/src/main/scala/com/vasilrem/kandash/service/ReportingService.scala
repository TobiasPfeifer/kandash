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
import java.util.Date
import com.vasilrem.kandash.mongo._

trait ReportingService extends JObjectBuilder{

  /** mongo host */
  val host:String
  /** mongo port */
  val port:Int
  /** mongo database name */
  val database:String

  val preparedFunction:PreparedFunction

  /** instantiates new connection to mongo */
  MongoDB.defineDb(DefaultMongoIdentifier, MongoAddress(MongoHost(host, port), database))
   

  /**
   * Gets list of history records related to the tasks of the specified tier
   * @val tierId identifier of the tier tasks are related to
   * @val upperDateBound upper date bound
   * @val lowerDateBound lower date bound
   * @return list of history records
   */
  def getTaskHistoryByTier(tierId: String, upperDateBound:Date, lowerDateBound:Date): List[TaskHistory] = {
    DashboardModel.find(("tiers._id" -> tierId)).get.tasks.filter(_.tierId == tierId).foldLeft(List[TaskHistory]()){
      (list, task)=>
      val history = getTaskHistory(task._id, upperDateBound, lowerDateBound)
      if(history.taskFacts.length > 0) history::list else list
    }
  }

  /**
   * Gets list of history records related to the tasks of the specified tier
   * @val tierId identifier of the tier tasks are related to
   * @return list of history records
   */
  def getTaskHistoryByTier(tierId: String): List[TaskHistory] = {
    DashboardModel.find(("tiers._id" -> tierId)).get.tasks.filter(_.tierId == tierId).foldLeft(List[TaskHistory]()){
      (list, task)=>
      val history = getTaskHistory(task._id)
      if(history.taskFacts.length > 0) history::list else list
    }
  }

  /**
   * Gets history record related to the task
   * @val taskId task identifier
   * @val upperDateBound upper date bound
   * @val lowerDateBound lower date bound
   * @return history record
   */
  def getTaskHistory(taskId: String, upperDateBound:Date, lowerDateBound:Date): TaskHistory = {
    val upperBoundString = DefaultFormats.lossless.dateFormat.format(upperDateBound)
    val lowerBoundString = DefaultFormats.lossless.dateFormat.format(lowerDateBound)    
    Serialization.read[TaskHistory](preparedFunction.call(
        "getTaskHistory('" + taskId + "', '" + upperBoundString + "', '" + lowerBoundString + "')").toString)
  }

  /**
   * Gets history record related to the task
   * @val taskId task identifier
   * @return history record
   */
  def getTaskHistory(taskId: String): TaskHistory = getTaskHistory(taskId, new Date(), new Date(1))

}
