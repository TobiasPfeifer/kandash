/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vasilrem.kandash.service

import org.specs._
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

class HistoryChartModelSpecTest extends SpecificationWithJUnit {

  val prepared = new PreparedFunction{
    val host = "localhost"
    val port = 27017
    val database = "chart"
  }

  val reportingService = new ReportingService{
    val host = "localhost"
    val port = 27017
    val database = "chart"
    val preparedFunction = prepared
  }

  val kandashService = new KandashService{
    val host = "localhost"
    val port = 27017
    val database = "chart"
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

  doBeforeSpec{
    /*MongoDB.defineDb(DefaultMongoIdentifier, MongoAddress(MongoHost("localhost", 27017), "chart"))
     TaskUpdateFact.drop
     DashboardModel.drop
     prepared.loadPreparedFunctions(List("/mongo/preparedFunctions.js"))
     val cal = Calendar.getInstance
     cal.set(Calendar.YEAR, 2010)
     val boardId = kandashService.createNewDashboard("chart")
     val board = kandashService.getDashboardById(boardId)
     val projectId = board.workflows.first._id
     (1 until 300) foreach{
     i=>
     cal.set(Calendar.DAY_OF_YEAR, i)
     val date = cal.getTime
     var taskId = ObjectId.get.toString
     kandashService.addTask(boardId, NewTask(taskId, projectId, board.tiers(0)._id))
     TaskUpdateFact(taskId, ObjectId.get.toString, projectId, board.tiers(0)._id, date).save
     if(i%3 == 0){
     taskId = ObjectId.get.toString
     kandashService.addTask(boardId, NewTask(taskId, projectId, board.tiers(2)._id))
     TaskUpdateFact(taskId, ObjectId.get.toString, projectId, board.tiers(2)._id, date).save
     }
     if(i%2 == 0){
     taskId = ObjectId.get.toString
     kandashService.addTask(boardId, NewTask(taskId, projectId, board.tiers(1)._id))
     TaskUpdateFact(taskId, ObjectId.get.toString, projectId, board.tiers(1)._id, date).save
     }
     }
     println("Dummy board is created")
     println("Dummy facts are created")*/
  }

  "Monthly chart model for the specified data" should {
    "contain more than one group of chart poins" in{
      /*val board = kandashService.getDashboardByName("chart")
       val chartModel = reportingService.getMonthlyChartModel(board._id, None)
       print("Chart model: " + chartModel)
       chartModel.points.length must beGreaterThan(0)*/
    }
  }

}
