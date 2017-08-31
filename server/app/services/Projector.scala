
package projector

import java.io._
import java.net.Socket
import java.net.SocketTimeoutException
import java.net.InetSocketAddress

import ProjectorCommand._

trait Projector {

  var id = 0
  var name = ""
  var address = ""
  var port = 0
  var status:ProjectorStatus = ProjectorStatus.Disconnected

  var socket:Socket = null
  var in:InputStream = null
  var out:OutputStream = null
  var response:String = ""

  def connect(ip:String){
    address = ip
    connect()
  }

  def connect(){
    if(socket == null || !socket.isConnected() || socket.isClosed()){
      try{
        socket = new Socket();
        socket.setSoTimeout(1000);
        socket.connect(new InetSocketAddress(address, port), 1000);
        out = socket.getOutputStream()
        in = socket.getInputStream()
        status = ProjectorStatus.Connected
        response = ""
      } catch { 
        case e:Exception => 
          status = ProjectorStatus.Unreachable; 
          response = "Connection timed out." //println(s"Connection timed out for $name at $address $port")
      }
    }
  }
  
  def disconnect(){
    if (socket != null && socket.isConnected()) {
      out.close()
      in.close()
      socket.close()
      status = ProjectorStatus.Disconnected
    }
  }

  def writeBytes(arr:Array[Byte]){
    if (socket != null && socket.isConnected() && !socket.isClosed()) {
      out.write(arr, 0, arr.length)
    }
  }

  //XXX getBytes isn't safe in all cases
  def writeString(s:String) = writeBytes(s.getBytes)

  def readResponse():String
  def readResponse(ack:Char, term:Char):String = {
    if (socket != null && socket.isConnected() && !socket.isClosed()) {
      try{
        var reading = false
        var c = in.read()
        while (c != -1) {
          // println(s"$c $response")
          if (c == ack && !reading) {
            reading = true;
            response = "" //c.toChar.toString
          } else if(reading){
            if(c == term) return response
            response += c.toChar.toString
          }
          c = in.read();
        }
      } catch { 
        case e:SocketTimeoutException => println("Socket timed out in readResponse")
        case e:Exception => println("Exception in readResponse")
      }
    }
    response
  }

  def parseResponse()
  def updateStatus(value:String)
  
  // def command(com:ProjectorCommand) = run(com)
  def run(com:ProjectorCommand)

}


class PD extends Projector {
  port = 1025

  def run(command:ProjectorCommand) = command match {
    case On => writeString(":POWR1\r\n")
    case Off => writeString(":POWR0\r\n")
    case Mute => writeString(":PMUT1\r\n")
    case Unmute => writeString(":PMUT0\r\n")
    case GetPowerState => writeString(":POST?\r\n")
    case Command(cmd) => writeString(cmd)
    case _ => //error("unimplemented")
  }

  def readResponse() = readResponse('%','\r')
  def parseResponse(){
    if(response.length == 0) return
    try {
      val ack = response.split("\\s+")(0)
      val command = response.split("\\s+")(1)
      val value = response.split("\\s+").last
      command match {
        case "POST" => updateStatus(value)
        case _ => 
      }
    } catch { case e:Exception => println("parseResponse: parseError") }
  }

  def updateStatus(value:String){
    status = value.toInt match {
      case 0 => ProjectorStatus.DeepSleep
      case 1 => ProjectorStatus.Off
      case 2 => ProjectorStatus.PoweringUp
      case 3 => ProjectorStatus.On
      case 4 => ProjectorStatus.PoweringDown
      case 5 => ProjectorStatus.CriticalPoweringDown
      case 6 => ProjectorStatus.CriticalOff
      case _ => ProjectorStatus.Unknown
    }
  }

}

class Christie extends Projector {
  port = 3002

  def run(command:ProjectorCommand) = command match {
    case On => writeString("(PWR1)")
    case Off => writeString("(PWR0)")
    case Mute => writeString("(PMT1)")
    case Unmute => writeString("(PMT0)")
    case GetPowerState => writeString("(PWR?)")
    case SelectInput(1) => writeString("(SIN 11)")
    case SelectInput(2) => writeString("(SIN 21)")
    case BlendMode(mode) => writeString(s"(EBL+SLCT $mode)")
    case BlendOff => writeString("(EBL+SLCT 0)")
    case BlendOn => writeString("(EBL+SLCT 2)")
    case WarpMode(mode) => writeString(s"(WRP+SLCT $mode)")
    case WarpOff => writeString("(WRP+SLCT 0)")
    case WarpOn => writeString("(WRP+SLCT 2)")

    case SurroundMode => writeString("(WRP+SLCT 0)(EBL+SLCT 0)(SIN 21)")
    case DesktopMode => writeString("(WRP+SLCT 2)(EBL+SLCT 2)(SIN 11)")

    case Command(cmd) => writeString(cmd)

    case _ => //error("unimplemented")
  }

  def readResponse() = readResponse('(',')')
  def parseResponse(){
    if(response.length == 0) return
    try {
      val ack = response.split("\\s+")(0)
      val command = ack.split("!")(0)
      val value = ack.split("!").last
      val message = response.split("\\s+").last
      command match {
        case "PWR" => updateStatus(value)
        case _ => 
      }
    } catch { case e:Exception => println("parseResponse: parseError") }
  }

  def updateStatus(value:String){
    status = value.toInt match {
      case 0 => ProjectorStatus.Off
      case 1 => ProjectorStatus.On
      case 11 => ProjectorStatus.PoweringUp
      case 10 => ProjectorStatus.PoweringDown
      case _ => status
    }
  }

}


class TestProjector extends Projector {
  port = 9999

  override def connect() = {
    response = "connecting.."
    Thread.sleep(1000 + scala.util.Random.nextInt(2000))
    status = ProjectorStatus.Connected
    response = "connected."  
  }
  override def disconnect() = { status = ProjectorStatus.Disconnected; response = "disconnected." }
  override def writeBytes(arr:Array[Byte]){ }
  override def readResponse(ack:Char, term:Char):String = {
    Thread.sleep(1000 + scala.util.Random.nextInt(2000))
    response = "beep boop!"
    response
  }
  def run(command:ProjectorCommand) = {
    command match {
      case On => status = ProjectorStatus.On
      case Off => status = ProjectorStatus.Off
      case Mute => 
      case Unmute => 
      case GetPowerState => 
      case _ => 
    }
    response = "sent command ${command.getClass.getSimpleName}."
  }

  def readResponse() = readResponse('%','\r')
  def parseResponse(){}
  def updateStatus(value:String){}

}