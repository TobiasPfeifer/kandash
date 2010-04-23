/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vasilrem.kandash.runtime

import com.vasilrem.kandash.mongo._
import scala.actors.{Actor,Exit,TIMEOUT}
import Actor._

/**
 * Persists state (count of tasks per tier in project) of all boards in the
 * database for chart modeling
 */
trait BoardStatePersister {
  
  case class TrackBoardsState
  val prepared: PreparedFunction
  val timeout: Int

  val boardsStatePersister = actor { loop {
      receiveWithin(3000) {
        case TrackBoardsState => prepared.call("trackBoardsState()")
        case TIMEOUT => 
        case Exit => exit
      }
    } }

  val scheduler = Scheduler.schedule({ boardsStatePersister ! TrackBoardsState }, timeout)

}
