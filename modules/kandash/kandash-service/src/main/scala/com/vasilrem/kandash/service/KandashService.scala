/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vasilrem.kandash.service

import com.eltimn.scamongo._;
import com.vasilrem.kandash.model._;
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST._
import net.liftweb.json._
import com.mongodb._

/**
 * Represents high-level server-sede routing of the board
 */
trait KandashService extends JObjectBuilder{

  /** mongo host */
  val host:String
  /** mongo port */
  val port:Int
  /** mongo database name */
  val database:String
  /** tiers (stages) added to the new board by default */
  val defaultTiers:List[Tier] = List(new Tier(ObjectId.get.toString, "{tier.name.todo}", 2, None),
                                     new Tier(ObjectId.get.toString, "{tier.name.inprogress}", 1, None),
                                     new Tier(ObjectId.get.toString, "{tier.name.done}", 0, None))
  /** workflows (projects) added to the new board by default */
  val defaultWorkflows:List[Workflow] = List(new Workflow(ObjectId.get.toString, "workflow.name.default"))

  /** instantiates new connection to mongo */
  MongoDB.defineDb(DefaultMongoIdentifier, MongoAddress(MongoHost(host, port), database))

  /**
   * Craete new board model
   * @val name name of the model
   * @return board model identifier
   */
  def createNewDashboard(name:String):String = {
    val model = DashboardModel(ObjectId.get.toString, name, defaultTiers, defaultWorkflows, List())
    model.save
    model._id
  }

  /**
   * Get board by ID
   * @val id board model indetifier
   * @return board model
   */
  def getDashboardById(id:String):DashboardModel = DashboardModel.find(id).get

  /**
   * Create dummy empty dashboard with the given ID
   * @param id identifier of the new board
   * @return new dummy board model
   */
  def createDummyDashboard(id:String):DashboardModel = {
    val model = DashboardModel(id, "dummy", defaultTiers, defaultWorkflows, List())
    model.save
    model
  }

  /**
   * Get board by name
   * @val name board name
   * @return board model
   */
  def getDashboardByName(name:String):DashboardModel = DashboardModel.find("name", name).get

  /**
   * Gets list of dashboards from backend (only id's and names are filled)
   * @return list of boards
   */
  def getDashboards: List[DashboardModel] = DashboardModel.findAll

  /**
   * Adds a new element to the board
   * @val boardId identifier of the board the element will be added to
   * @val document element to be added
   * @return element identifier
   */
  def add[A](boardId: String, document: MongoDocument[A]): String = {
    val documentId = ObjectId.get.toString
    DashboardModel.update(("_id" -> boardId),
                          ("$push" -> (document.meta.collectionName -> buildQuery(documentId, document))
      ))
    documentId
  }

  /**
   * Adds task to the board
   * @val boardId identifier of the board the task will be added to
   * @val task task to be added
   * @return task identifier
   */
  def addTask(boardId: String, task: Task): String = {    
    val taskId = add[Task](boardId, task)
    TaskUpdateFact(ObjectId.get.toString,
                   taskId,
                   task.tierId,
                   new java.util.Date).save
    taskId
  }

  /**
   * Adds a new tier to the board
   * @val boardId identifier of the board the tier will be added to
   * @val tier tier to be added
   * @return tier identifier
   */
  def addTier(boardId: String, tier: Tier): String = {
    incTiersOrder(boardId, tier.order - 1)
    add[Tier](boardId, tier)
  }
  
  /**
   * Updates board element
   * @val document board element
   * @return element identifier
   */
  def update[A](document: MongoDocument[A]): String = {
    val collectionName = document.meta.collectionName
    val documentId = getDocumentIdentifier(document)
    DashboardModel.update((collectionName + "._id" -> documentId),
                          ("$set" -> (collectionName + ".$" -> buildQuery(documentId, document))
      ))
    documentId
  }

  /**
   * Updates task
   * @val task
   * @return task identifier
   */
  def updateTask(task:Task): String = {
    createFact(task._id, task.tierId)
    update[Task](task)
  }

  /**
   * Creates new fact based on changing the task's tier (state)
   * @val taskId identifier of the task
   * @val tierId identifier if the new task's tier (state)
   * @return true, if task's tier was changed
   */
  def createFact(taskId: String, tierId: String):Boolean = {
    val tierIsChanged: Boolean =
      DashboardModel.find(("tasks._id" -> taskId)).get.tasks.find {task => task._id == taskId && task.tierId == tierId} == None
    if(tierIsChanged) TaskUpdateFact(ObjectId.get.toString, taskId, tierId, new java.util.Date).save
    tierIsChanged
  }

  /**
   * Updates tier
   * @val tier
   * @return tier identifier
   */
  def updateTier(tier: Tier): String = {
    changeTierOrder(tier._id.toString(), tier.order)
    update[Tier](tier)
  }

  /**
   * Increases order number of the board tiers, starting from the tier with
   * the given order number
   * @param boardId identifier of the board the tiers should be updated at
   * @param startingFromOrder tiers with the order number bigger then specified
   * will have increased (by 1) order number
   */
  def incTiersOrder(boardId: String, startingFromOrder: Int) =
    updateTiersOrder(boardId, startingFromOrder, true)

  /**
   * Decreases order number of the board tiers, starting from the tier with
   * the given order number
   * @param boardId identifier of the board the tiers should be updated at
   * @param startingFromOrder tiers with the order number bigger then specified
   * will have decreased (by 1) order number
   */
  def decTiersOrder(boardId: String, startingFromOrder: Int) =
    updateTiersOrder(boardId, startingFromOrder, false)

