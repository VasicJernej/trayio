package controllers

import javax.inject._
import play.api._
import play.api.mvc._

@Singleton
class Workflows @Inject() (cc: ControllerComponents) extends AbstractController(cc) {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def addWorkflow = Action {
    NotImplemented
  }

  def addExecution(workflow_id: Int) = TODO

  def incrementStep(workflow_id: Int, workflow_execution_id: Int) = TODO

  def executionState(workflow_id: Int, workflow_execution_id: Int) = TODO
}
