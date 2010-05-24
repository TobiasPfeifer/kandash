package com.vasilrem.kandash.service

import org.specs._
import org.specs.matcher._
import net.liftweb.mongodb._
import com.vasilrem.kandash.model._;
import net.liftweb.json.JsonDSL._
import com.vasilrem.kandash.service._
import com.vasilrem.kandash.mongo._
import com.vasilrem.kandash.actors._
import com.vasilrem.kandash.runtime._
import se.scalablesolutions.akka.actor.Actor
import se.scalablesolutions.akka.actor.Actor._
import se.scalablesolutions.akka.actor._

class kandashPersistenceActorSpecTest extends SpecificationWithJUnit with KandashPersistenceUtil{

  TestBoot
  val kandashPersistenceActor = KandashActors.kandashPersistenceActor

  /**
   * Creates new dummy task
   */
  def DummyTask(taskId: String, tierId: String, workflowId: String)
  = NamedDummyTask(taskId, "Dummy Task", tierId, workflowId)

  /**
   * Creates new dummy task
   */
  def NamedDummyTask(taskId: String, taskName: String, tierId: String, workflowId: String)
  = new Task(taskId,
             new Some("unknown"),
             taskName,
             new Some(5),
             new Some("unknown"),
             50,
             50,
             new Some(100),
             1,
             tierId,
             workflowId)

  /**
   * Performs deep comparison of two objects. If value of any field differs for
   * the objects, AssertionException is thrown
   */
  def deepCompare(value1: Object, value2: Object) = {
    value1.getClass.getDeclaredFields.toList.foreach{
      field1 =>
      field1.setAccessible(true)
      val fieldName = field1.getName
      val field2 = value2.getClass.getDeclaredField(fieldName)
      field2.setAccessible(true)
      field1.get(value1) must beEqualTo(field2.get(value2))
    }
  }

  def sleep = Thread.sleep(500)

  "Create new dashboard" in {
    val boardId = kandashPersistenceActor !! CreateNewDashboard("TestBoard")
    sleep
    getDashboardById(boardId.get) must beSome[DashboardModel]
    getDashboardByName("TestBoard") must beSome[DashboardModel]
  }

  "Add new task to the board" in{
    val boardId = kandashPersistenceActor !! CreateNewDashboard("TestBoard")
    sleep
    val board = getDashboardById(boardId.get).get
    kandashPersistenceActor ! AddTask(boardId.get, DummyTask(null, board.tiers.last._id,
                                                             board.workflows.last._id))
    sleep
    val resultingDashboard = getDashboardById(boardId.get).get
    resultingDashboard.tasks.length must beEqualTo(1)
  }

  "Add new project to the board" in{
    val boardId = kandashPersistenceActor !! CreateNewDashboard("testDashboard")
    sleep
    val projectId = kandashPersistenceActor !! Add[Workflow](boardId.get, new Workflow(null, "testProject1"))
    sleep
    getDashboardById(boardId.get).get.workflows.length must beEqualTo(2)
  }

  "Add new tier to the board" in{
    val boardId = kandashPersistenceActor !! CreateNewDashboard("testDashboard")
    sleep
    kandashPersistenceActor ! AddTier(boardId.get, new Tier(null, "Test Tier", 2, new Some(2)))
    sleep
    getDashboardById(boardId.get).get.tiers.length must beEqualTo(4)
  }

  "Update task on the board" in {
    val boardId = kandashPersistenceActor !! CreateNewDashboard("testDashboard")
    sleep
    val dashboard = getDashboardById(boardId.get).get
    val taskId = kandashPersistenceActor !! AddTask(boardId.get, DummyTask(null, dashboard.tiers.last._id,
                                                                           dashboard.workflows.last._id))
    sleep
    val updatedTask = NamedDummyTask(taskId.get, "Updated!!!", dashboard.tiers.last._id,
                                     dashboard.workflows.last._id)
    kandashPersistenceActor ! UpdateTask(updatedTask)
    sleep
    getDashboardById(boardId.get).get.tasks.foreach {
      task =>
      if(task._id == updatedTask._id){
        deepCompare(task, updatedTask)
      }
    }
  }
  
  "Update tier on the board" in {
    val boardId = kandashPersistenceActor !! CreateNewDashboard("testDashboard")
    sleep
    val originalTier = getDashboardById(boardId.get).get.tiers.last
    val updatedTier = new Tier(originalTier._id, "updated!", originalTier.order, originalTier.wipLimit)
    kandashPersistenceActor ! UpdateTier(updatedTier)
    sleep
    getDashboardById(boardId.get).get.tiers.foreach {
      tier =>
      if(tier._id == updatedTier._id){
        deepCompare(tier, updatedTier)
      }
    }
  }
  
  "Update project on the board" in {
    val boardId = kandashPersistenceActor !! CreateNewDashboard("testDashboard")
    sleep
    val updatedWorkflow = new Workflow(getDashboardById(boardId.get)
                                       .get.workflows.last._id,
                                       "updated!")
    kandashPersistenceActor ! Update[Workflow](updatedWorkflow)
    sleep
    getDashboardById(boardId.get).get.workflows.foreach {
      workflow =>
      if(workflow._id == updatedWorkflow._id){
        deepCompare(workflow, updatedWorkflow)
      }
    }
  }
   
  "Remove tier from the board" in {
    val boardId = kandashPersistenceActor !! CreateNewDashboard("testDashboard")
    sleep
    val dashboard = getDashboardById(boardId.get).get
    val tierId = dashboard.tiers.last._id
    kandashPersistenceActor ! RemoveTier(tierId)
    sleep
    getDashboardById(boardId.get).get.tiers.length must beEqualTo(2)
  }
  
  "Remove project from the board" in {
    val boardId = kandashPersistenceActor !! CreateNewDashboard("testDashboard")
    sleep
    kandashPersistenceActor !! RemoveProject(getDashboardById(boardId.get)
                                             .get.workflows.
                                             last._id)
    sleep
    getDashboardById(boardId.get).get.workflows.length must beEqualTo(0)
  }

  "Request for boards list" should {
    "retun more than 1 board" in{
      kandashPersistenceActor !! CreateNewDashboard("testDashboard")
      sleep
      getDashboards.length>0 must beEqualTo(true)
    }
  }
   
  doAfterSpec{
    DashboardModel.drop
    TaskUpdateFact.drop
  }

}

