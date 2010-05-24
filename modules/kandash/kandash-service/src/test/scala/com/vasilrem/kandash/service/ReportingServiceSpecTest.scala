/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vasilrem.kandash.service

import org.specs._
import net.liftweb.mongodb._
import com.vasilrem.kandash.actors.KandashActors
import com.vasilrem.kandash.actors._
import com.vasilrem.kandash.model._;
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST._
import net.liftweb.json._
import com.mongodb.ObjectId
import com.vasilrem.kandash.runtime.TestBoot
import com.vasilrem.kandash.service._
import java.util.Date
import com.vasilrem.kandash.mongo._
import java.io._
import java.util.Calendar
import java.text.SimpleDateFormat
import se.scalablesolutions.akka.actor.Actor

class reportingActorSpecTest extends SpecificationWithJUnit with KandashPersistenceUtil{

  TestBoot
  val reportingActor = KandashActors.reportingActor
  val kandashPersistenceActor = KandashActors.kandashPersistenceActor

  def sleep = Thread.sleep(500)

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

    TaskUpdateFact.drop
    DashboardModel.drop
    val cal = Calendar.getInstance
    cal.set(Calendar.YEAR, 2010)
    kandashPersistenceActor !! CreateNewDashboard("reporting")
    sleep
    val board = getDashboardByName("reporting").get
    val boardId = board._id
    val projectId = board.workflows.head._id
    val start = (new java.util.Date).getTime
    (1 until 100) foreach{
      i=>
      Thread.sleep(1)
      cal.set(Calendar.DAY_OF_YEAR, i)
      val date = cal.getTime
      if(i%2 == 0){
        val task = NewTask("ReportingTask 0:" + i, projectId, board.tiers(0)._id)
        kandashPersistenceActor ! AddTask(boardId, task)
        //TaskUpdateFact(ObjectId.get.toString, boardId, task, date).save
      }
      if(i%4 == 0){
        val task = NewTask("ReportingTask 2:" + i, projectId, board.tiers(2)._id)
        kandashPersistenceActor ! AddTask(boardId, task)
        //TaskUpdateFact(ObjectId.get.toString, boardId, task, date).save
      }
      if(i%3 == 0){
        val task = NewTask("ReportingTask 1:" + i, projectId, board.tiers(1)._id)
        kandashPersistenceActor ! AddTask(boardId, task)
        //TaskUpdateFact(ObjectId.get.toString, boardId, task, date).save
      }
    }
    println("Total time: " + ((new java.util.Date).getTime - start))
    println("Completed tasks are defined")
    println("Dummy board is created")
  }

  "Non-empty model should be built for correct date interval" in {
    val board = getDashboardByName("reporting").get
    def formatDate(date: Date):String = DefaultFormats.lossless.dateFormat.format(date)

    (reportingActor !! GetReportModel(board._id,
                                      "{updateDate: {$gt: '" + formatDate(new Date(1)) + "', $lt: '" + formatDate(new Date()) + "'}}")).get.asInstanceOf[ReportModel].taskHistoryEntries must notBeEmpty
    (reportingActor !! GetReportModel(board._id,
                                      "{updateDate: {$gt: '" + formatDate(new Date(1)) + "', $lt: '" + formatDate(new Date(1)) + "'}}")).get.asInstanceOf[ReportModel].taskHistoryEntries must beEmpty
  }
  
}
