/**
 * Copyright (C) 2009-2010 Scalable Solutions AB <http://scalablesolutions.se>
 */

package com.vasilrem.kandash.resources.config

import se.scalablesolutions.akka.remote.BootableRemoteActorService
import se.scalablesolutions.akka.actor.BootableActorLoaderService
import se.scalablesolutions.akka.config.Config
import se.scalablesolutions.akka.util.{Logging, Bootable}
import se.scalablesolutions.akka.servlet._

import javax.servlet.{ServletContextListener, ServletContextEvent}
 
class SimpleAkkaInitializer extends ServletContextListener {
  lazy val loader = new AkkaLoader

  def contextDestroyed(e: ServletContextEvent): Unit =
    loader.shutdown

  def contextInitialized(e: ServletContextEvent): Unit =
    loader.boot(true, new BootableActorLoaderService{})
}