package futures

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.async.Async.{async, await}
/**
  * Created by panguansen on 17/6/27.
  */
object FutureAwait extends App {
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

  val v1 = async {
     await(fut1) + await(fut2)
  }

  v1 foreach {
    case r => println(s"the result1 is ${v1}")
  }

  //顺序执行
  Thread.sleep(2500)
  val v2 = async {
    await(Future {
      println("enter task1")
      Thread.sleep(2000)
      1 + 1
    }) + await(Future {
      println("enter task2")
      Thread.sleep(1000)
      2 + 2
    })
  }
  println(s"the result2 is ${v2}")
  Thread.sleep(3500)
}
