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
   * Gets model for the report grid
   * @val boardId board identifier
   * @val filter JSON/mongo query
   * @return grid model
   */
  def getReportModel(boardId: String, filter: String): ReportModel = {
    Serialization.read[ReportModel](preparedFunction.call(
        "getReportModel('" + boardId + "', " + filter.replaceAll("\"", "'") + ")").toString)
  }

  /**
   * Builds monthly chart model for the specified board and project
   * @val boardId identifier of the board the chart model will be built for
   * @val projectId identifier of the project the chart will be built for (for all
   * projects on the board, if not specified)
   * @return chart model
   */
  def getMonthlyWorkflowChartModel(boardId: String, projectId: Option[String]): ChartModel =
    getWorkflowChartModel(boardId, 2, projectId)

  /**
   * Builds weekly chart model for the specified board and project
   * @val boardId identifier of the board the chart model will be built for
   * @val projectId identifier of the project the chart will be built for (for all
   * projects on the board, if not specified)
   * @return chart model
   */
  def getWeeklyWorkflowChartModel(boardId: String, projectId: Option[String]): ChartModel =
    getWorkflowChartModel(boardId, 1, projectId)

  /**
   * Builds daily chart model for the specified board and project
   * @val boardId identifier of the board the chart model will be built for
   * @val projectId identifier of the project the chart will be built for (for all
   * projects on the board, if not specified)
   * @return chart model
   */
  def getDailyWorkflowChartModel(boardId: String, projectId: Option[String]): ChartModel =
    getWorkflowChartModel(boardId, 0, projectId)

  /**
   * Builds wrokflow chart model for the specified board, project and scale
   * @val boardId identifier of the board the chart model will be built for
   * @val chartType chart scale (2 - MONTHLY, 1 - WEEKLY, 0 - DAILY)
   * @val projectId identifier of the project the chart will be built for (for all
   * projects on the board, if not specified)
   * @return chart model
   */
  def getWorkflowChartModel(boardId: String, chartType: Int, projectId: Option[String]): ChartModel = {
    Serialization.read[ChartModel](preparedFunction.call(
        "getWorkflowChartModel('" + boardId + "', " + chartType + ", '" + projectId.getOrElse("") + "')").toString)
  }
  
}
