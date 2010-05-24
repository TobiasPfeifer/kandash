/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vasilrem.kandash.actors

import se.scalablesolutions.akka.patterns._
import com.vasilrem.kandash.service.ReportingService
import se.scalablesolutions.akka.actor._

object KandashActors {

  def usageTrackingActor = ActorRegistry.actorsFor[UsageTracking](classOf[UsageTracking]).head

  def kandashPersistenceActor = ActorRegistry.actorsFor[KandashPersistence](classOf[KandashPersistence]).head

  def reportingActor = ActorRegistry.actorsFor[ReportingService](classOf[ReportingService]).head

}
