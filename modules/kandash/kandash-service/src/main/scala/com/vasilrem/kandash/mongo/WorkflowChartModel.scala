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
import java.util.Calendar
import java.text.SimpleDateFormat

object WorkflowChartModel {

  def main(args: Array[String]): Unit = {

    val prepared = new PreparedFunction{
      val host = "localhost"
      val port = 27017
      val database = "kandash"
    }

    val kandashService = new KandashService{
      val host = "localhost"
      val port = 27017
      val database = "kandash"
      val preparedFunction = prepared
    }

    def NewTask(taskID: String, workflowId: String, tierId: String) =
      new Task(taskID,
               new Some("unknown"),
               "Test Task",
               new Some(5),
               new Some("unknown"),
               50,
               50,
               new Some(100),
               1,
               tierId,
               workflowId)

    MongoDB.defineDb(DefaultMongoIdentifier, MongoAddress(MongoHost("localhost", 27017), "kandash"))
    val cal = Calendar.getInstance
    cal.set(Calendar.YEAR, 2010)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    //val boardId = kandashService.createNewDashboard("kandash")
    val board = kandashService.getDashboardByName("kandash")
    val boardId = board._id
    val projectId = board.workflows.first._id
    val random = new java.util.Random
    var backlogCount = 0
    (1 until 300) foreach{
      i=>
      cal.set(Calendar.DAY_OF_YEAR, i)
      val date = cal.getTime
      ChartPointGroup(ObjectId.get.toString,
                      projectId,
                      date,
                      List(ChartPoint(ObjectId.get.toString, board.tiers.apply(0)._id, board.tiers.apply(0).name, random.nextInt(4)),
                           ChartPoint(ObjectId.get.toString, board.tiers.apply(1)._id, board.tiers.apply(1).name, random.nextInt(4)),
                           ChartPoint(ObjectId.get.toString, board.tiers.apply(2)._id, board.tiers.apply(2).name, backlogCount))).save
      backlogCount += random.nextInt(2)
    }
  }

}
