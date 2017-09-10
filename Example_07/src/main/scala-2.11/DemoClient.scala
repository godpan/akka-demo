import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import sample.cluster.transformation.backend.TransformationBackendApp
import sample.cluster.transformation.frontend.TransformationFrontendApp

import scala.io.StdIn
import scala.concurrent.duration._


object DemoClient {
  def main(args : Array[String]) {

    TransformationFrontendApp.main(Seq("2551").toArray)  //启动集群客户端
    TransformationBackendApp.main(Seq("8001").toArray)   //启动三个后台节点
    TransformationBackendApp.main(Seq("8002").toArray)
    TransformationBackendApp.main(Seq("8003").toArray)

    val system = ActorSystem("OTHERSYSTEM")
    val clientJobTransformationSendingActor =
      system.actorOf(Props[ClientJobTransformationSendingActor],
        name = "clientJobTransformationSendingActor")

    val counter = new AtomicInteger
    import system.dispatcher
    system.scheduler.schedule(2.seconds, 2.seconds) {   //定时发送任务
      clientJobTransformationSendingActor ! Send(counter.incrementAndGet())
    }
    StdIn.readLine()
    system.terminate()
  }
}




