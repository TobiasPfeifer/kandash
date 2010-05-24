/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vasilrem.kandash.resources

import javax.ws.rs._
import javax.ws.rs.core._
import com.vasilrem.kandash.service._
import com.vasilrem.kandash.actors.KandashActors
import com.vasilrem.kandash.model._
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import net.liftweb.json._
import java.util.Date
import net.liftweb.json.Serialization.{read, write, formats}

/**
 * REST-endpoint to work with report models
 */
@Path("/reportmodel")
class ReportModelResource {

  val reportingService = KandashActors.reportingActor

  val log = LogFactory.getLog(this.getClass)

  /**
   * Type hint for seria  lization/deserialization
   */
  implicit val formats = Serialization.formats(NoTypeHints)

  /**
   * Gets report model for specifier tier
   * @val boardId identifier of the board tasks are related to
   * @val query filter query
   * @return report model
   */
  @GET @Path("/{boardId}/{query}")
  @Produces(Array("text/json"))
  def getReportModel(@PathParam("boardId") boardId:String,
                     @PathParam("query") query:String): String = {
    log.info("Getting report model for board " + boardId +
             " with query " + query)
    Serialization.write(
      (reportingService !! GetReportModel(boardId,query))
      .get.asInstanceOf[ReportModel])
  }

}
