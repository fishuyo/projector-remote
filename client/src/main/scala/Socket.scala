package projector

import scala.scalajs.js
import scala.scalajs.js.annotation._
import org.scalajs.dom.document
import org.scalajs.dom.console
import org.scalajs.dom.raw._

import julienrf.json.derived._
import play.api.libs.json._
import play.api.libs.functional.syntax._

import Protocol._


@JSExport
object Socket {
  
  var ws:WebSocket = _

  @JSExport
  def send(data:String){ ws.send(data) }
  
  def send(cmd:Protocol){
    val json = Json.toJson(cmd).toString()
    ws.send(json)
  }

  def init() = {

    ws = new WebSocket(getWebsocketUri())
    
    ws.onopen = { (event: Event) =>
      ws.send("handshake")
      send(RunAll(ProjectorCommand.GetPowerState))

      event
    }

    ws.onerror = { (event: ErrorEvent) => println(event) }

    ws.onmessage = { (event: MessageEvent) =>
      // println(event.data.toString)
      val msg = Json.parse(event.data.toString).as[Protocol]

      msg match {
        case ProjectorList(ps) => Projectors.update(ps)
        case res:ProjectorResponse => Projectors.update(res)
        case _ => ()
      }
    }

    ws.onclose = { (event: Event) =>
      println("ws close")
    }
  }

  def getWebsocketUri(): String = {
    val wsProtocol = if (document.location.protocol == "https:") "wss" else "ws"
    val path = if(document.location.pathname == "/") "/ws" else document.location.pathname+"/ws"
    s"$wsProtocol://${document.location.host}${path}"
  }
}
