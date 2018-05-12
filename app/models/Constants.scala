package models

import play.api.http

object Constants {
  val status = "status"
  val message = "message"
  val workflowId = "workflow_id"
  val workflowExecutionId = "workflow_execution_id"
  val numSteps = "number_of_steps"
  val finished = "finished"
  val workflowNF = "Workflow Not found."
  val executionNF = "Execution Not found."

  val badRequest = http.Status.BAD_REQUEST
  val notFound = http.Status.NOT_FOUND
  val ok = http.Status.OK
  val created = http.Status.CREATED
}