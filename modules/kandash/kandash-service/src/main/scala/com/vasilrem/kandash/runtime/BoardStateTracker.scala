/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vasilrem.kandash.runtime

import com.vasilrem.kandash.mongo.PreparedFunction
import se.scalablesolutions.akka.actor.Actor._
import se.scalablesolutions.akka.actor.Actor

case class TrackBoardsState

class BoardStateTracker(val trackngTimeout:Long) extends Actor{

  val prepared = new PreparedFunction 

  def receive = {
    case TrackBoardsState =>
      Thread.sleep(trackngTimeout)
      Actor.spawn(prepared.call("trackBoardsState()"))
      self ! TrackBoardsState
    case _ => exit
  }

}
