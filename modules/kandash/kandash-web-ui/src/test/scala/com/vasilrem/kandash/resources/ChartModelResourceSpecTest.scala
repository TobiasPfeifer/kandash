/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vasilrem.kandash.resources

import org.specs._
import java.util.Date
import com.eltimn.scamongo._;
import com.vasilrem.kandash.model._;
import net.liftweb.json.JsonDSL._
import com.mongodb.ObjectId
import com.vasilrem.kandash.service._
import java.io._
import java.util.Calendar
import java.text.SimpleDateFormat
import net.liftweb.json.Serialization.{read, write, formats}
import net.liftweb.json._
import com.vasilrem.kandash.resources._

class ChartModelResourceSpecTest extends SpecificationWithJUnit {

  val chartModelResource = new ChartModelResource(KandashServiceTestInstance, ReportingServiceTestInstance)

  /**
   * Type hint for serialization/deserialization
   */
  implicit val formats = Serialization.formats(NoTypeHints)

  doBeforeSpec{
    TaskUpdateFact.drop
    DashboardModel.drop
    ChartPointGroup.drop
    val cal = Calendar.getInstance
    cal.set(Calendar.YEAR, 2010)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    val boardId = KandashServiceTestInstance.createNewDashboard("chart")
    val board = KandashServiceTestInstance.getDashboardById(boardId)
    val projectId = board.workflows.first._id
    val random = new java.util.Random
    var backlogCount = 0
    (1 until 300) foreach{
      i=>
      cal.set(Calendar.DAY_OF_YEAR, i)
      val date = cal.getTime
      ChartPointGroup(ObjectId.get.toString,
                      projectId,
                      random.nextDouble * 20,
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
      val board = KandashServiceTestInstance.getDashboardByName("chart")
      val chartModel = Serialization.read[ChartModel](chartModelResource.getWorkflowChartModel(board._id, "month", null))
      print("Chart model: " + chartModel)
      chartModel.chartGroups.length must beGreaterThan(0)
    }
  }

}


