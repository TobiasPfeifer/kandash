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

  /**
   * Builds monthly chart model for the specified board and project
   * @val boardId identifier of the board the chart model will be built for
   * @val projectId identifier of the project the chart will be built for (for all
   * projects on the board, if not specified)
   * @return chart model
   */
  def getMonthlyChartModel(boardId: String, projectId: Option[String]): ChartModel =
    getChartModel(boardId, 2, projectId)

  /**
   * Builds weekly chart model for the specified board and project
   * @val boardId identifier of the board the chart model will be built for
   * @val projectId identifier of the project the chart will be built for (for all
   * projects on the board, if not specified)
   * @return chart model
   */
  def getWeeklyChartModel(boardId: String, projectId: Option[String]): ChartModel =
    getChartModel(boardId, 1, projectId)

  /**
   * Builds daily chart model for the specified board and project
   * @val boardId identifier of the board the chart model will be built for
   * @val projectId identifier of the project the chart will be built for (for all
   * projects on the board, if not specified)
   * @return chart model
   */
  def getDailyChartModel(boardId: String, projectId: Option[String]): ChartModel =
    getChartModel(boardId, 0, projectId)

  /**
   * Builds chart model for the specified board, project and scale
   * @val boardId identifier of the board the chart model will be built for
   * @val chartType chart scale (2 - MONTHLY, 1 - WEEKLY, 0 - DAILY)
   * @val projectId identifier of the project the chart will be built for (for all
   * projects on the board, if not specified)
   * @return chart model
   */
  def getChartModel(boardId: String, chartType: Integer, projectId: Option[String]): ChartModel = {
    Serialization.read[ChartModel](preparedFunction.call(
        "buildChartModel('" + boardId + "', " + chartType + ", '" + projectId.getOrElse("") + "')").toString)
  }
  
}
