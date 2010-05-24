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

class ProjectResourceSpecTest extends SpecificationWithJUnit {

  TestBoot
  /**
   * Type hint for serialization/deserialization
   */
  implicit val formats = Serialization.formats(NoTypeHints)

  val boardResource = new BoardResource
  val projectResource = new ProjectResource

  
  def sleep = Thread.sleep(500)

  val testProject = new Workflow(null, "test-project")
  def updatedProject(projectId: String) = new Workflow(projectId, "test-project-updated")

  "Create project" in {
    val boardId = boardResource.createBoard("test-board")
    sleep
    projectResource.createProject(boardId,
                                  null,
                                  Serialization.write(testProject).getBytes) must notBeNull
  }

  "Update project" in {
    val boardId = boardResource.createBoard("test-board")
    sleep
    val projectId = projectResource.createProject(boardId,
                                                  null,
                                                  Serialization.write(testProject).getBytes)
    sleep
    projectResource.updateProject(null,
                                  Serialization.write(updatedProject(projectId)).getBytes) must notBeNull
  }

  "Delete project" in {
    val boardId = boardResource.createBoard("test-board")
    sleep
    val projectId = projectResource.createProject(boardId,
                                                  null,
                                                  Serialization.write(testProject).getBytes)
    sleep
    projectResource.deleteProject(projectId)
  }

  doAfterSpec{
    TaskUpdateFact.drop
    DashboardModel.drop
    ChartPointGroup.drop
  }

}
