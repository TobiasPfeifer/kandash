/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vasilrem.kandash.service

import com.eltimn.scamongo._;
import com.vasilrem.kandash.model._;
import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import com.mongodb.ObjectId

/**
 * Represents high-level server-sede routing of the board
 */
trait KandashService extends JObjectBuilder{

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

  /** Serialization into JSON goes w/o any type hints */
  implicit val formats = Serialization.formats(NoTypeHints)

  /**
   * Craete new board model
   * @val name name of the model
   * @return board model identifier
   */
  def createNewDashboard(name:String):String = {
    val model = DashboardModel(ObjectId.get.toString, name, defaultTiers, defaultWorkflows, List())
    model.save
    model._id
  }

  /**
   * Get board by ID
   * @val id board model indetifier
   * @return board model
   */
  def getDashboardById(id:String):DashboardModel = DashboardModel.find(id).get

  /**
   * Get board by name
   * @val name board name
   * @return board model
   */
  def getDashboardByName(name:String):DashboardModel = DashboardModel.find("name", name).get

  /**
   * Adds new project to the board
   * @val boardId identifier of the board the project will be added to
   * @val project project to be added
   * @return project identifier
   */
  def createProject(boardId: String, project: Workflow): String = {
    val projectId = ObjectId.get.toString
    DashboardModel.update(("_id" -> boardId),
                          ("$push" -> ("workflows" -> buildQuery(projectId, project))
      ))
    projectId
  }

  /**
   * Adds new tier to the model
   * @val boardId identifier of the board the tier will be added to
   * @val tier tier to be added to the board
   * @return tier identifier
   */
  def addTier(boardId: String, tier: Tier): String = {
    val tierId = ObjectId.get.toString
    DashboardModel.update(("_id" -> boardId),
                          ("$push" -> ("tiers" -> buildQuery(tierId, tier))
      ))
    tierId
  }

  /**
   * Adds new task to the board
   * @val boardId identifier of the board the tier will be added to
   * @val task task to be added to the board
   * @return task identifier
   */
  def addTask(boardId: String, task: Task): String = {
    val taskId = ObjectId.get.toString
    DashboardModel.update(("_id" -> boardId),
                          ("$push" -> ("tasks" -> buildQuery(taskId, task))
      ))
    taskId
  }

  /**
   * Updates tier of the board
   * @val tier must contain identifier of the tier that should be updated
   * @return identifier of the updated tier
   */
  def updateTier(tier: Tier): String = {
    DashboardModel.update(("tiers._id" -> tier._id),
                          ("$set" -> ("tiers.$" -> buildQuery(tier._id, tier))
      ))
    tier._id
  }

  /**
   * Updates workflow of the board
   * @val workflow must contain identifier of the workflow that should be updated
   * @return identifier of the updated workflow
   */
  def updateWorkflow(workflow: Workflow): String = {
    DashboardModel.update(("workflows._id" -> workflow._id),
                          ("$set" -> ("workflows.$" -> buildQuery(workflow._id, workflow))
      ))
    workflow._id
  }

  /**
   * Updates task of the board
   * @val task must contain identifier of the task that should be updated
   * @return identifier of the updated task
   */
  def updateTask(task: Task): String = {
    DashboardModel.update(("tasks._id" -> task._id),
                          ("$set" -> ("tasks.$" -> buildQuery(task._id, task))
      ))
    task._id
  }


}

