package models

import play.api.libs.json._

import java.util.Date
import scala.collection.concurrent.{ TrieMap => MMap }

/*
 * Class to create workflows with a number of steps
 * 
 * @param workflowId The identification of the workflow. It is optional to allow creating an
 *                   instance from a Json request without providing the id.
 * @param numberOfSteps Amount of executions allowed
 */
case class Workflow(workflowId: Option[Int], numberOfSteps: Int)

object Workflow {
  implicit object WorkflowFormat extends Format[Workflow] {
    def reads(json: JsValue): JsResult[Workflow] = {
      val s = (json \ Constants.numSteps)
      if (s.isEmpty) {
        JsError(Seq())
      } else {
        JsSuccess(
          Workflow(
            (json \ Constants.workflowId).asOpt[Int],
            (json \ Constants.numSteps).as[Int]
          )
        )
      }
    }
    def writes(w: Workflow): JsValue = {
      JsObject(
        Seq(
          Constants.workflowId -> (w.workflowId match {
            case None => JsNull
            case Some(x) => JsNumber(x)
          }),
          Constants.numSteps -> JsNumber(w.numberOfSteps)
        )
      )
    }
  }
}

/**
 * Creates an execution instance.
 * 
 * @param workflowId Identification of the workflow object that created this execution.
 * @param currentStep Amount of calls to the incrementStep function.
 * @param workflowExecutionId Identification for the execution.
 * @param creationDate The date of creation.
 */
case class WorkflowExecution(
  workflowId: Int,
  currentStep: Int,
  workflowExecutionId: Int,
  creationDate: String
)

/**
 * Class to hold a tuple of a workflow and an optional execution. 
 */
case class Works(workflow: Workflow, execution: Option[WorkflowExecution])

object Workflows {
  var workflowsMap: MMap[Int, Works] = MMap()
}
