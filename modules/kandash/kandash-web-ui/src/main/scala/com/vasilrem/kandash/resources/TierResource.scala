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
 * REST-endpoint to work with tiers
 */
@Path("/tier")
class TierResource {

  val log = LogFactory.getLog(this.getClass);

  /**
   * Type hint for serialization/deserialization
   */
  implicit val formats = Serialization.formats(NoTypeHints)

  /**
   * Adds new tier to the board
   * @val boardId identifier of the board the tier should be added to
   * @val headers HTTP headers
   * @val in HTTP input stream conveterted to array
   * @return tier identifier
   */
  @POST @Path("/{boardId}") 
  def createTier(@PathParam("boardId") boardId:String,
                 @Context headers: HttpHeaders, in: Array[Byte]): String = {
    log.info("Create new tier")
    KandashServiceInstance.addTier(boardId, Serialization.read[Tier](new String(in)))
  }

  /**
   * Updates tier
   * @val headers HTTP headers
   * @val in HTTP input stream conveterted to array
   */
  @PUT 
  def updateTier(@Context headers: HttpHeaders, in: Array[Byte]) = {
    log.info("Update tier")
    KandashServiceInstance.updateTier(
      Serialization.read[Tier](new String(in)))
  }

  /**
   * Removes tier
   * @val tierId identifier of the tier that should be removed
   */
  @DELETE @Path("/{tierId}")
  def deleteTier(@PathParam("tierId") tierId:String) = {
    log.info("Delete tier " + tierId)
    KandashServiceInstance.removeTier(tierId)
  }

}
