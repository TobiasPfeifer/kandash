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
 * REST-endpoint to work with projects
 */
@Path("/project")
class ProjectResource(kandashService: KandashService) {

  def this() = this(KandashServiceInstance)

  val log = LogFactory.getLog(this.getClass);

  /**
   * Type hint for serialization/deserialization
   */
  implicit val formats = Serialization.formats(NoTypeHints)

  /**
   * Adds new project to the board
   * @val boardId identifier of the board the project should be added to
   * @val headers HTTP headers
   * @val in HTTP input stream conveterted to array
   * @return project identifier
   */
  @POST @Path("/{boardId}") 
  def createProject(@PathParam("boardId") boardId:String,
                    @Context headers: HttpHeaders, in: Array[Byte]): String = {
    log.info("Create new project")
    kandashService.add[Workflow](boardId,
                                         Serialization.read[Workflow](new String(in)))
  }

  /**
   * Updates project
   * @val headers HTTP headers
   * @val in HTTP input stream conveterted to array
   * @return project identifier
   */
  @PUT 
  def updateProject(@Context headers: HttpHeaders, in: Array[Byte]): String = {
    log.info("Update project")
    kandashService.update[Workflow](
      Serialization.read[Workflow](new String(in)))
  }

  /**
   * Removes project
   * @val projectId identifier of the project that should be removed
   */
  @DELETE @Path("/{projectId}")
  def deleteProject(@PathParam("projectId") projectId:String) = {
    log.info("Delete project " + projectId)
    kandashService.removeProject(projectId)
  }

}
