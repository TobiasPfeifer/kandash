/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vasilrem.kandash.resources

import org.specs._
import java.util.Date
import com.eltimn.scamongo._;
import com.vasilrem.kandash.model._;
import net.liftweb.json.JsonDSL._
import com.mongodb.ObjectId
import com.vasilrem.kandash.service._
import com.vasilrem.kandash.resources._
import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write, formats}

class TierResourceSpecTest extends SpecificationWithJUnit {

  /**
   * Type hint for serialization/deserialization
   */
  implicit val formats = Serialization.formats(NoTypeHints)

  val boardResource = new BoardResource()
  val tierResource = new TierResource()

  val boardId = boardResource.createBoard("test-board")
  val testTier = new Tier(null, "test-tier", 1, None)
  def updatedTier(tierId: String) = new Tier(tierId, "test-tier-updated", 1, None)

  "Create tier" in {
    println("Creating new tier in " + boardId)
    tierResource.createTier(boardId,
                            null,
                            Serialization.write(testTier).getBytes) must notBeNull
  }

  "Update tier" in {
    val tierId = tierResource.createTier(boardId,
                                         null,
                                         Serialization.write(testTier).getBytes)
    tierResource.updateTier(null,
                            Serialization.write(updatedTier(tierId)).getBytes) must notBeNull
  }

  "Delete tier" in {
    val tierId = tierResource.createTier(boardId,
                                         null,
                                         Serialization.write(testTier).getBytes)   
    println("Deleting tier " + tierId + " from " + boardResource.getBoard(boardId))
    tierResource.deleteTier(tierId)
  }

}
