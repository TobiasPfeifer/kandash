/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vasilrem.kandash.mongo

import com.eltimn.scamongo._;
import com.vasilrem.kandash.model._;
import net.liftweb.json.JsonDSL._
import com.mongodb.ObjectId
import com.vasilrem.kandash.service._
import java.util.Date
import com.vasilrem.kandash.mongo._
import java.io._

object NewMain {

  class FileBulkReader(val source: File){
    type In = File
    /**
     * Reads file content as string
     */
    def read = {
      val in = new BufferedInputStream(new FileInputStream(source))
      val numBytes = in.available()
      val bytes = new Array[Byte](numBytes)
      in.read(bytes, 0, numBytes)
      new String(bytes)
    }
  }

  /**
   * @param args the command line arguments
   */
  def main(args: Array[String]): Unit = {
    /*val prepared = new PreparedFunction{
     val host = "localhost"
     val port = 27017
     val database = "kandash_reporting"
     loadPreparedFunctions(List("/mongo/preparedFunctions.js"))
     }

     println(prepared.call("""test('rem')"""))*/

    println(new FileBulkReader(new File("file:\\C:\\kandash-service-1.0-SNAPSHOT.jar!\\mongo\\preparedFunctions.js")).read)
  }

}
