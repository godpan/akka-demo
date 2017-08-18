import akka.actor._
import akka.actor.Actor.Receive
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by panguansen on 17/8/14.
  */
case object Init
case object SendNoReturn
case object SendHasReturn
case object SendSerialization

case class JoinEvt(
    id: Long,
    name: String
)

class LocalActor extends Actor {

  val path =
    ConfigFactory.defaultApplication().getString("remote.actor.name.test")
  implicit val timeout = Timeout(4.seconds)

  val remoteActor = context.actorSelection(path)

  def receive: Receive = {
    case Init => "init local actor"
    case SendNoReturn =>
      for {
        remoteActor <- remoteActor.resolveOne()
        r = remoteActor ! "kkkk"
      } yield r
    case SendHasReturn =>
      for {
        r <- remoteActor.ask("hello remote actor")
      } yield println(r)

    case SendSerialization =>
      for {
        r <- remoteActor.ask(JoinEvt(1L,"godpan"))
      } yield println(r)
  }

}
