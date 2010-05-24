/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vasilrem.kandash.model

import com.vasilrem.kandash.model._
import net.liftweb.mongodb._
import com.vasilrem.kandash.model._;
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST._
import net.liftweb.json._

/**
 * Build Lift-JSON object, that can be passed to $push and other Mongo functions
 * as paramter
 */
trait JObjectBuilder {

  /** Serialization into JSON goes w/o any type hints */
  implicit val formats = Serialization.formats(NoTypeHints)

  /**
   * Builds List-JSON from a plain (non-tree) document
   * @param documentId identifier of the document
   * @param document document to be converted into List-JSON
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
   * @param value scala-typed valued
   */
  def convertValueToJValue(value: Any):JValue = {
    value match {
      case x: String => JString(x)
      case x: Int => JInt(x)
      case x: Long => JInt(x)
      case x: Double => JDouble(x)
      case x: Float => JDouble(x)
      case x: Byte => JInt(BigInt(x))
      case x: BigInt => JInt(x)
      case x: Boolean => JBool(x)
      case x: Short => JInt(BigInt(x))
      case x: java.lang.Integer => JInt(BigInt(x.asInstanceOf[Int]))
      case x: java.lang.Long => JInt(BigInt(x.asInstanceOf[Long]))
      case x: java.lang.Double => JDouble(x.asInstanceOf[Double])
      case x: java.lang.Float => JDouble(x.asInstanceOf[Float])
      case x: java.lang.Byte => JInt(BigInt(x.asInstanceOf[Byte]))
      case x: java.lang.Boolean => JBool(x.asInstanceOf[Boolean])
      case x: java.lang.Short => JInt(BigInt(x.asInstanceOf[Short]))
      case x: java.util.Date => JString(formats.dateFormat.format(x))
      case x: Symbol => JString(x.name)
      case _ => error("not a primitive " + value.asInstanceOf[AnyRef].getClass)
    }
  }

}
