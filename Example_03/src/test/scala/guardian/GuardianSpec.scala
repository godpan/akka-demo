package guardian

import akka.actor.{ActorRef, ActorSystem, InternalActorRef, Props, Terminated}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{Matchers, WordSpecLike}

/**
  * Created by panguansen on 17/4/7.
  */
class GuardianSpec(_system: ActorSystem)
    extends TestKit(_system)
    with WordSpecLike
    with Matchers
    with ImplicitSender {

  def this() = this(ActorSystem("GuardianSpec"))

  "A supervisor" must {

    "apply the chosen strategy for its child" in {
      // code here
      val supervisor = system.actorOf(Props[Supervisor], "supervisor")

      supervisor ! Props[Child]
      val child = expectMsgType[ActorRef] // 从 TestKit 的 testActor 中获取回应

      //TestOne
      child ! 42 // 将状态设为 42
      child ! Command("get",child.path.name)
      expectMsg(42)

      //TestTwo
      child ! new ArithmeticException // crash it
      child ! Command("get",child.path.name)
      expectMsg(42)

      //TestThree
      child ! new NullPointerException // crash it harder
      child ! "get"
      expectMsg(0)


      //TestFour
      supervisor ! Props[Child] // create new child
      val child2 = expectMsgType[ActorRef]
      child2 ! 100 // 将状态设为 100
      watch(child) // have testActor watch “child”
      child ! new IllegalArgumentException // break it
      expectMsgPF() {
        case Terminated(`child`) => (println("the child stop"))
      }
      child2 ! Command("get",child2.path.name)
      expectMsg(100)


      //TestFive
      watch(child2)
      child2 ! Command("get",child2.path.name) // verify it is alive
      expectMsg(100)
      supervisor ! Props[Child] // create new child
      val child3 = expectMsgType[ActorRef]
      child2 ! new Exception("CRASH") // escalate failure
//      expectMsgPF() {
//        case t @ Terminated(`child2`) if t.existenceConfirmed => (
//          println("the child2 stop")
//          )
//      }
      child3 ! Command("get",child.path.name)
      expectMsg(0)
    }
  }
}
