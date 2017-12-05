
package projector

import julienrf.json.derived

sealed trait Protocol
case class RunAll(command:ProjectorCommand) extends Protocol
case class RunGroup(name:String, command:ProjectorCommand) extends Protocol
case class Run(id:Int, command:ProjectorCommand) extends Protocol

case class ProjectorInfo(id:Int, name:String, commands:Seq[ProjectorCommand]) extends Protocol
case class ProjectorGroup(name:String, projectors:Seq[ProjectorInfo]) extends Protocol
case class ProjectorList(groups:Seq[ProjectorGroup]) extends Protocol

case class ProjectorResponse(id:Int, status:ProjectorStatus, message:String) extends Protocol


object Protocol {
  implicit val cformat = derived.oformat[ProjectorCommand]()
  // implicit val rformat = derived.oformat[ProjectorResponse]()
  implicit val sformat = derived.oformat[ProjectorStatus]()
  implicit val pformat = derived.oformat[ProjectorInfo]()
  implicit val pgformat = derived.oformat[ProjectorGroup]()
  implicit val format = derived.oformat[Protocol]()
}
