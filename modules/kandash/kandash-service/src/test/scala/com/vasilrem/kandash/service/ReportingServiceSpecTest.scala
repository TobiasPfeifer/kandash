/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vasilrem.kandash.service

import org.specs._
import com.eltimn.scamongo._;
import com.vasilrem.kandash.model._;
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST._
import net.liftweb.json._
import com.mongodb.ObjectId
import com.vasilrem.kandash.service._
import java.util.Date
import com.vasilrem.kandash.mongo._
import java.io._
import java.util.Calendar
import java.text.SimpleDateFormat

class ReportingServiceSpecTest extends SpecificationWithJUnit {

  val prepared = new PreparedFunction{
    val host = "localhost"
    val port = 27017
    val database = "kandash_reporting"
  }  

  val reportingService = new ReportingService{
    val host = "localhost"
    val port = 27017
    val database = "kandash_reporting"
    val preparedFunction = prepared
  }
  
  val kandashService = new KandashService{
    val host = "localhost"
    val port = 27017
    val database = "kandash_reporting"
    val preparedFunction = prepared
  }

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

    MongoDB.defineDb(DefaultMongoIdentifier, MongoAddress(MongoHost("localhost", 27017), "kandash_reporting"))
    TaskUpdateFact.drop
    DashboardModel.drop
    prepared.loadPreparedFunctions(List("/mongo/preparedFunctions.js"))
    val cal = Calendar.getInstance
    cal.set(Calendar.YEAR, 2010)
    kandashService.createNewDashboard("reporting")
    val board = kandashService.getDashboardByName("reporting")
    val boardId = board._id
    val projectId = board.workflows.first._id
    (1 until 300) foreach{
      i=>
      cal.set(Calendar.DAY_OF_YEAR, i)
      val date = cal.getTime
      if(i%2 == 0){
        val task = NewTask(ObjectId.get.toString, projectId, board.tiers(0)._id)
        kandashService.addTask(boardId, task)
        TaskUpdateFact(ObjectId.get.toString, boardId, task, date).save
      }
      if(i%4 == 0){
        val task = NewTask(ObjectId.get.toString, projectId, board.tiers(2)._id)
        kandashService.addTask(boardId, task)
        TaskUpdateFact(ObjectId.get.toString, boardId, task, date).save
      }
      if(i%3 == 0){
        val task = NewTask(ObjectId.get.toString, projectId, board.tiers(1)._id)
        kandashService.addTask(boardId, task)
        TaskUpdateFact(ObjectId.get.toString, boardId, task, date).save
      }
    }
    println("Completed tasks are defined")
    println("Dummy board is created")
  }

  "Non-empty model should be built for correct date interval" in {
    val board = kandashService.getDashboardByName("reporting")
    def formatDate(date: Date):String = DefaultFormats.lossless.dateFormat.format(date)

    reportingService.getReportModel(board._id,
                                    "{updateDate: {$gt: '" + formatDate(new Date(1)) + "', $lt: '" + formatDate(new Date()) + "'}}").taskHistoryEntries must notBeEmpty
    reportingService.getReportModel(board._id,
                                    "{updateDate: {$gt: '" + formatDate(new Date(1)) + "', $lt: '" + formatDate(new Date(1)) + "'}}").taskHistoryEntries must beEmpty
  }
  
}
