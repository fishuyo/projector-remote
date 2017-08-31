package projector

import scala.scalajs.js
import scala.scalajs.js.annotation._
import scala.scalajs.js.timers.setInterval
import org.scalajs.dom.document
import org.scalajs.dom.console
import org.scalajs.dom.raw._

import org.querki.jquery._

import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding.{Var, Vars}
import com.thoughtworks.binding.dom


object Main extends js.JSApp {
  
  def main(): Unit = {
    println("from scalajs Main hi!")
    
    dom.render(document.body, render)

    Socket.init()
    setInterval(1000){ Socket.send("keepalive") }

  }
  
  @dom
  def render = {
    // <header>{ renderHeader.bind }</header>
    <main>{ renderMain.bind }</main>
    <footer>{ renderFooter.bind }</footer>
  }

  @dom
  def renderHeader = {
    <i></i>
  }

  @dom
  def renderMain = {
    <div class="container">
      { Projectors.views.projectorList.bind }
    </div>
  }

  @dom
  def renderFooter = {
    <div class="page-footer">
      <div class="container">
        <div class="row">
          <div class="col l6 s12">
          </div>
          <div class="col l4 offset-l2 s12">
          </div>
        </div>
      </div>
      <div class="footer-copyright">
        <div class="container">
        AlloSphere Projector Remote
        <a class="grey-text text-lighten-4 right" href="#!">More Services</a>
        </div>
      </div>
    </div> 
  }


  
}