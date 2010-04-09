/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vasilrem.kandash.dummy

import com.vasilrem.kandash.service._

object CreateDummyBoard {

  val kandashService = new KandashService{
    val host = "localhost"
    val port = 27017
    val database = "kandash_test"
  }

  /**
   * @param args the command line arguments
   */
  def main(args: Array[String]): Unit = {
    kandashService.createDummyDashboard("4bbdc76c83071da6edf96825")
  }

}
