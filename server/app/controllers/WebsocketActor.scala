package controllers

import projector._
import projector.Protocol._
import ProjectorCommand._

import julienrf.json.derived._
import play.api.libs.json._
import play.api.libs.functional.syntax._

import akka.actor._


object WebsocketActor {
  def props(out: ActorRef) = Props(new WebsocketActor(out))
}

class WebsocketActor(out: ActorRef) extends Actor {

  val actors = Projectors.all.zipWithIndex.map{ case (p,i) => context.actorOf(ProjectorActor.props(p), s"projector-$i") }.toArray

  def receive = {
    case msg:String if msg == "keepalive" => ()
    case msg:String if msg == "handshake" => 
      sendProjectorList()
      actors.foreach(_ ! ProjectorActor.Connect)
    case msg:String => 
      val proto = Json.parse(msg).as[Protocol]
      proto match {
        case Run(id, cmd) => actors(id) ! cmd
        case RunGroup(group, cmd) => Projectors.projectors(group).map(_.id).foreach( actors(_) ! cmd)
        case RunAll(cmd) => actors.foreach( _ ! cmd)
      }

    case msg:ProjectorResponse => out ! Json.toJson(msg).toString

    case msg => println(msg)
  }

  def sendProjectorList() = {
    val projectors = Projectors.projectors
    val map = projectors.map { 
      case (g,ps) => 
        val pis = ps.map { 
          case p:PD => ProjectorInfo(p.id, p.name, Seq(On, Off, Mute, Unmute))
          case p:Christie => ProjectorInfo(p.id, p.name, Seq(On, Off, Mute, Unmute, SurroundMode, DesktopMode))
          case p:TestProjector => ProjectorInfo(p.id, p.name, Seq(On, Off, Mute, Unmute))
        }
        (g,pis)
    }
    out ! Json.toJson(ProjectorList(map)).toString
  }

}
