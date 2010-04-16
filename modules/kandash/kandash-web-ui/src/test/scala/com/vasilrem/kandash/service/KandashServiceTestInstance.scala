/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vasilrem.kandash.service

import com.vasilrem.kandash.mongo._

/**
 * Instance of Kandash service
 */
object KandashServiceTestInstance extends KandashService{
  val host = "localhost"
  val port = 27017
  val database = "kandash_test"
  val preparedFunction = new PreparedFunction{
    val host = "localhost"
    val port = 27017
    val database = "kandash_test"
  }

  preparedFunction.loadPreparedFunctions(List("/mongo/preparedFunctions.js"))
}


