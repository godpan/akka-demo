package guardian

import akka.actor._
import akka.actor.OneForOneStrategy
import akka.actor.SupervisorStrategy._
import akka.event.Logging

import scala.concurrent.duration._

/**
  * Created by panguansen on 17/3/26.
  */
class Supervisor extends Actor {

  //监管下属，根据下属抛出的异常进行相应的处理
  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: ArithmeticException => Resume
      case _: NullPointerException => Restart
      case _: IllegalArgumentException => Stop
      case _: Exception => Escalate
    }
  var childIndex = 0 //用于标示下属Actor的序号

  def receive = {
    case p: Props =>
      childIndex += 1
      //返回一个Child Actor的引用，所以Supervisor Actor是Child Actor的监管者
      sender() ! context.actorOf(p,s"child${childIndex}")
  }

  // override default to kill all children during restart
  //  override def preRestart(cause: Throwable, msg: Option[Any]) {}
}

class Child extends Actor {
  val log = Logging(context.system, this)
  var state = 0
  def receive = {
    case ex: Exception => throw ex //抛出相应的异常
    case x: Int => state = x //改变自身状态
    case s: Command if s.content == "get" =>
      log.info(s"the ${s.self} state is ${state}")
      sender() ! state //返回自身状态
  }
}

case class Command(  //相应命令
    content: String,
    self: String
)
