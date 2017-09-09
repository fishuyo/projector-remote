package controllers
package projector

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.streams._
import play.api.http.HttpErrorHandler

import akka.actor._
import akka.stream._

import collection.mutable.HashMap
import collection.mutable.ListBuffer


class Assets @Inject() (
  errorHandler: HttpErrorHandler,
  assetsMetadata: AssetsMetadata
) extends AssetsBuilder(errorHandler, assetsMetadata)

// object ChildAssets extends AssetsBuilder(LazyHttpErrorHandler) {
//   lazy val additionalPath = if(play.Play.application().configuration().getBoolean("child.submodule")) "/lib/server/" else ""
//   override def at(path: String, file: String, aggressiveCaching: Boolean): Action[AnyContent] = {
//     super.at(path + additionalPath, file, aggressiveCaching)
//   }
// }

@Singleton
class HomeController @Inject()(cc: ControllerComponents)(implicit system: ActorSystem, materializer: Materializer) extends AbstractController(cc) {

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.projector.index())
  }

  def socket = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef { out =>
      WebsocketActor.props(out)
    }
  }

}
