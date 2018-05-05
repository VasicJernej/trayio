package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test._
import play.api.test.Helpers._

class WorkflowSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  "Workflows GET" should {

    "render the index page from a new instance of controller" in {
      val controller = new Workflows(stubControllerComponents())
      val home = controller.index().apply(FakeRequest(GET, "/"))

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include ("Welcome to Play")
    }

    "render the index page from the application" in {
      val controller = inject[Workflows]
      val home = controller.index().apply(FakeRequest(GET, "/"))

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include ("Welcome to Play")
    }

    "render the index page from the router" in {
      val request = FakeRequest(GET, "/")
      val home = route(app, request).get

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include ("Welcome to Play")
    }
    
    "workflows must return 501 not implemented" in {
      val request = FakeRequest(POST, "/workflows")
      val workflowsRoute = route(app, request).get
      
      status(workflowsRoute) mustBe 501
    }
  }
}
