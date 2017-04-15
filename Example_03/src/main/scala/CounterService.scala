import akka.actor.SupervisorStrategy.Restart
import akka.actor._
import akka.event.LoggingReceive

import scala.concurrent.duration._

/**
  * Created by panguansen on 17/3/26.
  */

object CounterService {
  case class Increment(n: Int)
  case object GetCurrentCount
  case class CurrentCount(key: String, count: Long)
  class ServiceUnavailable(msg: String) extends RuntimeException(msg)

  private case object Reconnect
}

class CounterService extends Actor {
  import CounterService._
  import Counter._
  import Storage._

  // Restart the storage child when StorageException is thrown.
  // After 3 restarts within 5 seconds it will be stopped.
  // 当storage抛出StorageException时重启storage
  // 如果5秒内3次重启还未成功就停止它
  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 3,
    withinTimeRange = 5 seconds) {
    case _: Storage.StorageException => Restart
  }

  val key = self.path.name
  var storage: Option[ActorRef] = None
  var counter: Option[ActorRef] = None
  var backlog = IndexedSeq.empty[(ActorRef, Any)]
  val MaxBacklog = 10000

  import context.dispatcher // Use this Actors' Dispatcher as ExecutionContext

  override def preStart() {
    initStorage()
  }

  /**
    * The child storage is restarted in case of failure, but after 3 restarts,
    * and still failing it will be stopped. Better to back-off than continuously
    * failing. When it has been stopped we will schedule a Reconnect after a delay.
    * Watch the child so we receive Terminated message when it has been terminated.
    */
  def initStorage() {
    storage = Some(context.watch(context.actorOf(Props[Storage], name = "storage")))
    // Tell the counter, if any, to use the new storage
    counter foreach { _ ! UseStorage(storage) }
    // We need the initial value to be able to operate
    storage.get ! Get(key)
  }

  def receive = LoggingReceive {

    case Entry(k, v) if k == key && counter == None =>
      // Reply from Storage of the initial value, now we can create the Counter
      val c = context.actorOf(Props(classOf[Counter], key, v))
      counter = Some(c)
      // Tell the counter to use current storage
      c ! UseStorage(storage)
      // and send the buffered backlog to the counter
      for ((replyTo, msg) <- backlog) c.tell(msg, sender = replyTo)
      backlog = IndexedSeq.empty

    case msg @ Increment(n)    => forwardOrPlaceInBacklog(msg)

    case msg @ GetCurrentCount => forwardOrPlaceInBacklog(msg)

    case Terminated(actorRef) if Some(actorRef) == storage =>
      // After 3 restarts the storage child is stopped.
      // We receive Terminated because we watch the child, see initStorage.
      storage = None
      // Tell the counter that there is no storage for the moment
      counter foreach { _ ! UseStorage(None) }
      // Try to re-establish storage after while
      context.system.scheduler.scheduleOnce(10 seconds, self, Reconnect)

    case Reconnect =>
      // Re-establish storage after the scheduled delay
      initStorage()
  }

  def forwardOrPlaceInBacklog(msg: Any) {
    // We need the initial value from storage before we can start delegate to
    // the counter. Before that we place the messages in a backlog, to be sent
    // to the counter when it is initialized.
    counter match {
      case Some(c) => c forward msg
      case None =>
        if (backlog.size >= MaxBacklog)
          throw new ServiceUnavailable(
            "CounterService not available, lack of initial value")
        backlog :+= (sender() -> msg)
    }
  }

}