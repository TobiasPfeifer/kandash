/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vasilrem.kandash.resources

import org.specs._
import java.util.Date
import com.vasilrem.kandash.actors.KandashPersistenceUtil
import com.vasilrem.kandash.model._;
import net.liftweb.json.JsonDSL._
import com.mongodb.ObjectId
import com.vasilrem.kandash.runtime.TestBoot
import com.vasilrem.kandash.service._
import com.vasilrem.kandash.resources._
import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write, formats}
import org.atmosphere.cpr._

class TaskResourceSpecTest extends SpecificationWithJUnit with KandashPersistenceUtil{

  TestBoot
  /**
   * Type hint for serialization/deserialization
   */
  implicit val formats = Serialization.formats(NoTypeHints)

  val boardResource = new BoardResource
  val taskResource = new TaskResource

  def sleep = Thread.sleep(500)
  
  def testTask(tierId: String, projectId: String) =  new Task(null,
                                                              new Some("unknown"),
                                                              "Test Task",
                                                              new Some(5),
                                                              new Some("unknown"),
                                                              50,
                                                              50,
                                                              new Some(100),
                                                              1,
                                                              tierId,
                                                              projectId)

  def updatedTask(taskId: String, tierId: String, projectId: String) = new Task(taskId,
                                                                                new Some("unknown"),
                                                                                "Test Task Updated",
                                                                                new Some(5),
                                                                                new Some("unknown"),
                                                                                50,
                                                                                50,
                                                                                new Some(100),
                                                                                1,
                                                                                tierId,
                                                                                projectId)

  "Create task" in {
    val boardId = boardResource.createBoard("test-board")    
    sleep
    val board = getDashboardById(boardId).get
    taskResource.createTask(new DefaultBroadcaster(boardId),
                            null,
                            Serialization.write(testTask(board.tiers.last._id, board.workflows.last._id)).getBytes) must notBeNull
  }

  "Update task" in {
    val boardId = boardResource.createBoard("test-board")
    sleep
    val board = getDashboardById(boardId).get
    val taskId = Serialization.read[Task](taskResource.createTask(new DefaultBroadcaster(boardId),
                                                                  null,
                                                                  Serialization.write(testTask(board.tiers.last._id, board.workflows.last._id)).getBytes).message.toString)._id
    sleep
    taskResource.updateTask(new DefaultBroadcaster(boardId), null,
                            Serialization.write(updatedTask(taskId, board.tiers.last._id, board.workflows.last._id)).getBytes) must notBeNull
  }

  "Delete task" in {
    val boardId = boardResource.createBoard("test-board")
    sleep
    val board = getDashboardById(boardId).get
    val taskId = Serialization.read[Task](taskResource.createTask(new DefaultBroadcaster(boardId),
                                                                  null,
                                                                  Serialization.write(testTask(board.tiers.last._id, board.workflows.last._id)).getBytes).message.toString)._id
    sleep
    taskResource.deleteTask(new DefaultBroadcaster(boardId), taskId)
  }

  doAfterSpec{
    TaskUpdateFact.drop
    DashboardModel.drop
    ChartPointGroup.drop
  }

}
