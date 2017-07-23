import java.util.concurrent.{ExecutorService, Executors}

import akka.actor.Actor.Receive
import akka.actor.{ActorLogging, ActorRef, ActorSystem, Props}
import akka.persistence._
import akka.pattern.ask
import akka.util.Timeout

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by panguansen on 17/7/19.
  */
case class LotteryCmd(
    userId: Long,
    username: String,
    email: String
)
case class LuckyEvent(  //抽奖成功事件
    userId: Long,
    luckyMoney: Int
)
case class FailureEvent(  //抽奖失败事件
    userId: Long,
    reason: String
)
case class Lottery(
    totalAmount: Int,  //红包总金额
    remainAmount: Int  //剩余红包金额
) {
  def update(luckyMoney: Int) = {
    copy(
      remainAmount = remainAmount - luckyMoney
    )
  }
}
class LotteryActor(initState: Lottery) extends PersistentActor with ActorLogging{
  override def persistenceId: String = "lottery-actor-1"

  var state = initState  //初始化Actor的状态

  override def receiveRecover: Receive = {
    case event: LuckyEvent =>
      updateState(event)  //恢复Actor时根据持久化的事件恢复Actor状态
    case SnapshotOffer(_, snapshot: Lottery) =>
      log.info(s"Recover actor state from snapshot and the snapshot is ${snapshot}")
      state = snapshot //利用快照恢复Actor的状态
    case RecoveryCompleted => log.info("the actor recover completed")
  }

  def updateState(le: LuckyEvent) =
    state = state.update(le.luckyMoney)  //更新自身状态

  override def receiveCommand: Receive = {
    case lc: LotteryCmd =>
      doLottery(lc) match {     //进行抽奖，并得到抽奖结果，根据结果做出不同的处理
        case le: LuckyEvent =>  //抽到随机红包
          persist(le) { event =>
            updateState(event)
            increaseEvtCountAndSnapshot()
            sender() ! event
          }
        case fe: FailureEvent =>  //红包已经抽完
          sender() ! fe
      }
    case "saveSnapshot" =>  // 接收存储快照命令执行存储快照操作
      saveSnapshot(state)
    case SaveSnapshotSuccess(metadata) =>  ???  //你可以在快照存储成功后做一些操作，比如删除之前的快照等
  }


  private def increaseEvtCountAndSnapshot() = {
    val snapShotInterval = 5
    if (lastSequenceNr % snapShotInterval == 0 && lastSequenceNr != 0) {  //当有持久化5个事件后我们便存储一次当前Actor状态的快照
      self ! "saveSnapshot"
    }
  }


  def doLottery(lc: LotteryCmd) = {  //抽奖逻辑具体实现
    if (state.remainAmount > 0) {
      val luckyMoney = scala.util.Random.nextInt(state.remainAmount) + 1
      LuckyEvent(lc.userId, luckyMoney)
    }
    else {
      FailureEvent(lc.userId, "下次早点来，红包已被抽完咯！")
    }
  }
}


class LotteryActorN(initState: Lottery) extends PersistentActor with ActorLogging{
  override def persistenceId: String = "lottery-actor-2"

  var state = initState  //初始化Actor的状态

  override def receiveRecover: Receive = {
    case event: LuckyEvent =>
      updateState(event)  //恢复Actor时根据持久化的事件恢复Actor状态
    case SnapshotOffer(_, snapshot: Lottery) =>
      log.info(s"Recover actor state from snapshot and the snapshot is ${snapshot}")
      state = snapshot //利用快照恢复Actor的状态
    case RecoveryCompleted => log.info("the actor recover completed")
  }

  def updateState(le: LuckyEvent) =
    state = state.update(le.luckyMoney)  //更新自身状态

  var lotteryQueue : ArrayBuffer[(LotteryCmd, ActorRef)] = ArrayBuffer()

  context.system.scheduler  //定时器，定时触发抽奖逻辑
    .schedule(
      0.milliseconds,
      100.milliseconds,
      new Runnable {
        def run = {
          self ! "doLottery"
        }
      }
    )

