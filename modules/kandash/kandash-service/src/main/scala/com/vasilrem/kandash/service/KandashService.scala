/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vasilrem.kandash.service

import com.eltimn.scamongo._;
import com.vasilrem.kandash.model._;
import net.liftweb.json.JsonDSL._
import com.mongodb.ObjectId

trait KandashService {

  val host:String
  val port:Int
  val database:String
  val defaultTiers:List[Tier] = List(new Tier(ObjectId.get.toString, "{tier.name.todo}", 0, None),
                                     new Tier(ObjectId.get.toString, "{tier.name.inprogress}", 1, None),
                                     new Tier(ObjectId.get.toString, "{tier.name.done}", 2, None))
  val defaultWorkflows:List[Workflow] = List(new Workflow(ObjectId.get.toString, "workflow.name.default"))

  MongoDB.defineDb(DefaultMongoIdentifier, MongoAddress(MongoHost(host, port), database))

  def createNewDashboard(name:String):String = {
    val model = DashboardModel(ObjectId.get.toString, name, defaultTiers, defaultWorkflows, None)
    model.save
    model._id
  }

  def getDashboardById(id:String):DashboardModel = DashboardModel.find(id).get

}
