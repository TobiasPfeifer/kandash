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

object HistoryChartModel {

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
    prepared.loadPreparedFunctions(List("/mongo/preparedFunctions.js"))
    val cal = Calendar.getInstance
    cal.set(Calendar.YEAR, 2010)
    kandashService.createNewDashboard("kandash")
    val board = kandashService.getDashboardByName("kandash")
    val boardId = board._id
    val projectId = board.workflows.first._id
    (1 until 300) foreach{
      i=>
      cal.set(Calendar.DAY_OF_YEAR, i)
      val date = cal.getTime
      if(i%2 == 0){
        val task = NewTask(ObjectId.get.toString, projectId, board.tiers.apply(0)._id)
        kandashService.addTask(boardId, task)
        TaskUpdateFact(ObjectId.get.toString, boardId, task, date).save
      }
      if(i%4 == 0){
        val task = NewTask(ObjectId.get.toString, projectId, board.tiers.apply(2)._id)
        kandashService.addTask(boardId, task)
        TaskUpdateFact(ObjectId.get.toString, boardId, task, date).save
      }
      if(i%3 == 0){
        val task = NewTask(ObjectId.get.toString, projectId, board.tiers.apply(1)._id)
        kandashService.addTask(boardId, task)
        TaskUpdateFact(ObjectId.get.toString, boardId, task, date).save
      }
    }
  }

}
