package projector

import org.scalajs.dom.raw.Event

import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding._
import com.thoughtworks.binding.dom

import ProjectorCommand._

import scala.scalajs.js
import scala.scalajs.js.annotation._
import scala.scalajs.js.timers.setInterval

@JSExport
object Projectors {

  val projectors = Vars.empty[(String, Seq[(ProjectorInfo, Var[ProjectorResponse])])]

  def apply() = projectors

  def update(gs:Map[String,Seq[ProjectorInfo]])  = {
    gs.foreach { case (group, ps) => 
      val res = ps.map( p => Var(ProjectorResponse(p.id, ProjectorStatus.Disconnected, "")))
      projectors.value += ((group, ps.zip(res)))
    }
    js.Dynamic.global.collapsible()
  }

  def update(res:ProjectorResponse) = {
    projectors.value.foreach { case (g,ps) =>
      ps.find(_._1.id == res.id).foreach(_._2.value = res)
    }
  }

  @JSExport
  def runGroup(group:String, cmd:String) = {
    Socket.send(RunGroup(group,Command(cmd)))
  }

  @JSExport
  def run(id:Int, cmd:String) = {
    Socket.send(Run(id,Command(cmd)))
  }

  object views {
   
    @dom
    def projectorList = {
      <ul class="collection with-header">
        <li class="collection-header">
          <div class="row">
            <div class="col s4">
              <h4>Projectors</h4> 
            </div>
            <div class="col s8">
              { for(cmd <- Constants(Seq(On,Off,Mute,Unmute): _*)) yield {
                <a href="#" onclick={ event:Event => event.preventDefault(); event.stopPropagation(); Socket.send(RunAll(cmd))} class="btn grey lighten-4 black-text waves-effect waves-teal">
                  {cmd.getClass.getSimpleName}
                </a>
              }}
            </div>
          </div>
        </li>
        { for( (g,ps) <- projectors) yield { collapsibleGroup(g,ps).bind } }
      </ul> 
    }

    @dom
    def projectorGroup(group:String, ps:Seq[(ProjectorInfo, Var[ProjectorResponse])]) = {
        <ul class="collection with-header no-margin">
          <li class="collection-header">
            <div class="row">
              <div class="col s2">
                <h5>{ group }</h5> 
              </div>
              <div class="col s10">
                { for(cmd <- Constants(ps.head._1.commands: _*)) yield {
                  <a href="#" onclick={ event:Event => event.preventDefault(); event.stopPropagation(); Socket.send(RunGroup(group, cmd))} class="btn grey lighten-4 black-text waves-effect waves-teal">
                    {cmd.getClass.getSimpleName}
                  </a>
                }}
              </div>
            </div>
          </li>
          { Constants(ps: _*).map{ case (p,r) => projector(p,r).bind }}
        </ul>
    }

    @dom
    def collapsibleGroup(group:String, ps:Seq[(ProjectorInfo, Var[ProjectorResponse])]) = {
      <ul class="collapsible collapsible-accordion no-margin">
        <li>
          <a class="collapsible-header">
            <div class="row">
              <div class="col s3">
                <i class="material-icons black-text">arrow_drop_down</i>
                <h5 class="black-text">{ group }</h5> 
              </div>
              <div class="col s9">
                { for(cmd <- Constants(ps.head._1.commands: _*)) yield {
                  <a href="#" onclick={ event:Event => event.preventDefault(); event.stopPropagation(); Socket.send(RunGroup(group, cmd))} class="btn grey lighten-4 black-text waves-effect waves-teal">
                    {cmd.getClass.getSimpleName}
                  </a>
                }}
                { badge(ps.map(_._2), Seq(ProjectorStatus.CriticalPoweringDown, ProjectorStatus.CriticalOff), "red").bind }
                { badge(ps.map(_._2), Seq(ProjectorStatus.On, ProjectorStatus.PoweringUp), "green").bind }
                { badge(ps.map(_._2), Seq(ProjectorStatus.Connected, ProjectorStatus.Off, ProjectorStatus.DeepSleep), "blue").bind }
                { badge(ps.map(_._2), Seq(ProjectorStatus.Disconnected, ProjectorStatus.Unreachable), "grey").bind }
              </div>
            </div>
          </a>
          <div class="collapsible-body no-padding">
            <ul class="collection">
              { Constants(ps: _*).map{ case (p,r) => projector(p,r).bind }}
            </ul>
          </div>
        </li>
      </ul>
    }

    @dom
    def badge(rs:Seq[Var[ProjectorResponse]], seq:Seq[ProjectorStatus], color:String) = {
      val ss = Constants(rs: _*).withFilter( (p) => {
        val s = p.bind.status
        seq.find(_ == s).isDefined
      }).map(_.bind.status)
      val c = ss.length.bind
      if(c > 0) <span class={"badge new right " + color} data:data-badge-caption="">{c.toString}</span>
      else <span></span>   
    }

    @dom
    def projector(p:ProjectorInfo, r:Binding[ProjectorResponse]) = {
      <li class="collection-item">
        <div class="row">
          <div class="col s2">
            <span class="title">{ p.name }</span> 
            <p class="info grey-text">
              { message(r).bind }
            </p>       
          </div>
          <div class="col s10">
            <!-- <span class="badge new grey" data:data-badge-caption="disconnected"></span> -->
            { status(r).bind }
            { for(cmd <- Constants(p.commands: _*)) yield {
              <a href="#" onclick={ event:Event => event.preventDefault(); event.stopPropagation(); Socket.send(Run(p.id, cmd))} class="btn grey lighten-4 black-text waves-effect waves-teal">
                {cmd.getClass.getSimpleName}
              </a>
            }}
          </div>
        </div>
      </li>
    }

    @dom
    def status(r:Binding[ProjectorResponse]) = {
      { 
        val status = r.bind.status
        status match {
          case ProjectorStatus.Disconnected => 
            <span class="badge new grey" data:data-badge-caption={status.getClass.getSimpleName}></span>
          case ProjectorStatus.Connected => 
            <span class="badge new blue" data:data-badge-caption={status.getClass.getSimpleName}></span>
          case ProjectorStatus.On => 
            <span class="badge new green" data:data-badge-caption={status.getClass.getSimpleName}></span>
          case ProjectorStatus.Off => 
            <span class="badge new blue" data:data-badge-caption={status.getClass.getSimpleName}></span>
          case ProjectorStatus.DeepSleep => 
            <span class="badge new blue" data:data-badge-caption={status.getClass.getSimpleName}></span>
          case ProjectorStatus.CriticalOff => 
            <span class="badge new red" data:data-badge-caption={status.getClass.getSimpleName}></span>
          case _ => 
            <span class="badge new grey" data:data-badge-caption={status.getClass.getSimpleName}></span>
        }
      }
    }

    @dom
    def message(r:Binding[ProjectorResponse]) = {
      <span>{r.bind.message}</span>
    }
    
  }
}