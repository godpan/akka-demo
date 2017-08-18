import akka.actor.Actor

case class JoinEvt(
    id: Long,
    name: String
)

class RemoteActor extends Actor {
  def receive = {
    case j: JoinEvt => sender() ! s"the ${j.name} has Join"
    case msg: String =>
      println(s"RemoteActor received message '$msg'")
      sender ! "Hello from the RemoteActor"
  }
}
