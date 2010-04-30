/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vasilrem.kandash.resources

import javax.ws.rs._
import javax.ws.rs.core._
import com.vasilrem.kandash.service._
import com.vasilrem.kandash.model._
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write, formats}

/**
 * REST-endpoint to work with tasks
 */
@Path("/task")
class TaskResource(kandashService: KandashService) {

  def this() = this(KandashServiceInstance)

  val log = LogFactory.getLog(this.getClass);

  /**
   * Type hint for serialization/deserialization
   */
  implicit val formats = Serialization.formats(NoTypeHints)

  /**
   * Adds new task to the board
   * @val boardId identifier of the board the tier should be added to
   * @val headers HTTP headers
   * @val in HTTP input stream conveterted to array
   * @return task identifier
   */
  @POST @Path("/{boardId}") 
  def createTask(@PathParam("boardId") boardId:String,
                 @Context headers: HttpHeaders, in: Array[Byte]): String = {
    log.info("Create new task")
    kandashService.addTask(boardId,
                           Serialization.read[Task](new String(in)))
  }

  /**
   * Updates task
   * @val headers HTTP headers
   * @val in HTTP input stream conveterted to array
   */
  @PUT 
  def updateTask(@Context headers: HttpHeaders, in: Array[Byte]) = {
    log.info("Update task")
    kandashService.updateTask(
      Serialization.read[Task](new String(in)))
  }

  /**
   * Removes task
   * @val taskId identifier of the task that should be removed
   */
  @DELETE @Path("/{taskId}")
  def deleteTask(@PathParam("taskId") taskId:String) = {
    log.info("Delete task " + taskId)
    kandashService.remove(taskId, Task.collectionName)
  }

}
