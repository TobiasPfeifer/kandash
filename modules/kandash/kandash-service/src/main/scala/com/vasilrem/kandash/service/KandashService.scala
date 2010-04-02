/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vasilrem.kandash.service

import com.eltimn.scamongo._;
import com.vasilrem.kandash.model._;
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST._
import net.liftweb.json._
import com.mongodb._

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
   * Adds a new element to the board
   * @val boardId identifier of the board the element will be added to
   * @val document element to be added
   * @return element identifier
   */
  def add[A](boardId: String, document: MongoDocument[A]): String = {
    val documentId = ObjectId.get.toString
    DashboardModel.update(("_id" -> boardId),
                          ("$push" -> (document.meta.collectionName -> buildQuery(documentId, document))
      ))
    documentId
  }

  /**
   * Updates board element
   * @val document board element
   * @return element identifier
   */
  def update[A](document: MongoDocument[A]): String = {
    val collectionName = document.meta.collectionName
    val documentId = getDocumentIdentifier(document)
    DashboardModel.update((collectionName + "._id" -> documentId),
                          ("$set" -> (collectionName + ".$" -> buildQuery(documentId, document))
      ))
    documentId
  }

  /**
   * Removes element from the board
   * @val documentId element identifier
   * @val collectionName name of the element's collection'
   */
  def remove(documentId: String, collectionName: String): Unit = {
    val boardId = DashboardModel.find((collectionName + "._id" -> documentId)).get._id
    println("Element is up to be removed from " + boardId)
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

  /**
   * Gets document identifier (value of "_id" field)
   * @val document any element that can present on the board (task, project, tier)
   */
  def getDocumentIdentifier(document: AnyRef): String = {
    val idField = document.getClass.getDeclaredField("_id")
    idField.setAccessible(true)
    idField.get(document).toString
  }

}

