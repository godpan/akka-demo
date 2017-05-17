package futures

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by panguansen on 17/5/17.
  */
object FutureFlatMap extends App{

  val fut1 = Future {
    println("enter task1")
    Thread.sleep(2000)
    1 + 1
  }

  val fut2 = Future {
    println("enter task2")
    Thread.sleep(1000)
    2 + 2
  }

  fut1.flatMap { v1 =>
    fut2.map { v2 =>
      println(s"the result is ${v1 + v2}")
    }
  }
  Thread.sleep(2500)
}
