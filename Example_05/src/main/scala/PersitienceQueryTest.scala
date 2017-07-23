import akka.NotUsed
import akka.actor.ActorSystem
import akka.persistence.query.{EventEnvelope, PersistenceQuery}
import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
/**
  * Created by panguansen on 17/7/23.
  */
object PersistenceQueryTest extends App {
  val system = ActorSystem("example-05")
  implicit val mat = ActorMaterializer()(system)
  val queries = PersistenceQuery(system).readJournalFor[LeveldbReadJournal](
    LeveldbReadJournal.Identifier)

  val src: Source[EventEnvelope, NotUsed] =
    queries.eventsByPersistenceId("lottery-actor-2", 0L, Long.MaxValue)

  src.runForeach { event => println("Event: " + event) }
  Thread.sleep(3000)
  system.terminate()

}
