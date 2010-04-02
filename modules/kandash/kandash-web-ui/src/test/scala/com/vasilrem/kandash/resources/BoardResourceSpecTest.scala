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

class BoardResourceSpecTest extends SpecificationWithJUnit {

  val boardResource = new BoardResource()

  "Creates new board" in{    
    boardResource.createBoard("tast-board") must notBeNull
  }

  "Gets board" in {
    val boardId = boardResource.createBoard("test-board")
    boardResource.getBoard(boardId) must include("test-board")
  }

}


