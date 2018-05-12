package controllers

import java.util.Date
import scala.collection.mutable.{ Map => MMap }

import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._

import models.{ Workflow, Workflows, WorkflowExecution, Works }

class WorkflowSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {
	val controller = new WorkflowsController(stubControllerComponents())

	"workflows POST" should {

		"create a new workflow with a number of steps json request" in {
			val jsonBody = """{ "number_of_steps": 2 }"""
			val jsonObj = Json.parse(jsonBody)
			val request = FakeRequest(POST, "/workflows").withJsonBody(jsonObj)
			val result = route(app, request).get
			status(result) mustBe 201
		}

		"bad request returns 400" in {
			val jsonBody = """{ "numbdaer_of_steps": 2 }"""
			val request = FakeRequest(POST, "/workflows").withJsonBody(Json.parse(jsonBody))
			val result = route(app, request).get
			status(result) mustBe 400

		}
	}

	"add execution POST" should {
		"201 and create a new execution with a workflow id" in {
			Workflows.workflowsMap = MMap(2 -> Works(
				Workflow(Some(2), 3), Some(WorkflowExecution(2, 0, 2, new Date().getTime.toString()))
			))
			val request = FakeRequest(POST, "/workflows/2/executions")
			val result = route(app, request).get
			status(result) mustBe 201
		}

		"404 if the id is not found" in {
			Workflows.workflowsMap = MMap()
			val request = FakeRequest(POST, "/workflows/2/executions")
			val result = route(app, request).get
			status(result) mustBe 404
		}
	}

	"increment counter PUT" should {
		"404 if the workflow id doesn't exist" in {
			Workflows.workflowsMap = MMap()
			val request = FakeRequest(PUT, "/workflows/20/executions/20/")
			val result = route(app, request).get
			status(result) mustBe 404
		}
		"404 if execution doesn't exist" in {
			Workflows.workflowsMap = MMap(2 -> Works(Workflow(Some(2), 3), None))
			val request = FakeRequest(PUT, "/workflows/2/executions/2/")
			val result = route(app, request).get
			status(result) mustBe 404
		}

		"400 if the current step is too high" in {
			Workflows.workflowsMap = MMap(2 -> Works(
				Workflow(Some(2), 3), Some(WorkflowExecution(2, 3, 2, new Date().getTime.toString()))
			))
			val request = FakeRequest(PUT, "/workflows/2/executions/2/")
			val result = route(app, request).get
			status(result) mustBe 400
		}

		"204 No Content if the current step could be incremented" in {
			Workflows.workflowsMap = MMap(2 -> Works(
				Workflow(Some(2), 3), Some(WorkflowExecution(2, 0, 2, new Date().getTime.toString()))
			))
			val request = FakeRequest(PUT, "/workflows/2/executions/2/")
			val result = route(app, request).get
			status(result) mustBe 204
			Workflows.workflowsMap.get(2).value.execution.get.currentStep mustBe 1
		}
	}

	"execution GET" should {

		"404 if workflow doesn't exist" in {
			Workflows.workflowsMap = MMap()
			val request = FakeRequest(GET, "/workflows/20/executions/20/")
			val result = route(app, request).get
			status(result) mustBe 404
		}

		"404 if execution doesn't exist" in {
			Workflows.workflowsMap = MMap(2 -> Works(Workflow(Some(2), 3), None))
			val request = FakeRequest(GET, "/workflows/2/executions/2/")
			val result = route(app, request).get
			status(result) mustBe 404
		}

		"200 and is finished true" in {
			Workflows.workflowsMap = MMap(2 -> Works(
				Workflow(Some(2), 3), Some(WorkflowExecution(2, 3, 2, new Date().getTime.toString()))
			))
			val request = FakeRequest(GET, "/workflows/2/executions/2/")
			val result = route(app, request).get
			status(result) mustBe 200
		}

		"200 and is finished false" in {
			Workflows.workflowsMap = MMap(2 -> Works(
				Workflow(Some(2), 3), Some(WorkflowExecution(2, 2, 2, new Date().getTime.toString()))
			))
			val request = FakeRequest(GET, "/workflows/2/executions/2/")
			val result = route(app, request).get
			status(result) mustBe 200
		}

	}
}
