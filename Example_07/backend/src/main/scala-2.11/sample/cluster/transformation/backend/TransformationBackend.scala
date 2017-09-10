package sample.cluster.transformation.backend

import sample.cluster.transformation.{BackendRegistration, TransformationJob, TransformationResult}

import language.postfixOps
import scala.concurrent.duration._
import akka.actor.Actor
import akka.actor.RootActorPath
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberEvent, MemberUp}
import akka.cluster.Member
import akka.cluster.MemberStatus


class TransformationBackend extends Actor {

  val cluster = Cluster(context.system)

  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberEvent])  //在启动Actor时将该节点订阅到集群中
  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive = {
    case TransformationJob(text) => {
      val result = text.toUpperCase // 任务执行得到结果（将字符串转换为大写）
      sender() ! TransformationResult(text.toUpperCase) // 向发送者返回结果
    }
    case state: CurrentClusterState =>
      state.members.filter(_.status == MemberStatus.Up) foreach register // 根据节点状态向集群客户端注册
    case MemberUp(m) => register(m)  // 将刚处于Up状态的节点向集群客户端注册
  }

  def register(member: Member): Unit = {   //将节点注册到集群客户端
    context.actorSelection(RootActorPath(member.address) / "user" / "frontend") !
      BackendRegistration
  }
}