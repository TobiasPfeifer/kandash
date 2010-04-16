/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vasilrem.kandash.mongo

import org.specs._
import java.util.Date
import com.eltimn.scamongo._;
import com.vasilrem.kandash.model._;
import net.liftweb.json.JsonDSL._
import com.mongodb.ObjectId
import com.vasilrem.kandash.service._
import com.vasilrem.kandash.mongo._

class PreparedFunctionSpecTest extends SpecificationWithJUnit {

  val preparedFunction = new PreparedFunction{
    val host = "localhost"
    val port = 27017
    val database = "kandash_test"
  }

  doBeforeSpec{
    preparedFunction.loadPreparedFunctions(List("/mongo/preparedFunctions.js"))
  }

  "Calls prepared function 'test' with parameter 'prepared'" in {
    println("\r\n\r\n=====Calls prepared function 'test' with parameter 'prepared'======")
    preparedFunction.call("test('prepared')") must beEqualTo("test prepared")
  }

}
