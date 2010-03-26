/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vasilrem.kandash.service

import com.eltimn.scamongo._;
import com.vasilrem.kandash.model._;
import net.liftweb.json.JsonDSL._
import com.mongodb.ObjectId

/**
 * Represents high-level server-sede routing of the board
 */
trait KandashService {

  /** mongo host */
  val host:String
  /** mongo port */
  val port:Int
  /** mongo database name */
  val database:String
  /** tiers (stages) added to the new board by default */
  val defaultTiers:List[Tier] = List(new Tier(ObjectId.get.toString, "{tier.name.todo}", 0, None),
                                     new Tier(ObjectId.get.toString, "{tier.name.inprogress}", 1, None),
                                     new Tier(ObjectId.get.toString, "{tier.name.done}", 2, None))
  /** workflows (projects) added to the new board by default */
  val defaultWorkflows:List[Workflow] = List(new Workflow(ObjectId.get.toString, "workflow.name.default"))

  /** instantiates new connection to mongo */
  MongoDB.defineDb(DefaultMongoIdentifier, MongoAddress(MongoHost(host, port), database))

  /**
   * Craete new board model
   * @val name name of the model
   * @return board model identifier
   */
  def createNewDashboard(name:String):String = {
    val model = DashboardModel(ObjectId.get.toString, name, defaultTiers, defaultWorkflows, None)
    model.save
    model._id
  }

  /**
   * Get board by ID
   * @val id board model indetifier
   * @return board model
   */
  def getDashboardById(id:String):DashboardModel = DashboardModel.find(id).get

}
