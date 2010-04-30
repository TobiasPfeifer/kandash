/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vasilrem.kandash.service

import com.vasilrem.kandash.mongo._
import com.vasilrem.kandash.runtime._

/**
 * Instance of Kandash service
 */
object KandashServiceInstance extends KandashService{
  val host = "localhost"
  val port = 27017
  val database = "kandash"
  val preparedFunction = new PreparedFunction{
    val host = "localhost"
    val port = 27017
    val database = "kandash"
  }

  preparedFunction.loadPreparedFunctions(List("/mongo/preparedFunctions.js"))

  val persister = new BoardStatePersister{
    val timeout = 20*1000
    val prepared = preparedFunction
    println("Started BoardStatePersister!")
  }
}
