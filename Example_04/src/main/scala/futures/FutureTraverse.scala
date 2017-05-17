package futures

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by panguansen on 17/5/17.
  */
object FutureTraverse extends App {

  val list = List(1, 2, 3, 4, 5)

  // 这种方式会使用list的size个工作线程，这里是使用了5个线程
  val listFutures: List[Future[Int]] = list map (num => Future(num * num))
  listFutures.foreach { case future =>
    future foreach {
      num => println(s"num in future is $num")
    }
  }

  // 这种方式只会使用一个工作线程
  val futureList: Future[List[Int]] = Future.traverse(list)(num => Future(num * num))
  futureList.foreach{ case listNum =>
    listNum foreach {
      num => println(s"num in list is $num")
    }
  }
}
