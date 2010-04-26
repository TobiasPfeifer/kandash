package com.vasilrem.kandash.service

import org.specs._
import java.util.Date
import com.eltimn.scamongo._;
import com.vasilrem.kandash.model._;
import net.liftweb.json.JsonDSL._
import com.mongodb.ObjectId
import com.vasilrem.kandash.service._
import com.vasilrem.kandash.mongo._

class KandashServiceSpecTest extends SpecificationWithJUnit {

  val prepared = new PreparedFunction{
    val host = "localhost"
    val port = 27017
    val database = "kandash_test"
  }

  val kandashService = new KandashService{
    val host = "localhost"
    val port = 27017
    val database = "kandash_test"
    val preparedFunction = prepared
  }

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

  doBeforeSpec{
    prepared.loadPreparedFunctions(List("/mongo/preparedFunctions.js"))
  }

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
      println("Comparing " + fieldName + " : " + field1.get(value1) + " vs " + field2.get(value2))
      field1.get(value1) must beEqualTo(field2.get(value2))
    }
  }

  "Create new board" in{
    println("\r\n\r\n=====Create new board======")
    val validID = kandashService.createNewDashboard("testDashboard")
    println("""New dashboard "testDashboard" has been created with ID """ + validID)
    validID must notBeNull
  }

  "Fatch board by ID" in{
    println("\r\n\r\n=====Fatch board by ID======")
    val validID = kandashService.createNewDashboard("testDashboard")
    val validDashboard = kandashService.getDashboardById(validID)
    println("Request for dashboard with id " + validID + " returned " + validDashboard)
    validDashboard must notBeNull
  }

  "Add new project to the board" in{
    println("\r\n\r\n=====Add new project to the board======")
    val boardId = kandashService.createNewDashboard("testDashboard")
    println("Updating dashboard " + boardId)
    val projectId = kandashService.add[Workflow](boardId, new Workflow(null, "testProject1"))
    println("Added project " + projectId)
    kandashService.getDashboardById(boardId).workflows.length must beEqualTo(2)
  }

  "Add new tier to the board" in{
    println("\r\n\r\n=====Add new tier to the board======")
    val boardId = kandashService.createNewDashboard("testDashboard")
    println("Adding tier to board " + boardId)
    println("Added new tier " + kandashService.add[Tier](boardId, new Tier(null, "Test Tier", 2, new Some(2))))
    kandashService.getDashboardById(boardId).tiers.length must beEqualTo(4)
  }

  "Add new task to the board" in{
    println("\r\n\r\n=====Add new task to the board======")
    val boardId = kandashService.createNewDashboard("testDashboard")    
    val dashboard = kandashService.getDashboardById(boardId)
    println("Adding tasks to " + dashboard)
    println("Added new task " +
            kandashService.add[Task](boardId, DummyTask(null, dashboard.tiers.last._id,
                                                        dashboard.workflows.last._id)))
    val resultingDashboard = kandashService.getDashboardById(boardId)
    println("Resulting dashboard: " + resultingDashboard)
    resultingDashboard.tasks.length must beEqualTo(1)
  }

  "Update tier on the board" in {
    println("\r\n\r\n=====Update tier on the board======")
    val boardId = kandashService.createNewDashboard("testDashboard")
    val dashboard = kandashService.getDashboardById(boardId)
    val tier = dashboard.tiers.last
    val updatedTier = new Tier(tier._id, "updated!", tier.order, tier.wipLimit)
    println("Updating " + updatedTier + " at " + dashboard)
    kandashService.update[Tier](updatedTier)
    val resultingBoard = kandashService.getDashboardById(boardId)
    println("Resulting board: " + resultingBoard)
    resultingBoard.tiers.foreach {
      tier =>
      if(tier._id == updatedTier._id){
        deepCompare(tier, updatedTier)
      }
    }
  }

  "Update project on the board" in {
    println("\r\n\r\n=====Update project on the board======")
    val boardId = kandashService.createNewDashboard("testDashboard")
    val dashboard = kandashService.getDashboardById(boardId)
    val workflow = dashboard.workflows.last
    val updatedWorkflow = new Workflow(workflow._id, "updated!")
    println("Updating " + updatedWorkflow + " at " + dashboard)
    kandashService.update[Workflow](updatedWorkflow)
    val resultingBoard = kandashService.getDashboardById(boardId)
    println("Resulting board: " + resultingBoard)
    resultingBoard.workflows.foreach {
      workflow =>
      if(workflow._id == updatedWorkflow._id){
        deepCompare(workflow, updatedWorkflow)
      }
    }
  }

  "Update task on the board" in {
    println("\r\n\r\n=====Update task on the board======")
    val boardId = kandashService.createNewDashboard("testDashboard")
    val dashboard = kandashService.getDashboardById(boardId)
    val taskId = kandashService.add[Task](boardId, DummyTask(null, dashboard.tiers.last._id,
                                                             dashboard.workflows.last._id))
    val updatedTask =  NamedDummyTask(taskId, "Updated!!!", dashboard.tiers.last._id,
                                      dashboard.workflows.last._id)
    println("Updating " + updatedTask + " at " + dashboard)
    kandashService.update[Task](updatedTask)
    val resultingBoard = kandashService.getDashboardById(boardId)
    println("Resulting board: " + resultingBoard)
    resultingBoard.tasks.foreach {
      task =>
      if(task._id == updatedTask._id){
        deepCompare(task, updatedTask)
      }
    }
  }

  "Remove tier from the board" in {
    println("\r\n\r\n=====Remove tier from the board======")
    val boardId = kandashService.createNewDashboard("testDashboard")
    val dashboard = kandashService.getDashboardById(boardId)
    val tierId = dashboard.tiers.last._id
    println("Removing tier " + tierId + " from " + boardId)
    kandashService.remove(tierId, Tier.collectionName)    
    val resultingBoard = kandashService.getDashboardById(boardId)
    kandashService.getDashboardById(boardId).tiers.length must beEqualTo(2)
  }

  "Remove project from the board" in {
    println("\r\n\r\n=====Remove project from the board======")
    val boardId = kandashService.createNewDashboard("testDashboard")
    val dashboard = kandashService.getDashboardById(boardId)
    val workflowId = dashboard.workflows.last._id
    println("Removing project " + workflowId + " from " + boardId)
    kandashService.remove(workflowId, Workflow.collectionName)    
    val resultingBoard = kandashService.getDashboardById(boardId)
    kandashService.getDashboardById(boardId).workflows.length must beEqualTo(0)
  }

  "Remove task on the board" in {
    println("\r\n\r\n=====Remove task on the board======")
    val boardId = kandashService.createNewDashboard("testDashboard")
    val dashboard = kandashService.getDashboardById(boardId)
    val taskId = kandashService.add[Task](boardId,
                                          DummyTask(null, dashboard.tiers.last._id,
                                                    dashboard.workflows.last._id))
    println("Removing task " + taskId + " from " + boardId)
    kandashService.remove(taskId, Task.collectionName)    
    val resultingBoard = kandashService.getDashboardById(boardId)    
    kandashService.getDashboardById(boardId).tasks.length must beEqualTo(0)

  }

  "Request for boards list" should {
    "retun more than 1 board" in{
      println("\r\n\r\n=====Request for boards list======")
      val boardsCount = kandashService.getDashboards.length
      println("Boards count: " + boardsCount)
      boardsCount>0 must beEqualTo(true)
    }
  }

  "Evaluated script" should {
    println("\r\n\r\n=====Evaluated script======")
    val boardId = kandashService.createNewDashboard("testDashboard")
    println("Updating board " + boardId)   
    "affect persisted board model (increse order count of some tiers)" in {
      val originalDashboard = kandashService.getDashboardById(boardId)
      println("\r\n\r\n=====affect persisted board model (increse order count of some tiers)======")
      kandashService.incTiersOrder(boardId, 0)
      val updatedDashboard = kandashService.getDashboardById(boardId)
      updatedDashboard.tiers.filter(_.order > 0).foreach({
          tier=>
          val index = updatedDashboard.tiers.indexOf(tier)
          tier.order must beEqualTo(originalDashboard.tiers(index).order + 1)
        })
    }
    "affect persisted board model (decrease order count of some tiers)" in {
      val originalDashboard = kandashService.getDashboardById(boardId)
      println("\r\n\r\n=====affect persisted board model (decrease order count of some tiers)======")
      kandashService.decTiersOrder(boardId, 0)
      val updatedDashboard = kandashService.getDashboardById(boardId)
      updatedDashboard.tiers.filter(_.order > 0).foreach({
          tier=>
          val index = updatedDashboard.tiers.indexOf(tier)
          tier.order must beEqualTo(originalDashboard.tiers(index).order - 1)
        })
    }
  }

  "Remove all tasks related to the tier" in {
    println("\r\n\r\n=====Remove all tasks related to the tier======")
    val boardId = kandashService.createNewDashboard("testDashboard")
    val dashboard = kandashService.getDashboardById(boardId)
    println("Adding tasks to " + dashboard)
    println("Added new task " +
            kandashService.add[Task](boardId,
                                     DummyTask(null, dashboard.tiers.last._id,
                                               dashboard.workflows.last._id)))
    println("Added new task " +
            kandashService.add[Task](boardId,
                                     DummyTask(null, dashboard.tiers(1)._id,
                                               dashboard.workflows.last._id)))
    println("Removing tasks from " + dashboard.tiers.last._id + "(" + Tier.collectionName + ")")
    kandashService.removeTasksFromContainer(dashboard.tiers.last._id, Tier.collectionName)
    kandashService.getDashboardById(boardId).tasks.length must beEqualTo(1)
  }

  "Remove all tasks related to the project" in {
    println("\r\n\r\n=====Remove all tasks related to the project======")
    val boardId = kandashService.createNewDashboard("testDashboard")
    val dashboard = kandashService.getDashboardById(boardId)
    println("Adding tasks to " + dashboard)
    println("Added new task " +
            kandashService.add[Task](boardId,
                                     DummyTask(null, dashboard.tiers.last._id,
                                               dashboard.workflows.last._id)))
    println("Added new task " +
            kandashService.add[Task](boardId,
                                     DummyTask(null, dashboard.tiers(1)._id,
                                               dashboard.workflows.last._id)))
    println("Removing tasks from " + dashboard.workflows.last._id + "(" + Workflow.collectionName + ")")
    kandashService.removeTasksFromContainer(dashboard.workflows.last._id, Workflow.collectionName)
    kandashService.getDashboardById(boardId).tasks.length must beEqualTo(0)
  }

  "Change tier order" in {
    println("\r\n\r\n=====Change tier order======")
    val boardId = kandashService.createNewDashboard("testDashboard")
    val dashboard = kandashService.getDashboardById(boardId)
    println("Updating board " + boardId +
            ", task ID " + dashboard.tiers.last._id +
            ", task order " + dashboard.tiers.last.order +
            ", setting order " + 2
    )
    kandashService.changeTierOrder(dashboard.tiers.last._id, 2)
    val updatedDashboard = kandashService.getDashboardById(boardId)
    updatedDashboard.tiers.last.order must beEqualTo(2)
    updatedDashboard.tiers.first.order must beEqualTo(0)
  }

  "Changed fact's tier" should{
    "create an appropriate fact" in{
      println("\r\n\r\n=====Changed fact's tier======")
      val boardId = kandashService.createNewDashboard("testDashboard")
      val dashboard = kandashService.getDashboardById(boardId)
      val taskId = kandashService.add[Task](boardId, DummyTask(null, dashboard.tiers.last._id,
                                                               dashboard.workflows.last._id))
      println("Setting tier " + dashboard.tiers.first._id)
      kandashService.createFact(DummyTask(taskId, dashboard.tiers.last._id,
                                          dashboard.workflows.last._id)) must beFalse
      kandashService.createFact(DummyTask(taskId, dashboard.tiers.first._id,
                                          dashboard.workflows.last._id)) must beTrue
    }
  }

}

