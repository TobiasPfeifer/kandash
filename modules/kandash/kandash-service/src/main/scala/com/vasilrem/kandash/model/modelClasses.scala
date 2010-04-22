/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vasilrem.kandash.model

import com.eltimn.scamongo._
import java.util.Date
import scala.reflect.BeanInfo

/**
 * Tier represents standardized state of task per workflow (started, in
 * progress, finished, etc.)
 */
@BeanInfo
case class Tier(_id:String, name:String, order:Int, wipLimit:Option[Int]) extends MongoDocument[Tier]{
  def meta = Tier
}
object Tier extends MongoDocumentMeta[Tier] {
  override def mongoIdentifier = DefaultMongoIdentifier
  override def collectionName = "tiers"
}

/**
 * Workflow is the table column. Represents project of activity that team-members
 * are committed to
 */
@BeanInfo
case class Workflow(_id:String, name:String) extends MongoDocument[Workflow]{
  def meta = Workflow
}
object Workflow extends MongoDocumentMeta[Workflow] {
  override def mongoIdentifier = DefaultMongoIdentifier
  override def collectionName = "workflows"
}

/**
 * User represents a team-member who can access and modify the board
 */
case class User(_id:String, name:String) extends MongoDocument[User]{
  def meta = User

  def this(name: String) = this(null, name)
}
object User extends MongoDocumentMeta[User] {
  override def mongoIdentifier = DefaultMongoIdentifier
  override def collectionName = "users"
}

/**
 * EstimationType is the scale of estimation units (man/days, man/weeks, etc.)
 */
case class EstimationType(_id:String, name:String) extends MongoDocument[EstimationType]{
  def meta = EstimationType
}
object EstimationType extends MongoDocumentMeta[EstimationType] {
  override def mongoIdentifier = DefaultMongoIdentifier
  override def collectionName = "estimationtypes"
}

/**
 * Represents task priority
 */
object TaskPriority extends Enumeration {
  type Priority = Value
  val Low, Medium, High = Value
}

/**
 * Task is an atomic piece of job to be done by team-member
 */
@BeanInfo
case class Task(_id:String, assigneeId:Option[String], description:String, estimation:Option[Int],
                estimationTypeId:Option[String], offsetLeft:Int, offsetTop:Int,
                percentCompleted:Option[Int], priority:Int,
                tierId:String, workflowId:String) extends MongoDocument[Task]{
  def meta = Task  
}
object Task extends MongoDocumentMeta[Task] {
  override def mongoIdentifier = DefaultMongoIdentifier
  override def collectionName = "tasks"
}

/**
 * TaskUpdateFact represents any change of facts state (IOW, new fact is created
 * every time the tier is changed for a task)
 */
@BeanInfo
case class TaskUpdateFact(_id:String, boardId: String, task:Task, updateDate:Date) extends MongoDocument[TaskUpdateFact]{
  def meta = TaskUpdateFact
}
object TaskUpdateFact extends MongoDocumentMeta[TaskUpdateFact] {
  override def mongoIdentifier = DefaultMongoIdentifier
  override def collectionName = "taskupdatefacts"
}

/**
 * DashboardModel contains all information to display dashboard consistently on
 * UI: rows and columns of the board, tasks and their locations, etc.
 */
@BeanInfo
case class DashboardModel(_id:String, name:String, tiers:List[Tier], workflows:List[Workflow],
                          tasks:List[Task]) extends MongoDocument[DashboardModel]{
  def meta = DashboardModel

  /**
   * Gets "backlog" tier
   */
  def backlogTier: Tier = tiers.find(_.order == (tiers.length - 1)).get

  /**
   * Gets "done" tier
   */
  def doneTier: Tier = tiers.find(_.order == 0).get

  /**
   * Gets tier by specified order
   * @val order order of the tier on the board (0 - done (last tier))
   */
  def getTierByOrder(order: Int): Tier = tiers.find(_.order == order).get

}
object DashboardModel extends MongoDocumentMeta[DashboardModel] {
  override def mongoIdentifier = DefaultMongoIdentifier
  override def collectionName = "dashboardmodels"
}

/**
 * Represents report model based on task modifications
 * @val reportModelRecords list of model records
 */
@BeanInfo
case class ReportModel(taskHistoryEntries: List[ReportModelRecord])

@BeanInfo
case class ReportModelRecord(taskFact: TaskUpdateFact, workflow: Workflow, tier: Tier, taskCreated: Date, daysActive: Double)

/**
 * Chart model consisting of chart point groups
 * @val chartGroups groups of chart points
 */
@BeanInfo
case class ChartModel(chartGroups: List[ChartPointGroup])

/**
 * Represents group of chart points (association of the data and count of tasks
 * assigned during the specified period per tier)
 * @val workflowId identifier of the workflow
 * @val date date on the chart
 * @val tiers association of the data and count of tasks
 * completed during the specified period per tier
 */
@BeanInfo
case class ChartPointGroup(_id:String, workflowId: String, date: Date, tiers: List[ChartPoint]) extends MongoDocument[ChartPointGroup]{
  def meta = ChartPointGroup
}
object ChartPointGroup extends MongoDocumentMeta[ChartPointGroup] {
  override def mongoIdentifier = DefaultMongoIdentifier
  override def collectionName = "chartpointgroups"
}

/**
 * Represents single point on the chart. Cound of tasks assigned per specified tier
 */
@BeanInfo
case class ChartPoint(_id: String, tierId: String, tierName: String, count: Double) extends MongoDocument[ChartPoint]{
  def meta = ChartPoint
}
object ChartPoint extends MongoDocumentMeta[ChartPoint] {
  override def mongoIdentifier = DefaultMongoIdentifier
  override def collectionName = "chartpoints"
}