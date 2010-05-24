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

class BoardResourceSpecTest extends SpecificationWithJUnit {

  TestBoot
  val boardResource = new BoardResource

  "Creates new board" in{    
    boardResource.createBoard("tast-board") must notBeNull
  }

  "Gets board" in {
    val boardId = boardResource.createBoard("test-board")
    Thread.sleep(500)
    boardResource.getBoard(boardId) must include("test-board")
  }

  doAfterSpec{
    TaskUpdateFact.drop
    DashboardModel.drop
    ChartPointGroup.drop
  }
}


