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
 * REST-endpoint to work with boards
 */
@Path("/board")
class BoardResource {

  val log = LogFactory.getLog(this.getClass);

  /**
   * Type hint for serialization/deserialization
   */
  implicit val formats = Serialization.formats(NoTypeHints)

  /**
   * Creates new board
   * @val boardName board name
   * @return board identifier
   */
  @POST @Path("/{boardName}")
  def createBoard(@PathParam("boardName") boardName:String): String = {
    log.info("Creating new dashboard " + boardName)
    KandashServiceInstance.createNewDashboard(boardName)
  }

  /**
   * Gets board by ID
   * @val boardId board ID
   * @return board as JSON text
   */
  @GET @Path("/{boardId}")
  @Produces(Array("application/json"))
  def getBoard(@PathParam("boardId") boardId:String): String = {
    log.info("Getting board " + boardId)
    Serialization.write(KandashServiceInstance.getDashboardById(boardId))
  }

}
