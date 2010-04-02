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
import com.mongodb.ObjectId
import com.vasilrem.kandash.service._
import com.vasilrem.kandash.resources._
import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write, formats}

class TaskResourceSpecTest extends SpecificationWithJUnit {

  /**
   * Type hint for serialization/deserialization
   */
  implicit val formats = Serialization.formats(NoTypeHints)

  val boardResource = new BoardResource()
  val taskResource = new TaskResource()

  val boardId = boardResource.createBoard("test-board")
  val testTask =  new Task(null,
                           new Some("unknown"),
                           "Test Task",
                           new Some(5),
                           new Some("unknown"),
                           50,
                           50,
                           new Some(100),
                           1,
                           "dashboard.tiers.last._id",
                           "dashboard.workflows.last._id")
  def updatedTask(taskId: String) = new Task(taskId,
                                             new Some("unknown"),
                                             "Test Task Updated",
                                             new Some(5),
                                             new Some("unknown"),
                                             50,
                                             50,
                                             new Some(100),
                                             1,
                                             "dashboard.tiers.last._id",
                                             "dashboard.workflows.last._id")

  "Create task" in {
    taskResource.createTask(boardId,
                            null,
                            Serialization.write(testTask).getBytes) must notBeNull
  }

  "Update task" in {
    val taskId = taskResource.createTask(boardId,
                                         null,
                                         Serialization.write(testTask).getBytes)
    taskResource.updateTask(null,
                            Serialization.write(updatedTask(taskId)).getBytes) must notBeNull
  }

  "Delete task" in {
    val taskId = taskResource.createTask(boardId,
                                         null,
                                         Serialization.write(testTask).getBytes)
    taskResource.deleteTask(taskId)
  }

}
