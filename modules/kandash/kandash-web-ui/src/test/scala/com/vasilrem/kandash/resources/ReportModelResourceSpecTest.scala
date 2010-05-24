/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vasilrem.kandash.resources

import org.specs._
import java.util.Date
import com.vasilrem.kandash.actors._
import com.vasilrem.kandash.model._;
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST._
import net.liftweb.json._
import com.mongodb.ObjectId
import com.vasilrem.kandash.runtime.TestBoot
import com.vasilrem.kandash.service._
import java.io._
import java.util.Calendar
import java.text.SimpleDateFormat
import net.liftweb.json.Serialization.{read, write, formats}
import com.vasilrem.kandash.resources._

class ReportModelResourceSpecTest extends SpecificationWithJUnit  with KandashPersistenceUtil{

  TestBoot
  val reportModelResource = new ReportModelResource
  val kandashService = KandashActors.kandashPersistenceActor

  def sleep = Thread.sleep(500)

  /**
   * Type hint for serialization/deserialization
   */
  implicit val formats = Serialization.formats(NoTypeHints)

  doBeforeSpec{
    def NewTask(taskID: String, workflowId: String, tierId: String) =
      new Task(taskID,
               new Some("unknown"),
               "Test Task",
               new Some(5),
               new Some("unknown"),
               50,
               50,
               new Some(100),
               1,
               tierId,
               workflowId)
    val cal = Calendar.getInstance
    cal.set(Calendar.YEAR, 2010)
    kandashService ! CreateNewDashboard("reporting")
    sleep
    val board = getDashboardByName("reporting").get
    val boardId = board._id
    val projectId = board.workflows.first._id
    (1 until 300) foreach{
      i=>
      Thread.sleep(1)
      cal.set(Calendar.DAY_OF_YEAR, i)
      val date = cal.getTime
      if(i%2 == 0){
        val task = NewTask(ObjectId.get.toString, projectId, board.tiers(0)._id)
        kandashService ! AddTask(boardId, task)
      }
      if(i%4 == 0){
        val task = NewTask(ObjectId.get.toString, projectId, board.tiers(2)._id)
        kandashService ! AddTask(boardId, task)
      }
      if(i%3 == 0){
        val task = NewTask(ObjectId.get.toString, projectId, board.tiers(1)._id)
        kandashService ! AddTask(boardId, task)
      }
    }
    println("Completed tasks are defined")
    println("Dummy board is created")
  }

  "Non-empty model should be built for correct date interval" in {
    val board = getDashboardByName("reporting").get
    def formatDate(date: Date):String = DefaultFormats.lossless.dateFormat.format(date)

    Serialization.read[ReportModel](
      reportModelResource.getReportModel(board._id,
                                         "{updateDate: {$gt: '" + formatDate(new Date(1)) + "', $lt: '" + formatDate(new Date()) + "'}}")).taskHistoryEntries must notBeEmpty
    Serialization.read[ReportModel](
      reportModelResource.getReportModel(board._id,
                                         "{updateDate: {$gt: '" + formatDate(new Date(1)) + "', $lt: '" + formatDate(new Date(1)) + "'}}")).taskHistoryEntries must beEmpty
  }

  doAfterSpec{
    TaskUpdateFact.drop
    DashboardModel.drop
    ChartPointGroup.drop
  }

}


