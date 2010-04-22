/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vasilrem.kandash.service

import org.specs._
import com.eltimn.scamongo._;
import com.vasilrem.kandash.model._;
import net.liftweb.json.JsonDSL._
import com.mongodb.ObjectId
import com.vasilrem.kandash.service._
import java.util.Date
import com.vasilrem.kandash.mongo._
import java.io._
import java.util.Calendar
import java.text.SimpleDateFormat

class WorkflowChartModelSpecTest extends SpecificationWithJUnit {

  val prepared = new PreparedFunction{
    val host = "localhost"
    val port = 27017
    val database = "chart"
  }

  val reportingService = new ReportingService{
    val host = "localhost"
    val port = 27017
    val database = "chart"
    val preparedFunction = prepared
  }

  val kandashService = new KandashService{
    val host = "localhost"
    val port = 27017
    val database = "chart"
    val preparedFunction = prepared
  }

  doBeforeSpec{
    TaskUpdateFact.drop
    DashboardModel.drop
    ChartPointGroup.drop
    prepared.loadPreparedFunctions(List("/mongo/preparedFunctions.js"))
    val cal = Calendar.getInstance
    cal.set(Calendar.YEAR, 2010)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    val boardId = kandashService.createNewDashboard("chart")
    val board = kandashService.getDashboardById(boardId)
    val projectId = board.workflows.first._id
    val random = new java.util.Random
    var backlogCount = 0
    (1 until 300) foreach{
      i=>
      cal.set(Calendar.DAY_OF_YEAR, i)
      val date = cal.getTime
      ChartPointGroup(ObjectId.get.toString,
                      projectId,
                      date,
                      List(ChartPoint(ObjectId.get.toString, board.tiers.apply(0)._id, board.tiers.apply(0).name, random.nextInt(4)),
                           ChartPoint(ObjectId.get.toString, board.tiers.apply(1)._id, board.tiers.apply(1).name, random.nextInt(4)),
                           ChartPoint(ObjectId.get.toString, board.tiers.apply(2)._id, board.tiers.apply(2).name, backlogCount))).save
      backlogCount += random.nextInt(2)
    }
    println("Dummy board is created")
    println("Dummy facts are created")
  }

  "Monthly chart model for the specified data" should {
    "contain more than one group of chart poins" in{
      val board = kandashService.getDashboardByName("chart")
      val chartModel = reportingService.getMonthlyWorkflowChartModel(board._id, None)
      print("Chart model: " + chartModel)
      chartModel.chartGroups.length must beGreaterThan(0)
    }
  }
  
}
