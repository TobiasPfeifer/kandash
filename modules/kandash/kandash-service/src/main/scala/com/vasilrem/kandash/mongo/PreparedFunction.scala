/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vasilrem.kandash.mongo

import java.io._
import com.eltimn.scamongo._;
import com.vasilrem.kandash.model._;
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST._
import net.liftweb.json._
import com.mongodb._
import java.util.Date

trait PreparedFunction {

  /** mongo host */
  val host:String
  /** mongo port */
  val port:Int
  /** mongo database name */
  val database:String

  /** instantiates new connection to mongo */
  MongoDB.defineDb(DefaultMongoIdentifier, MongoAddress(MongoHost(host, port), database))

  /** Serialization into JSON goes w/o any type hints */
  implicit val formats = Serialization.formats(NoTypeHints)

  /**
   * File reader
   */
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
   * Creates prepared functions from classpath resources in Mongo
   * @val classPathResources list of resource names in classpath
   */
  def loadPreparedFunctions(classPathResources: List[String]) = {
    classPathResources.foreach {
      resourceName =>
      val fileContent = new FileBulkReader(new File(getClass().getResource(resourceName).getFile)).read
      MongoDB.use(DefaultMongoIdentifier) ( db => {
          db.eval(
            fileContent
          )
          ("(.*)=[ ]+function".r findAllIn fileContent).matchData foreach {
            matchedFunction => {
              val functionName = matchedFunction.group(1).trim
              println("Preparing function " + functionName)
              db.eval("db.system.js.save({_id: '" + functionName + "', value: " + functionName + "})")
            }
          }
        }
      )
    }
  }

  /**
   * Calls prepared function with specified list of arguments
   * @val functionCall function call with inlined arguments
   * @return result of function call
   */
  def call(functionCall: String): Object = {
    var ret:Object = null
    println("Calling " + functionCall)
    MongoDB.use(DefaultMongoIdentifier) ( db => {
        ret = db.eval("db.eval(\"" + functionCall + "\")")
      }
    )
    ret
  }


}
