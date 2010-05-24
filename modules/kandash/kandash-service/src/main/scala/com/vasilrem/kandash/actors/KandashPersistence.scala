/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vasilrem.kandash.actors

import se.scalablesolutions.akka.actor._
import com.vasilrem.kandash.actors._
import se.scalablesolutions.akka.actor.Actor
import se.scalablesolutions.akka.actor.Actor._
import net.liftweb.mongodb._
import com.vasilrem.kandash.model._;
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST._
import net.liftweb.json._
import com.mongodb._
import com.vasilrem.kandash.mongo._
import se.scalablesolutions.akka.util.Logging

sealed trait PersistenceEvent

case class CreateNewDashboard(name:String) extends PersistenceEvent
case class Add[A](boardId: String, document: MongoDocument[A]) extends PersistenceEvent
case class AddTask(boardId: String, task: Task) extends PersistenceEvent
case class AddTier(boardId: String, tier: Tier) extends PersistenceEvent
case class Update[A](document: MongoDocument[A]) extends PersistenceEvent
case class UpdateTask(task:Task) extends PersistenceEvent
case class UpdateTier(tier: Tier) extends PersistenceEvent
case class Remove(documentId: String, collectionName: String) extends PersistenceEvent
case class RemoveTier(tierId: String) extends PersistenceEvent
case class RemoveProject(projectId: String) extends PersistenceEvent
case class RemoveDashboard(boardId: String) extends PersistenceEvent

import se.scalablesolutions.akka.patterns._

class KandashPersistence extends Actor with JObjectBuilder with KandashPersistenceUtil with Logging{

  lazy val usageTrackingActor = KandashActors.usageTrackingActor
  lazy val preparedFunction = new PreparedFunction

  /**
   * Decreases or increases order number of the board tiers, starting from the tier with
   * the given order number
   * @param boardId identifier of the board the tiers should be updated at
   * @param startingFromOrder tiers with the order number bigger then specified
   * will have decreased or increased (by 1) order number
   * @param isInc if true, order number will be increased
   */
  def updateTiersOrder(boardId: String, startingFromOrder: Int, isInc: Boolean) = {
    val incrementor = if(isInc) 1 else -1
    preparedFunction.call("updateTiersOrder('" + boardId + "', " + startingFromOrder + ", " + incrementor + ")")
  }

  /**
   * Increases order number of the board tiers, starting from the tier with
   * the given order number
   * @param boardId identifier of the board the tiers should be updated at
   * @param startingFromOrder tiers with the order number bigger then specified
   * will have increased (by 1) order number
   */
  private def incTiersOrder(boardId: String, startingFromOrder: Int) =
    updateTiersOrder(boardId, startingFromOrder, true)

  /**
   * Decreases order number of the board tiers, starting from the tier with
   * the given order number
   * @param boardId identifier of the board the tiers should be updated at
   * @param startingFromOrder tiers with the order number bigger then specified
   * will have decreased (by 1) order number
   */
  private def decTiersOrder(boardId: String, startingFromOrder: Int) =
    updateTiersOrder(boardId, startingFromOrder, false)

  /**
   * Decreases order number of the board tiers, starting from the specified tier
   * @param tier identifier of the tier order decrease should start from
   */
  private def decTiersOrder(tierId: String) = {
    val board = DashboardModel.find((Tier.collectionName + "._id" -> tierId)).get
    val tierOrder = board.tiers.find(_._id == tierId).get.order
    updateTiersOrder(board._id, tierOrder, false)
  }

  /**
   * Gets document identifier (value of "_id" field)
   * @val document any element that can present on the board (task, project, tier)
   */
  private def getDocumentIdentifier(document: AnyRef): String = {
    val idField = document.getClass.getDeclaredField("_id")
    idField.setAccessible(true)
    idField.get(document).toString
  }

  /**
   * Removes all tasks assigned to the tier, project or any other container
   * @param containerId container identifier
   * @param collectionType collection type of the container
   */
  def removeTasksFromContainer(containerId: String, collectionType: String) = {
    preparedFunction.call("removeTasksFromContainer('" + containerId + "', '" + collectionType + "')")
  }

  /**
   * Changes order of the tier to the specified value
   * @param tierId identifier of the tier to be updated
   * @param order new tier order value
   */
  def changeTierOrder(tierId: String, order: Int) = {
    preparedFunction.call("changeTierOrder('" + tierId + "', " + order + ")")
  }

  /**
   * Adds a new element to the board
   * @val boardId identifier of the board the element will be added to
   * @val document element to be added
   */
  private def add[A](boardId: String, document: MongoDocument[A]) = {
    val documentId = ObjectId.get.toString
    reply_?(documentId)
    DashboardModel.update(("_id" -> boardId),
                          ("$push" -> (document.meta.collectionName -> buildQuery(documentId, document))
      ))
  }

  /**
   * Updates board element
   * @val document board element
   */
  private def update[A](document: MongoDocument[A]) = {
    val collectionName = document.meta.collectionName
    val documentId = getDocumentIdentifier(document)
    reply_?(documentId)
    DashboardModel.update((collectionName + "._id" -> documentId),
                          ("$set" -> (collectionName + ".$" -> buildQuery(documentId, document))
      ))
  }

  /**
   * Removes element from the board
   * @val documentId element identifier
   * @val collectionName name of the element's collection'
   */
  private def remove(documentId: String, collectionName: String): Unit = {
    val boardId = getBoardIdByCollection(collectionName, documentId)
    // Workaround to remove element from collection. $unset shouldn't be used,
    // because it replaces removed elements with nulls, that cannot be be pull out
    // of the list
    DashboardModel.update((collectionName + "._id" -> documentId),
                          ("$set" -> (collectionName + ".$" -> "null")
      ))
    DashboardModel.update(("_id" -> boardId),
                          ("$pull" -> (collectionName -> "null")
      ))
  }

  /**
   * Removes element from the board
   * @val document element to be removed
   */
  def remove[A](document: MongoDocument[A]): Unit = 
    remove(getDocumentIdentifier(document), document.meta.collectionName)    
  

  def receive = {
    case CreateNewDashboard(name) =>
      val model = DashboardModel(ObjectId.get.toString, name, defaultTiers, defaultWorkflows, List())
      reply_?(model._id)
      model.save     

    case RemoveDashboard(boardId) =>
      DashboardModel.delete(("_id" -> boardId))

    case Add(boardId, document) => add(boardId, document)

    case AddTask(boardId, task) =>
      add[Task](boardId, task)
      usageTrackingActor ! TrackTaskUpdate(boardId, task)

    case AddTier(boardId, tier) =>
      incTiersOrder(boardId, tier.order - 1)
      add[Tier](boardId, tier)
      
    case Update(document) => update(document)

    case UpdateTask(task) =>
      usageTrackingActor ! CreateFact(task)
      update[Task](task)

    case UpdateTier(tier: Tier) =>
      changeTierOrder(tier._id.toString(), tier.order)
      update[Tier](tier)
      
    case Remove(documentId, collectionName) => remove(documentId, collectionName)

    case RemoveTier(tierId) =>
      decTiersOrder(tierId)
      removeTasksFromContainer(tierId, Tier.collectionName)
      remove(tierId, Tier.collectionName)
      
    case RemoveProject(projectId) =>
      removeTasksFromContainer(projectId, Workflow.collectionName)
      remove(projectId, Workflow.collectionName)

    case x: Any => log.error("Unprocessable message " + x + " at KandashPersistence")

  }

}
