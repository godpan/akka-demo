import akka.actor._
import akka.event.Logging
import akka.pattern.{ask, pipe}
import akka.util.Timeout

import scala.concurrent.duration._

/**
  * Created by panguansen on 17/3/28.
  */
class BossActor extends Actor {
  val log = Logging(context.system, this)
  implicit val askTimeout = Timeout(5 seconds)
  import context.dispatcher
  var taskCount = 0
  def receive: Receive = {
    case b: Business =>
      log.info("I must to do some thing,go,go,go!")
      println(self.path.address)
      //创建Actor得到ActorRef的另一种方式，利用ActorContext.actorOf
      val managerActors = (1 to 3).map(i =>
        context.actorOf(Props[ManagerActor], s"manager${i}")) //这里我们召唤3个主管
      //告诉他们开会商量大计划
      managerActors foreach {
        _ ? Meeting("Meeting to discuss big plans") map {
          case c: Confirm =>
            //为什么这里可以知道父级Actor的信息？
            //熟悉树结构的同学应该知道每个节点有且只有一个父节点（根节点除外）
            log.info(c.actorPath.parent.toString)
            //根据Actor路径查找已经存在的Actor获得ActorRef
            //这里c.actorPath是绝对路径,你也可以根据相对路径得到相应的ActorRef
            val manager = context.actorSelection(c.actorPath)
            manager ! DoAction("Do thing")
        }
      }
    case d: Done => {
      taskCount += 1
      println(taskCount)
      if (taskCount == 3) {
        log.info("the project is done, we will earn much money")
        context.system.terminate()
      }
    }
  }
}
class ManagerActor extends Actor {
  val log = Logging(context.system, this)
  def receive: Receive = {
    case m: Meeting =>
      sender() ! Confirm("I have receive command", self.path)
    case d: DoAction =>
      val workerActor = context.actorOf(Props[WorkerActor], "worker")
      workerActor forward d
  }
}

class WorkerActor extends Actor {
  val log = Logging(context.system, this)
  def receive: Receive = {
    case d: DoAction =>
      log.info("I have receive task")
      sender() ! Done("I hava done work")
  }
}

trait Message {
  val content: String
}
case class Business(content: String) extends Message {}
case class Meeting(content: String) extends Message {}
case class Confirm(content: String, actorPath: ActorPath) extends Message {}
case class DoAction(content: String) extends Message {}
case class Done(content: String) extends Message {}

object Example_02 extends App {

  val actorSystem = ActorSystem("company-system") //首先我们创建一家公司
  //创建Actor得到ActorRef的一种方式，利用ActorSystem.actorOf
  val bossActor = actorSystem.actorOf(Props[BossActor], "boss") //公司有一个Boss
  bossActor ! Business("Fitness industry has great prospects") //从市场上观察到健身行业将会有很大的前景

}
