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
import net.liftweb.json.JsonAST._
import net.liftweb.json._
import com.mongodb.ObjectId
import com.vasilrem.kandash.service._
import java.io._
import java.util.Calendar
import java.text.SimpleDateFormat
import net.liftweb.json.Serialization.{read, write, formats}
import com.vasilrem.kandash.resources._

class ReportModelResourceSpecTest extends SpecificationWithJUnit {

  val reportModelResource = new ReportModelResource(KandashServiceTestInstance, ReportingServiceTestInstance)

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
    KandashServiceTestInstance.dropAllCollections
    val cal = Calendar.getInstance
    cal.set(Calendar.YEAR, 2010)
    KandashServiceTestInstance.createNewDashboard("reporting")
    val board = KandashServiceTestInstance.getDashboardByName("reporting")
    val boardId = board._id
    val projectId = board.workflows.first._id
    (1 until 300) foreach{
      i=>
      cal.set(Calendar.DAY_OF_YEAR, i)
      val date = cal.getTime
      if(i%2 == 0){
        val task = NewTask(ObjectId.get.toString, projectId, board.tiers.apply(0)._id)
        KandashServiceTestInstance.addTask(boardId, task)
        TaskUpdateFact(ObjectId.get.toString, boardId, task, date).save
      }
      if(i%4 == 0){
        val task = NewTask(ObjectId.get.toString, projectId, board.tiers.apply(2)._id)
        KandashServiceTestInstance.addTask(boardId, task)
        TaskUpdateFact(ObjectId.get.toString, boardId, task, date).save
      }
      if(i%3 == 0){
        val task = NewTask(ObjectId.get.toString, projectId, board.tiers.apply(1)._id)
        KandashServiceTestInstance.addTask(boardId, task)
        TaskUpdateFact(ObjectId.get.toString, boardId, task, date).save
      }
    }
    println("Completed tasks are defined")
    println("Dummy board is created")
  }

  "Non-empty model should be built for correct date interval" in {
    val board = KandashServiceTestInstance.getDashboardByName("reporting")
    def formatDate(date: Date):String = DefaultFormats.lossless.dateFormat.format(date)

    Serialization.read[ReportModel](
      reportModelResource.getReportModel(board._id,
                                         "{updateDate: {$gt: '" + formatDate(new Date(1)) + "', $lt: '" + formatDate(new Date()) + "'}}")).taskHistoryEntries must notBeEmpty
    Serialization.read[ReportModel](
      reportModelResource.getReportModel(board._id,
                                         "{updateDate: {$gt: '" + formatDate(new Date(1)) + "', $lt: '" + formatDate(new Date(1)) + "'}}")).taskHistoryEntries must beEmpty
  }

}


