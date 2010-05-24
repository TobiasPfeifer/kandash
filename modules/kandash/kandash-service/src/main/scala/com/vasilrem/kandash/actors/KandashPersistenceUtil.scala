/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vasilrem.kandash.actors

import com.mongodb.ObjectId
import com.vasilrem.kandash.model._;
import com.vasilrem.kandash.mongo.PreparedFunction
import net.liftweb.json.JsonDSL._
import net.liftweb.json.NoTypeHints
import net.liftweb.json.Serialization


trait KandashPersistenceUtil{

  val prepared = new PreparedFunction  

  /** tiers (stages) added to the new board by default */
  def defaultTiers:List[Tier] = List(new Tier(ObjectId.get.toString, "{tier.name.todo}", 2, None),
                                     new Tier(ObjectId.get.toString, "{tier.name.inprogress}", 1, None),
                                     new Tier(ObjectId.get.toString, "{tier.name.done}", 0, None))
  /** workflows (projects) added to the new board by default */
  def defaultWorkflows:List[Workflow] = List(new Workflow(ObjectId.get.toString, "workflow.name.default"))
  
  def createDummyDashboard(id:String):DashboardModel = {
    val model = DashboardModel(id, "dummy", defaultTiers, defaultWorkflows, List())
    model.save
    model
  }
  /**
   * Get board by ID
   * @val id board model indetifier
   * @return board model
   */
  def getDashboardById(id:String):Option[DashboardModel] = DashboardModel.find(id)

  /**
   * Get board by name
   * @val name board name
   * @return board model
   */
  def getDashboardByName(name:String):Option[DashboardModel] = DashboardModel.find("name", name)

  /**
   * Get all dashboards (id+name)
   */
  def getDashboards: List[DashboardModel] = {
    implicit val formats = Serialization.formats(NoTypeHints)
    Serialization.read[DashboardsList](prepared.call(
        "getAllBoards()").toString).list
  }

  def getBoardIdByCollection(collectionName: String, collectionId: String): String ={
    implicit val formats = Serialization.formats(NoTypeHints)
    Serialization.read[DashboardModel](prepared.call(
        "getBoardIdByCollection('" + collectionName + "', '" + collectionId + "')").toString)._id
  }
}