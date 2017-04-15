import akka.actor.SupervisorStrategy.Stop
import akka.actor._
import akka.event.LoggingReceive
import akka.util.Timeout
import akka.pattern.{ ask, pipe }

import scala.concurrent.duration._
/**
  * Created by panguansen on 17/3/26.
  */
object Worker {
  case object Start
  case object Do
  case class Progress(percent: Double)
}


class Listener extends Actor with ActorLogging {
  import Worker._
  // If we don't get any progress within 15 seconds then the service is unavailable
  //假如我们在15秒之内得不到任何反馈即表示当前服务不可用
  context.setReceiveTimeout(15 seconds)

  def receive = {
    case Progress(percent) =>
      //记录当前进度
      log.info("Current progress: {} %", percent)
      if (percent >= 20.0) {
        log.info("That's all, shutting down")
        context.system.terminate()
      }

    case ReceiveTimeout =>
      //15秒之内没有任务反馈，服务不可用，终止程序
      log.error("Shutting down due to unavailable service")
      context.system.terminate()
  }
}

class Worker extends Actor with ActorLogging {
  import Worker._
  import CounterService._
  implicit val askTimeout = Timeout(5 seconds)

  // Stop the CounterService child if it throws ServiceUnavailable
  // 假如接收到抛出的异常信息为服务不可用，停止CounterService的子Actor
  override val supervisorStrategy = OneForOneStrategy() {
    case _: CounterService.ServiceUnavailable => Stop
  }

  // The sender of the initial Start message will continuously be notified
  // about progress
  var progressListener: Option[ActorRef] = None
  val counterService = context.actorOf(Props[CounterService], name = "counter")
  val totalCount = 51
  import context.dispatcher // Use this Actors' Dispatcher as ExecutionContext

  def receive = LoggingReceive {
    case Start if progressListener.isEmpty =>
      progressListener = Some(sender)
      context.system.scheduler.schedule(Duration.Zero, 1 second, self, Do)

    case Do =>
      counterService ! Increment(1)
      counterService ! Increment(1)
      counterService ! Increment(1)

      // 这里可以看成 progressListener.get ！current progress
      // Actor异步获取结果，并根据结果的状态给发送者回应不同的消息
      counterService ? GetCurrentCount map {
        case CurrentCount(_, count) => Progress(100.0 * count / totalCount)
      } pipeTo progressListener.get
  }
}