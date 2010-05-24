/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vasilrem.kandash.actors

import se.scalablesolutions.akka.actor.ActorRef
import se.scalablesolutions.akka.config.ScalaConfig._
import se.scalablesolutions.akka.actor.Actor
import se.scalablesolutions.akka.actor.Actor._
import se.scalablesolutions.akka.dispatch.Dispatchers

class LoadBalancedPool(val _poolSize: Int, actorInitializer: => Actor) {

  val actorsPool:List[ActorRef] = {
    val workStealingDispatcher = Dispatchers.newExecutorBasedEventDrivenWorkStealingDispatcher("pooled-dispatcher")
    workStealingDispatcher.withNewThreadPoolWithLinkedBlockingQueueWithCapacity(_poolSize).buildThreadPool
    (0 to _poolSize toList).foldRight(List[ActorRef]()){
      (i, list) =>
      val actor = actorOf(actorInitializer)
      actor.dispatcher_=(workStealingDispatcher)
      actor :: list
    }
  }

  def createListOfSupervizedActors: List[Supervise] = {
    (0 to _poolSize toList).foldRight(List[Supervise]()) {
      (i, list) =>
      Supervise(actorsPool(i), LifeCycle(Permanent)) :: list
    }
  }

}
