/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vasilrem.kandash.runtime

import scala.actors.{Exit,TIMEOUT}
import scala.actors.Actor._

/**
 * Simple scheduler that touches the actor over a specified amount of time
 */
object Scheduler {
  def schedule(f: => Unit, time: Long) = new AnyRef {
    private val a = actor { loop }
    private def loop: Unit = reactWithin(time) {
      case TIMEOUT => f; loop
      case Exit =>
    }

    def stop() { a ! Exit }
  }
}


