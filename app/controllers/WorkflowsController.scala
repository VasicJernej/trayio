package controllers

import java.util.Date
import javax.inject.{ Inject, Singleton }
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import play.api.mvc.{ AbstractController, ControllerComponents }
import play.api.libs.json._

import models.{ Constants, Workflows, Workflow, WorkflowExecution, Works }

@Singleton
class WorkflowsController @Inject() (cc: ControllerComponents) extends AbstractController(cc) {

	/**
	 * Add a Workflow with a number of steps
	 */
	def addWorkflow = Action(parse.json) { implicit request =>
		request.body.validate[Workflow].map { x =>
			{
				val workflowId = Workflows.workflowsMap.size + 1
				Workflows.workflowsMap.putIfAbsent(workflowId, Works(
					Workflow(
						workflowId = Some(workflowId),
						numberOfSteps = x.numberOfSteps
					),
					None
				))
				Created(Json.obj(
					Constants.status -> Constants.created,
					Constants.workflowId -> workflowId
				))
			}
		}.recoverTotal { r: JsError => BadRequest }
	}

	/**
	 * Add a new execution for a given workflow id.
	 */
	def addExecution(workflow_id: Int) = Action {
		implicit request =>
		Workflows.workflowsMap.get(workflow_id).map { s: Works => {
			val wf = s.workflow
			val execution = WorkflowExecution(
				workflowId = wf.workflowId.get,
				currentStep = 1,
				workflowExecutionId = wf.workflowId.get,
				creationDate = new Date().getTime.toString()
			)
			Workflows.workflowsMap.putIfAbsent(wf.workflowId.get, Works(wf, Some(execution)))
			Created(
				Json.obj(
					Constants.status -> Constants.created,
					Constants.workflowExecutionId -> execution.workflowExecutionId
				)
			)
		}}.getOrElse(NotFound(Json.obj(
			Constants.status -> Constants.notFound,
			Constants.message -> Constants.workflowNF
		)))
	}

	/**
	 * Increment the current step of an execution.
	 */
	def incrementStep(workflow_id: Int, workflow_execution_id: Int) = Action {
		implicit request =>
		val s: Option[Works] = Workflows.workflowsMap.get(workflow_id)
		if (s.isEmpty) {
			NotFound(
				Json.obj(
					Constants.status -> Constants.notFound,
					Constants.message -> Constants.workflowNF
				)
			)
		} else {
			val workflow = s.get.workflow
			val maxSteps = workflow.numberOfSteps
			s.get.execution.map {
				x: WorkflowExecution =>	x.currentStep match {
					case f => {
						if (f < maxSteps) {
							val execution = WorkflowExecution(
								workflowId = x.workflowId,
								currentStep = f + 1,
								workflowExecutionId = x.workflowExecutionId,
								creationDate = x.creationDate
							)
							Workflows.workflowsMap(workflow_id) = Works(workflow, Some(execution))
							NoContent
						} else {
							BadRequest(
								Json.obj(
									Constants.status -> Constants.badRequest,
									Constants.message -> "Max steps reached."
								)
							)
						}
					}
				}
			}.getOrElse(
				NotFound(
					Json.obj(
						Constants.status -> Constants.notFound,
						Constants.message -> Constants.executionNF
					)
				)
			)
		}
	}

	/**
	 * Get the current state of a previously created execution.
	 */
	def executionState(workflow_id: Int, workflow_execution_id: Int) = Action {
		val s: Option[Works] = Workflows.workflowsMap.get(workflow_id)
		if (s.isEmpty) {
			NotFound(Json.obj(Constants.status -> Constants.notFound, Constants.message -> Constants.workflowNF))
		} else {
			val execution: Option[WorkflowExecution] = s.get.execution
			// A case where Works exists and Workflow doesn't is illegal, meaning this is safe to do.
			val workflow: Workflow = s.get.workflow
			if (execution.isEmpty) {
				NotFound(Json.obj(Constants.status -> Constants.notFound, Constants.message -> Constants.executionNF))
			} else {
				if (execution.get.currentStep == workflow.numberOfSteps) {
					Ok(Json.obj(Constants.status -> Constants.ok, Constants.finished -> "True"))
				} else {
					Ok(Json.obj(Constants.status -> Constants.ok, Constants.finished -> "False"))
				}
			}
		}
	}
}