  override def receiveCommand: Receive = {
    case lc: LotteryCmd =>
      lotteryQueue = lotteryQueue :+ (lc, sender())  //参与信息加入抽奖队列
      println(s"the lotteryQueue size is ${lotteryQueue.size}")
      if (lotteryQueue.size > 5)  //当参与人数有5个时触发抽奖
        joinN(lotteryQueue)
    case "doLottery" =>
      if (lotteryQueue.size > 0)
        joinN(lotteryQueue)
    case "saveSnapshot" =>  // 接收存储快照命令执行存储快照操作
      saveSnapshot(state)
    case SaveSnapshotSuccess(metadata) =>  ???  //你可以在快照存储成功后做一些操作，比如删除之前的快照等
  }

  private def joinN(lotteryQueue: ArrayBuffer[(LotteryCmd, ActorRef)]) = {  //批量处理抽奖结果
    val rs = doLotteryN(lotteryQueue)
    val success = rs.collect {  //得到其中中奖的相应信息
      case (event: LuckyEvent, ref: ActorRef) =>
        event -> ref
    }.toMap
    println(success)
    val failure = rs.collect {  //得到其中未中奖的相应信息
      case (event: FailureEvent, ref: ActorRef) => event -> ref
    }
    persistAll(success.keys.toIndexedSeq) {  //批量持久化中奖用户事件
      case event =>  println(event)
        updateState(event)
        increaseEvtCountAndSnapshot()
        success(event) ! event
    }
    failure.foreach {
      case (event, ref) => ref ! event
    }
    this.lotteryQueue.clear()  //情况参与队列
  }


  private def increaseEvtCountAndSnapshot() = {
    val snapShotInterval = 5
    if (lastSequenceNr % snapShotInterval == 0 && lastSequenceNr != 0) {  //当有持久化5个事件后我们便存储一次当前Actor状态的快照
      self ! "saveSnapshot"
    }
  }

  private def doLotteryN(lotteryQueue: ArrayBuffer[(LotteryCmd, ActorRef)]) = {  //抽奖逻辑具体实现
    var remainAmount = state.remainAmount
    lotteryQueue.map(lq =>
      if (remainAmount > 0) {
        val luckyMoney = scala.util.Random.nextInt(remainAmount) + 1
        remainAmount = remainAmount - luckyMoney
        (LuckyEvent(lq._1.userId, luckyMoney),lq._2)
      }
      else {
        (FailureEvent(lq._1.userId, "下次早点来，红包已被抽完咯！"),lq._2)
      }
    )
  }
}


object PersistenceTest extends App {
  val lottery = Lottery(10000,10000)
  val system = ActorSystem("example-05")
//  val lotteryActor = system.actorOf(Props(new LotteryActor(lottery)), "LotteryActor-1")  //创建抽奖Actor
  val lotteryActor = system.actorOf(Props(new LotteryActorN(lottery)), "LotteryActor-2")  //创建抽奖Actor
  val pool: ExecutorService = Executors.newFixedThreadPool(10)
  val r = (1 to 100).map(i =>
    new LotteryRun(lotteryActor, LotteryCmd(i.toLong,"godpan","xx@gmail.com"))  //创建100个抽奖请求
  )
  r.map(pool.execute(_))  //使用线程池来发起抽奖请求，模拟同时多人参加
  Thread.sleep(5000)
  pool.shutdown()
  system.terminate()
}

class LotteryRun(lotteryActor: ActorRef, lotteryCmd: LotteryCmd) extends Runnable { //抽奖请求
implicit val timeout = Timeout(3.seconds)
  def run: Unit = {
    for {
      fut <- lotteryActor ? lotteryCmd
    } yield fut match {  //根据不同事件显示不同的抽奖结果
      case le: LuckyEvent => println(s"恭喜用户${le.userId}抽到了${le.luckyMoney}元红包")
      case fe: FailureEvent =>  println(fe.reason)
      case _ => println("系统错误，请重新抽取")
    }
  }
}