  /**
   * Decreases order number of the board tiers, starting from the specified tier
   * @param tier identifier of the tier order decrease should start from
   */
  def decTiersOrder(tierId: String) = {
    val board = DashboardModel.find((Tier.collectionName + "._id" -> tierId)).get
    val tierOrder = board.tiers.find(_._id == tierId).get.order
    updateTiersOrder(board._id, tierOrder, false)
  }

  /**
   * Decreases or increases order number of the board tiers, starting from the tier with
   * the given order number
   * @param boardId identifier of the board the tiers should be updated at
   * @param startingFromOrder tiers with the order number bigger then specified
   * will have decreased or increased (by 1) order number
   * @param isInc if true, order number will be increased
   */
  def updateTiersOrder(boardId: String, startingFromOrder: Int, isInc: Boolean) = {
    MongoDB.use(DefaultMongoIdentifier) ( db => {
        val sign = if(isInc){"+"} else {"-"}
        db.eval(""" function() {
                  db.dashboardmodels.find({'_id' : ObjectId('""" + boardId + """')}).forEach(
                    function(o){
                      for(var i=0;i<o.tiers.length;i++){
                        var tier=o.tiers[i];
                        if(tier.order>""" + startingFromOrder + """){
                          tier.order""" + sign + """=1;
                        }
                      }
                    db.dashboardmodels.save(o);
                  })
                }""")
      })
  }

  /**
   * Changes order of the tier to the specified value
   * @param tierId identifier of the tier to be updated
   * @param order new tier order value
   */
  def changeTierOrder(tierId: String, order: Int) = {
    MongoDB.use(DefaultMongoIdentifier) ( db => {
        db.eval(""" function() {
                  db.dashboardmodels.find({'tiers._id' : ObjectId('""" + tierId + """')}).forEach(
                    function(o){
                      var sourceIndex
                      var targetIndex
                      var sourceOrder
                      for(var i=0;i<o.tiers.length;i++){
                        var tier=o.tiers[i];
                        if(tier._id.toString() == '""" + tierId + """'){
                          sourceIndex = i
                          sourceOrder = tier.order
                        }
                        if(tier.order == """ + order + """){
                          targetIndex = i
                        }
                      }
                      print('targetIndex = ' + targetIndex)
                      print('sourceIndex = ' + sourceIndex)
                      print('order = ' + """ + order + """)
                      print('sourceOrder = ' + sourceOrder)
                      o.tiers[targetIndex].order = sourceOrder
                      o.tiers[sourceIndex].order = """ + order + """
                      db.dashboardmodels.save(o);
                  })
                }""")
      })
  }

  /**
   * Removes element from the board
   * @val documentId element identifier
   * @val collectionName name of the element's collection'
   */
  def remove(documentId: String, collectionName: String): Unit = {
    val boardId = DashboardModel.find((collectionName + "._id" -> documentId)).get._id
    // Workaround to remove element from collection. $unset shouldn't be used,
    // because it replaces removed elements with nulls, that cannot be be pull out
    // of the list
    DashboardModel.update((collectionName + "._id" -> documentId),
                          ("$set" -> (collectionName + ".$" -> "null")
      ))
    DashboardModel.update(("_id" -> boardId),
                          ("$pull" -> (collectionName -> "null")
      ))
  }

  /**
   * Removes all tasks assigned to tier, project or any other container
   * @param collectionType collection type of the container
   */
  def removeTasksFromContainer(containerId: String, collectionType: String) = {
    val containerRefId = collectionType.substring(0, collectionType.length - 1) + "Id"
    MongoDB.use(DefaultMongoIdentifier) ( db => {
        db.eval(""" function() {
                  db.dashboardmodels.find({'""" + collectionType + """._id' : ObjectId('""" + containerId + """')}).forEach(
                    function(o){
                      for(var i=(o.tasks.length - 1);i>=0;i--){
                        print('Task ref ID: ' + o.tasks[i].""" + containerRefId + """.toString())
                        print('Container ID: """ + containerId + """')
                        if(o.tasks[i].""" + containerRefId + """.toString() == '""" + containerId + """'){
                          print('Removing ' + o.tasks[i]._id)
                          o.tasks.splice(i, 1);
                        }
                      }
                      db.dashboardmodels.save(o);
                    })
                }""")
      })
  }

  /**
   * Removes tier from the board
   * @val tierId identifier of the tier to be removed
   */
  def removeTier(tierId: String): Unit = {
    decTiersOrder(tierId)
    removeTasksFromContainer(tierId, Tier.collectionName)
    remove(tierId, Tier.collectionName)
  }

  /**
   * Removes project from the board
   * @val projectId identifier of the project to be removed
   */
  def removeProject(projectId: String): Unit = {
    removeTasksFromContainer(projectId, Workflow.collectionName)
    remove(projectId, Workflow.collectionName)
  }

  /**
   * Removes element from the board
   * @val document element to be removed
   */
  def remove[A](document: MongoDocument[A]): Unit =
    remove(getDocumentIdentifier(document), document.meta.collectionName)

  /**
   * Gets document identifier (value of "_id" field)
   * @val document any element that can present on the board (task, project, tier)
   */
  def getDocumentIdentifier(document: AnyRef): String = {
    val idField = document.getClass.getDeclaredField("_id")
    idField.setAccessible(true)
    idField.get(document).toString
  }

  /**
   * Drops all collecitons related to KandashService
   */
  def dropAllCollections = {
    DashboardModel.drop
    TaskUpdateFact.drop
  }

}

