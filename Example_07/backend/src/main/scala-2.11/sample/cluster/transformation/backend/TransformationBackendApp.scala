package sample.cluster.transformation.backend

import language.postfixOps
import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.actor.Props
import com.typesafe.config.ConfigFactory

object TransformationBackendApp {
  def main(args: Array[String]): Unit = {
    // Override the configuration of the port when specified as program argument
    val port = if (args.isEmpty) "2553" else args(0)
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
      withFallback(ConfigFactory.load())

    val system = ActorSystem("ClusterSystem", config)
    system.actorOf(Props[TransformationBackend], name = "backend")
  }
}