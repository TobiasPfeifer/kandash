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
@Path("/boards")
class BoardsResource {

  val log = LogFactory.getLog(this.getClass);

  /**
   * Type hint for serialization/deserialization
   */
  implicit val formats = Serialization.formats(NoTypeHints)

  /**
   * Gets list of all boards (only id's and names are filled)
   * @return JSON array of all boards
   */
  @GET
  @Produces(Array("text/json"))
  def getBoards(): String = {
    log.info("Getting list of boards")
    Serialization.write(KandashServiceInstance.getDashboards)
  }

}
