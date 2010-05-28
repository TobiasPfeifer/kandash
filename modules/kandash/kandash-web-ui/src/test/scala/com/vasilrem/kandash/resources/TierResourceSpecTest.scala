/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vasilrem.kandash.resources

import org.specs._
import java.util.Date
import com.vasilrem.kandash.model._;
import net.liftweb.json.JsonDSL._
import com.mongodb.ObjectId
import com.vasilrem.kandash.runtime.TestBoot
import com.vasilrem.kandash.service._
import com.vasilrem.kandash.resources._
import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write, formats}

class TierResourceSpecTest extends SpecificationWithJUnit {

  TestBoot
  /**
   * Type hint for serialization/deserialization
   */
  implicit val formats = Serialization.formats(NoTypeHints)

  val boardResource = new BoardResource
  val tierResource = new TierResource
  def sleep = Thread.sleep(500)
  
  val testTier = new Tier(null, "test-tier", 1, None)
  def updatedTier(tierId: String) = new Tier(tierId, "test-tier-updated", 1, None)

  "Create tier" in {
    val boardId = boardResource.createBoard("test-board")
    sleep
    println("Creating new tier in " + boardId)
    tierResource.createTier(boardId,
                            null,
                            Serialization.write(testTier).getBytes) must notBeNull
  }

  "Update tier" in {
    val boardId = boardResource.createBoard("test-board")
    sleep
    val tierId = tierResource.createTier(boardId,
                                         null,
                                         Serialization.write(testTier).getBytes)
    println("Updating tier " + tierId)
    sleep
    tierResource.updateTier(null,
                            Serialization.write(updatedTier(tierId)).getBytes) must notBeNull
  }

  "Delete tier" in {
    val boardId = boardResource.createBoard("test-board")
    sleep
    val tierId = tierResource.createTier(boardId,
                                         null,
                                         Serialization.write(testTier).getBytes)   
    println("Deleting tier " + tierId + " from " + boardResource.getBoard(boardId))
    //sleep
    tierResource.deleteTier(tierId)
  }

  doAfterSpec{
    TaskUpdateFact.drop
    DashboardModel.drop
    ChartPointGroup.drop
  }

}
