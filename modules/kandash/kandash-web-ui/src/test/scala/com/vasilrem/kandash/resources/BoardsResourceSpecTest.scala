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

class BoardsResourceSpecTest extends SpecificationWithJUnit {

  TestBoot
  val boardResource = new BoardResource
  val boardsResource = new BoardsResource

  "Gets boards" in {
    boardResource.createBoard("test-board")
    Thread.sleep(500)
    boardsResource.getBoards must include("workflows")
  }

  doAfterSpec{
    TaskUpdateFact.drop
    DashboardModel.drop
    ChartPointGroup.drop
  }

}


