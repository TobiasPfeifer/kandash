package com.vasilrem.ideabox

import org.specs._
import java.util.Date
import com.eltimn.scamongo._;
import com.vasilrem.kandash.model._;
import net.liftweb.json.JsonDSL._
import com.mongodb.ObjectId
import com.vasilrem.kandash.service._

class IdeaServiceSpecTest extends SpecificationWithJUnit {

  val kandashService = new KandashService{
    val host = "localhost"
    val port = 27017
    val database = "kandash_test"
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

  def sampleTask =

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
            kandashService.add[Task](boardId,
                                     new Task(null,
                                              new Some("unknown"),
                                              "Test Task",
                                              new Some(5),
                                              new Some("unknown"),
                                              50,
                                              50,
                                              new Some(100),
                                              1,
                                              dashboard.tiers.last._id,
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
    val taskId = kandashService.add[Task](boardId,
                                          new Task(null,
                                                   new Some("unknown"),
                                                   "Test Task",
                                                   new Some(5),
                                                   new Some("unknown"),
                                                   50,
                                                   50,
                                                   new Some(100),
                                                   1,
                                                   dashboard.tiers.last._id,
                                                   dashboard.workflows.last._id))
    val updatedTask =  new Task(taskId,
                                new Some("unknown"),
                                "Updated task!!!",
                                new Some(5),
                                new Some("unknown"),
                                50,
                                50,
                                new Some(100),
                                1,
                                dashboard.tiers.last._id,
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
                                          new Task(null,
                                                   new Some("unknown"),
                                                   "Test Task",
                                                   new Some(5),
                                                   new Some("unknown"),
                                                   50,
                                                   50,
                                                   new Some(100),
                                                   1,
                                                   dashboard.tiers.last._id,
                                                   dashboard.workflows.last._id))
    println("Removing task " + taskId + " from " + boardId)
    kandashService.remove(taskId, Task.collectionName)    
    val resultingBoard = kandashService.getDashboardById(boardId)    
    kandashService.getDashboardById(boardId).tasks.length must beEqualTo(0)

  }

}

