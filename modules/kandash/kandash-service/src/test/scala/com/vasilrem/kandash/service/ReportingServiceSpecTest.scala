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

  def DummyTask(taskId: String, tierId: String)(implicit projectId: String) =
    new Task(taskId,
             new Some("unknown"),
             "Test Task " + taskId,
             new Some(5),
             new Some("unknown"),
             50,
             50,
             new Some(100),
             1,
             tierId,
             projectId)

  doBeforeSpec{
    kandashService.dropAllCollections
    println("Model is dropped")
    prepared.loadPreparedFunctions(List("/mongo/preparedFunctions.js"))
    val board = kandashService.createDummyDashboard("reporting")
    implicit val projectId = board.workflows.last._id
    println("Backlog tier: " + board.backlogTier._id)
    println("Done tier: " + board.doneTier._id)
    println("In Progress tier: " + board.getTierByOrder(1)._id)
    val taskIdList = (0 until 100).foldLeft(List[String]()){
      (list, i) =>
      kandashService.addTask(board._id,
                             DummyTask("" ,
                                       board.backlogTier._id))::list
    }
    kandashService.getDashboardById("reporting").tasks.length must beEqualTo(100)
    println("Tasks are created")
    (30 until 60) foreach {
      i =>
      kandashService.updateTask(DummyTask(taskIdList.apply(i) ,
                                          board.getTierByOrder(1)._id))
    }
    println("In Progress tasks are defined")
    (61 until 99) foreach {
      i =>
      kandashService.updateTask(DummyTask(taskIdList.apply(i) ,
                                          board.getTierByOrder(1)._id))
      kandashService.updateTask(DummyTask(taskIdList.apply(i) ,
                                          board.doneTier._id))
    }
    println("Completed tasks are defined")
    println("Dummy board is created")
  }

  "Completed task should have 3 related facts (for each changed tier)" in{
    println("\r\n===Completed task should have 3 related facts (for each changed tier)===")
    val board = kandashService.getDashboardById("reporting")
    val res = reportingService.getTaskHistory(board.tasks.apply(1)._id)
    res.taskFacts must notBeEmpty
  }

  "Get tasks from backlog" in {
    println("\r\n===Get tasks from backlog===")
    val board = kandashService.getDashboardById("reporting")
    reportingService.getTaskHistoryByTier(board.backlogTier._id).length must beEqualTo(32)
  }

  "Get completed tasks" in {
    println("\r\n===Get completed tasks===")
    val board = kandashService.getDashboardById("reporting")
    reportingService.getTaskHistoryByTier(board.doneTier._id).length must beEqualTo(38)
  }

  "Get tasks in progress" in {
    println("\r\n===Get completed tasks===")
    val board = kandashService.getDashboardById("reporting")
    reportingService.getTaskHistoryByTier(board.getTierByOrder(1)._id).length must beEqualTo(30)
  }

  "Request for report with future dates should return nothing" in{
    println("\r\n===Request for report with future dates should return nothing===")
    val board = kandashService.getDashboardById("reporting")
    println("Tier ID: " + board.getTierByOrder(1)._id)
    reportingService.getTaskHistoryByTier(board.getTierByOrder(1)._id, new Date(), new Date()).length must beEqualTo(0)
    reportingService.getTaskHistoryByTier(board.getTierByOrder(1)._id, new Date(1), new Date(1)).length must beEqualTo(0)
  }
  
}
