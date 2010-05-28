/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vasilrem.kandash.actors

import se.scalablesolutions.akka.actor.Actor._
import se.scalablesolutions.akka.actor.Actor
import net.liftweb.mongodb._
import com.vasilrem.kandash.model._;
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST._
import net.liftweb.json._
import com.mongodb._
import com.vasilrem.kandash.mongo._

sealed trait UsageEvent

case class TrackTaskUpdate(boardId: String, task: Task) extends UsageEvent
case class CreateFact(task: Task) extends UsageEvent
case class DeleteFactsPerTask(taskId: String) extends UsageEvent

class UsageTracking extends Actor {

  def receive = {
    case TrackTaskUpdate(boardId, task) =>
      TaskUpdateFact(ObjectId.get.toString, boardId, task, new java.util.Date).save

    case CreateFact(task) =>
      val dashboard = DashboardModel.find(("tasks._id" -> task._id)).get
      val oldTask = dashboard.tasks.find{oldTask => oldTask._id == task._id}.get
      val tierIsChanged: Boolean = oldTask.tierId != task.tierId
      reply(tierIsChanged)
      if(tierIsChanged) TaskUpdateFact(ObjectId.get.toString, dashboard._id, task, new java.util.Date).save

    case DeleteFactsPerTask(taskId) =>
      TaskUpdateFact.delete(("task._id" -> taskId))

    case x: Any => log.error("Unprocessable message " + x + " at UsageTracking")
  }

}
