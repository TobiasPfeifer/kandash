/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vasilrem.kandash.runtime

import com.vasilrem.kandash.service._
import com.vasilrem.kandash.mongo._
import com.vasilrem.kandash.actors._
import se.scalablesolutions.akka.actor.SupervisorFactory
import se.scalablesolutions.akka.config.ScalaConfig._
import se.scalablesolutions.akka.stm.Transaction
import se.scalablesolutions.akka.util.Logging
import se.scalablesolutions.akka.patterns._
import java.util.concurrent.CountDownLatch
import net.liftweb.mongodb._
import com.mongodb._
import Transaction.Global._

object TestBoot extends Logging {

  var latch = new CountDownLatch(2)
  if(latch.getCount == 2) load else latch.await

  def load = {    
    latch.countDown
    log.info("Initializing mongo connection...")
    /** instantiates new connection to mongo */
    MongoDB.defineDb(DefaultMongoIdentifier, MongoAddress(MongoHost("localhost", 27017), "kandash_test"))

    log.info("Initializing PreparedFunction...")
    val preparedFunction = new PreparedFunction

    log.info("Loading PreparedFunction...")
    preparedFunction.loadPreparedFunctions(List("/mongo/preparedFunctions.js"))

    log.info("Initializing test supervisor...")
    val factory = SupervisorFactory(
      SupervisorConfig(
        RestartStrategy(AllForOne, 3, 1000, List(classOf[Exception])),
        new LoadBalancedPool(300, {new KandashPersistence})
        .createListOfSupervizedActors :::
        new LoadBalancedPool(300, {new UsageTracking})
        .createListOfSupervizedActors :::
        new LoadBalancedPool(300, {new ReportingService})
        .createListOfSupervizedActors))

    log.info("Test supervizor initialized.")

    factory.newInstance.start

    log.info("Test supervizor started.")
    latch.countDown
  }

}