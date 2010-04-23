/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vasilrem.kandash.runtime

import scala.actors.{Actor,Exit,TIMEOUT}
import Actor._
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
