package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.streams._

import akka.actor._
import akka.stream._

import collection.mutable.HashMap
import collection.mutable.ListBuffer


@Singleton
class HomeController @Inject()(cc: ControllerComponents)(implicit system: ActorSystem, materializer: Materializer) extends AbstractController(cc) {

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def socket = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef { out =>
      WebsocketActor.props(out)
    }
  }

}
