package futures

import scala.concurrent.Future
import scala.util.Success
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by panguansen on 17/5/17.
  */
object TestScalaFuture extends App {

  val fut = Future {
    Thread.sleep(1000)
    1 + 1
  }

  fut onComplete {
    case Success(r) => println(s"the result is ${r}")
    case _ => println("some Exception")
  }
//  fut onComplete {
//    case Success(r) => println(r+2)
//    case _ => println("some Exception")
//  }

  println("I am working")
  Thread.sleep(2000)

}
