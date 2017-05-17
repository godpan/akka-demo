package futures

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by panguansen on 17/5/17.
  */
object FutureSequence extends App {

  // 理想情况: 列表中的Future全部都成功
  val listF: Seq[Future[Int]] = for (i <- 0 until 10) yield Future { i * i }
  // sequence: 完成 Seq[Future[Int]] => Future[Seq[Int]] 的转换
  val fList: Future[Seq[Int]] = Future.sequence(listF)

  for {
    r <- fList
  } yield println(r)

  Thread.sleep(1000)

}
