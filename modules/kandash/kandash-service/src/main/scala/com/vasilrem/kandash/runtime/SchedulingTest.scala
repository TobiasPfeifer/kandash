/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vasilrem.kandash.runtime

import com.vasilrem.kandash.mongo._

object SchedulingTest {
  
  /**
   * @param args the command line arguments
   */
  def main(args: Array[String]): Unit = {
    new BoardStatePersister{
      val timeout = 20*1000
      val prepared = new PreparedFunction{
        val host = "localhost"
        val port = 27017
        val database = "kandash"
      }      
    }
  }

}
