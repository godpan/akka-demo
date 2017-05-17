package futures

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by panguansen on 17/5/17.
  */
object FutureReduce extends App {

  // 理想情况: 列表中的Future全部都成功
  val squares: Seq[Future[Int]] = for (i <- 0 until 10) yield Future { i * i }
  // reduce: 完成 Seq[Future[Int]] => Future[Int] 的转换
  val sumOfSquares: Future[Int] = Future.reduce(squares)(_ + _)

  sumOfSquares foreach {
    case sum => println(s"Sum of squares = $sum")
  }

  val futureWithException = List(Future( 1 + 2), Future(throw new RuntimeException("hello")))
  val reduceFutures = Future.reduce(futureWithException)(_ + _)

  // 不会被执行
  reduceFutures foreach { num => // 只处理成功的情况
    println(s"plus with exception = $num")
  }

  // 被调用执行，其结果是列表中第一个最新计算完成的异常的Future
  reduceFutures.failed foreach { e => // 只处理失败的情况
    println(s"plus with exception = $e")
  }

  Thread.sleep(2000)

}
