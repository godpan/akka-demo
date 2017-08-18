import akka.actor.{ActorSystem, Props}

import scala.io.StdIn

object LocalDemo extends App {

  implicit val system = ActorSystem("LocalDemoSystem")

  //val localActorUsingIdentity = system.actorOf(Props[LocalActorUsingIdentity], name = "LocalActorUsingIdentity")
  val localActor = system.actorOf(Props[LocalActor], name = "LocalActor")

  localActor ! Init
  localActor ! SendNoReturn
//  localActor ! SendHasReturn
  localActor ! SendSerialization

//  StdIn.readLine()
//  system.terminate()
//  StdIn.readLine()

}
