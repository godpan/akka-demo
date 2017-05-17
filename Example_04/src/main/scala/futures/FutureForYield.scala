package futures

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by panguansen on 17/5/17.
  */
object FutureForYield extends App{

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

  //异步执行
  for {
    v1 <- fut1
    v2 <- fut2
  } yield println(s"the result is ${v1 + v2}")

  //顺序执行
  Thread.sleep(2500)
  for {
    v1 <- Future {
      println("enter task1")
      Thread.sleep(2000)
      1 + 1
    }
    v2 <- Future {
      println("enter task2")
      Thread.sleep(1000)
      2 + 2
    }
  } yield println(s"the result is ${v1 + v2}")
  Thread.sleep(3500)
}
