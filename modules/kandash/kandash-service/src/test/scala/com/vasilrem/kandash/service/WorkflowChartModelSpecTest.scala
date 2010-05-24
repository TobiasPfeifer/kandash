/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vasilrem.kandash.service

import org.specs._
import net.liftweb.mongodb._
import com.vasilrem.kandash.actors._
import com.vasilrem.kandash.model._;
import net.liftweb.json.JsonDSL._
import com.mongodb.ObjectId
import com.vasilrem.kandash.runtime.TestBoot
import com.vasilrem.kandash.service._
import java.util.Date
import com.vasilrem.kandash.mongo._
import java.io._
import java.util.Calendar
import java.text.SimpleDateFormat

class WorkflowChartModelSpecTest extends SpecificationWithJUnit with KandashPersistenceUtil{

  TestBoot
  val reportingActor = KandashActors.reportingActor
  val kandashPersistenceActor = KandashActors.kandashPersistenceActor

  def sleep = Thread.sleep(500)

  doBeforeSpec{
    TaskUpdateFact.drop
    DashboardModel.drop
    ChartPointGroup.drop
    val cal = Calendar.getInstance
    cal.set(Calendar.YEAR, 2010)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    kandashPersistenceActor !! CreateNewDashboard("chart")
    sleep
    val board = getDashboardByName("chart").get
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
                      List(ChartPoint(ObjectId.get.toString, board.tiers(0)._id, board.tiers(0).name, random.nextInt(4)),
                           ChartPoint(ObjectId.get.toString, board.tiers(1)._id, board.tiers(1).name, random.nextInt(4)),
                           ChartPoint(ObjectId.get.toString, board.tiers(2)._id, board.tiers(2).name, backlogCount))).save
      backlogCount += random.nextInt(2)
    }
    println("Dummy board is created")
    println("Dummy facts are created")
  }

  "Monthly chart model for the specified data" should {
    "contain more than one group of chart poins" in{
      val board = getDashboardByName("chart").get
      val chartModel = (reportingActor !! GetMonthlyWorkflowChartModel(board._id, None)).get.asInstanceOf[ChartModel]
      println("Chart model: " + chartModel)
      chartModel.chartGroups.length must beGreaterThan(0)
    }
  }
  
}
