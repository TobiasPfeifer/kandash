/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vasilrem.kandash.resources

import javax.ws.rs._
import javax.ws.rs.core._
import com.vasilrem.kandash.service._
import com.vasilrem.kandash.actors._
import com.vasilrem.kandash.model._
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write, formats}
import org.atmosphere.annotation.{Broadcast, Suspend,Cluster}
import org.atmosphere.util.XSSHtmlFilter
import org.atmosphere.cpr.{Broadcaster, BroadcastFilter}
import org.atmosphere.jersey.Broadcastable
import se.scalablesolutions.akka.actor.Actor
import se.scalablesolutions.akka.actor.Actor._
import se.scalablesolutions.akka.util.Logging

/**
 * REST-endpoint to work with tasks
 */
@Path("/task")
class TaskResource extends Logging{

  lazy val kandashService = KandashActors.kandashPersistenceActor

  /**
   * Type hint for serialization/deserialization
   */
  implicit val formats = Serialization.formats(NoTypeHints)

  @Suspend(resumeOnBroadcast = true)
  @GET @Path("/{boardId}")
  @Produces(Array("text/json"))
  def subscribe(@PathParam("boardId") boardId: Broadcaster): Broadcastable =
    new Broadcastable("", boardId)

  /**
   * Adds new task to the board
   * @val boardId identifier of the board the tier should be added to
   * @val headers HTTP headers
   * @val in HTTP input stream conveterted to array
   * @return task identifier
   */
  @Broadcast(resumeOnBroadcast = true)
  @Produces(Array("text/json"))
  @POST @Path("/{boardId}") 
  def createTask(@PathParam("boardId") boardId:Broadcaster,
                 @Context headers: HttpHeaders, in: Array[Byte]): Broadcastable  = {
    log.info("Create new task")
    val task = Serialization.read[Task](new String(in))
    val taskId = (kandashService !! AddTask(boardId.getID, task))
    .get.asInstanceOf[String]
    new Broadcastable(Serialization.write[Task](task.copy(_id = taskId)), boardId)
  }

  /**
   * Updates task
   * @val headers HTTP headers
   * @val in HTTP input stream conveterted to array
   */
  @Broadcast(resumeOnBroadcast = true)
  @Produces(Array("text/json"))
  @PUT @Path("/{boardId}")
  def updateTask(@PathParam("boardId") boardId: Broadcaster,
                 @Context headers: HttpHeaders,
                 in: Array[Byte]): Broadcastable = {
    spawn {
      log.info("Update task")
      val task = Serialization.read[Task](new String(in))
      kandashService ! UpdateTask(task)
    }
    new Broadcastable(new String(in), boardId)
  }
 
  /**
   * Removes task
   * @val taskId identifier of the task that should be removed
   */
  @Broadcast(resumeOnBroadcast = true)
  @Produces(Array("text/json"))
  @DELETE @Path("/{boardId}/{taskId}")
  def deleteTask(@PathParam("boardId") boardId: Broadcaster,
                 @PathParam("taskId") taskId:String): Broadcastable = {
    log.info("Delete task " + taskId)
    kandashService ! Remove(taskId, Task.collectionName)
    new Broadcastable("{remove:'" + taskId + "'}", boardId)
  }

}
