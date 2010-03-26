package com.vasilrem.ideabox

import org.specs._
import java.util.Date
import com.eltimn.scamongo._;
import com.vasilrem.kandash.model._;
import net.liftweb.json.JsonDSL._
import com.mongodb.ObjectId
import com.vasilrem.kandash.service._

class IdeaServiceSpecTest extends SpecificationWithJUnit {

  val kandashService = new KandashService{
    val host = "localhost"
    val port = 27017
    val database = "kandash_test"
  }

  "Creation of a new dashboard" should{
    "return a valid dashboard ID" in{
      val validID = kandashService.createNewDashboard("testDashboard")
      println("""New dashboard "testDashboard" has been created with ID """ + validID)
      validID must notBeNull
    }
  }

  "Request for the dashboard by a valid ID" should{
    "return a valid dashboard" in{
      val validID = kandashService.createNewDashboard("testDashboard")
      val validDashboard = kandashService.getDashboardById(validID)
      println("Request for dashboard with id " + validID + " returned " + validDashboard)
      validDashboard must notBeNull
    }
  }

  doAfter{
    DashboardModel.drop
  }


}
