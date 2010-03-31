/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vasilrem.kandash.model

import com.vasilrem.kandash.model._
import com.eltimn.scamongo._
import com.vasilrem.kandash.model._;
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST._
import net.liftweb.json._
import com.mongodb.ObjectId

/**
 * Build Lift-JSON object, that can be passed to $push and other Mongo functions
 * as paramter
 */
trait JObjectBuilder {

  /**
   * Builds List-JSON from a plain (non-tree) document
   * @val documentId identifier of the document
   * @val document document to be converted into List-JSON
   */
  def buildQuery(documentId: String, document: AnyRef): JObject = {
    val query:JObject = ("_id" -> documentId)
    document.getClass.getDeclaredFields.toList.filter(!_.getName.startsWith("_")).foldRight(query) {
      (field: java.lang.reflect.Field, query: JObject) =>
      field.setAccessible(true)
      val fieldName = field.getName
      val fieldValue: JValue = field.get(document) match{
        case Some(option) => convertValueToJValue(option)
        case None => JNothing
        case value => convertValueToJValue(value)
      }
      query ~ (fieldName -> fieldValue)
    }
  }

  /**
   * Converts scala-typed valued to JValues
   * @val value scala-typed valued
   */
  def convertValueToJValue(value: Any):JValue = {
    value match {
      case intValue: Int => new JInt(intValue.asInstanceOf[Int])
      case unknowType => new JString(unknowType.toString)
    }
  }

}
