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
import net.liftweb.json.Serialization.{read, write, formats}

/**
 * REST-endpoint to work with chart models
 */
@Path("/chartmodel")
class ChartModelResource {

  val kandashService = KandashActors.kandashPersistenceActor
  val reportingService = KandashActors.reportingActor

  val log = LogFactory.getLog(this.getClass)

  /**
   * Enumeration with chart scales (X-axis)
   */
  object Scale extends Enumeration {
    val day = Value("day")
    val week = Value("week")
    val month = Value("month")
  }

  /**
   * Type hint for seria  lization/deserialization
   */
  implicit val formats = Serialization.formats(NoTypeHints)

  /**
   * Get model of the cumulative flow chart
   * @val boardId board identifier the chart will be built for
   * @val scale chart scale (day/week/month)
   * @val projectId identifier of the project the chart will be built for
   * (for all projects, of not specified)
   * @return chart model
   */
  @GET @Path("/workflow/{boardId}/{scale}/{projectId}")
  @Produces(Array("text/json"))
  def getWorkflowChartModel(@PathParam("boardId") boardId:String,
                            @PathParam("scale") scale:String,
                            @PathParam("projectId") projectId:String): String = {
    log.info("Getting chart model for board " + boardId + 
             " with scale " + scale + " for project " + projectId)
    Serialization.write(
      (reportingService !! GetWorkflowChartModel(boardId,
                                             Scale.valueOf(scale).getOrElse(Scale.month).id,
                                             if(projectId == null) None else Some(projectId)))
      .get.asInstanceOf[ChartModel])
  }

  /**
   * Get model of the cumulative flow chart
   * @val boardId board identifier the chart will be built for
   * @val scale chart scale (day/week/month)
   * @return chart model
   */
  @GET @Path("/workflow/{boardId}/{scale}")
  @Produces(Array("text/json"))
  def getWorkflowChartModel(@PathParam("boardId") boardId:String,
                            @PathParam("scale") scale:String): String =
                              getWorkflowChartModel(boardId, scale, null)

}
