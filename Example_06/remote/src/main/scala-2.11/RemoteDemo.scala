import akka.actor.{Props, ActorSystem}

import scala.io.StdIn

object RemoteDemo extends App  {
  val system = ActorSystem("RemoteDemoSystem")
  val remoteActor = system.actorOf(Props[RemoteActor], name = "RemoteActor")
  remoteActor ! "The RemoteActor is alive"
  StdIn.readLine()
  system.terminate()
  StdIn.readLine()
}
