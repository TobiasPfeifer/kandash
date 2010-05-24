/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vasilrem.kandash.service

import net.liftweb.mongodb._
import com.vasilrem.kandash.model._;
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST._
import net.liftweb.json._
import com.mongodb._
import java.util.Date
import com.vasilrem.kandash.mongo._
import se.scalablesolutions.akka.actor.Actor

sealed trait ReportingEvent

case class GetReportModel(boardId: String, filter: String) extends ReportingEvent
case class GetMonthlyWorkflowChartModel(boardId: String, projectId: Option[String]) extends ReportingEvent
case class GetWeeklyWorkflowChartModel(boardId: String, projectId: Option[String]) extends ReportingEvent
case class GetDailyWorkflowChartModel(boardId: String, projectId: Option[String]) extends ReportingEvent
case class GetWorkflowChartModel(boardId: String, chartType: Int, projectId: Option[String]) extends ReportingEvent

class ReportingService extends Actor with JObjectBuilder{

  lazy val preparedFunction = new PreparedFunction

  def receive = {
    case GetReportModel(boardId, filter) => reply(getReportModel(boardId, filter))
    case GetMonthlyWorkflowChartModel(boardId, projectId) => reply(getMonthlyWorkflowChartModel(boardId, projectId))
    case GetWeeklyWorkflowChartModel(boardId, projectId) => reply(getWeeklyWorkflowChartModel(boardId, projectId))
    case GetDailyWorkflowChartModel(boardId, projectId) => reply(getDailyWorkflowChartModel(boardId, projectId))
    case GetWorkflowChartModel(boardId, chartType, projectId) => reply(getWorkflowChartModel(boardId, chartType, projectId))
  }

  /**
   * Gets model for the report grid
   * @val boardId board identifier
   * @val filter JSON/mongo query
   * @return grid model
   */
  private def getReportModel(boardId: String, filter: String): ReportModel = {
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
  private def getMonthlyWorkflowChartModel(boardId: String, projectId: Option[String]): ChartModel =
    getWorkflowChartModel(boardId, 2, projectId)

  /**
   * Builds weekly chart model for the specified board and project
   * @val boardId identifier of the board the chart model will be built for
   * @val projectId identifier of the project the chart will be built for (for all
   * projects on the board, if not specified)
   * @return chart model
   */
  private def getWeeklyWorkflowChartModel(boardId: String, projectId: Option[String]): ChartModel =
    getWorkflowChartModel(boardId, 1, projectId)

  /**
   * Builds daily chart model for the specified board and project
   * @val boardId identifier of the board the chart model will be built for
   * @val projectId identifier of the project the chart will be built for (for all
   * projects on the board, if not specified)
   * @return chart model
   */
  private def getDailyWorkflowChartModel(boardId: String, projectId: Option[String]): ChartModel =
    getWorkflowChartModel(boardId, 0, projectId)

  /**
   * Builds wrokflow chart model for the specified board, project and scale
   * @val boardId identifier of the board the chart model will be built for
   * @val chartType chart scale (2 - MONTHLY, 1 - WEEKLY, 0 - DAILY)
   * @val projectId identifier of the project the chart will be built for (for all
   * projects on the board, if not specified)
   * @return chart model
   */
  private def getWorkflowChartModel(boardId: String, chartType: Int, projectId: Option[String]): ChartModel = {
    Serialization.read[ChartModel](preparedFunction.call(
        "getWorkflowChartModel('" + boardId + "', " + chartType + ", '" + projectId.getOrElse("") + "')").toString)
  }
  
}
