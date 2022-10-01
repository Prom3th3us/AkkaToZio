import zio.actors.persistence.PersistenceId
import zio.actors.persistence.journal.CassandraClient
import zio.{ Scope, ZIO, ZIOAppArgs }

import scala.concurrent.ExecutionContext.Implicits.global

object Example extends zio.ZIOAppDefault {

  case class Incremented(amount: Int)

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    val db            = CassandraClient()
    val journal       = new zio.actors.persistence.journal.CassandraJournal[Incremented](db)
    val persistenceId = PersistenceId("PersistentCounterActor-A")
    for {
      _      <- journal.persistEvent(persistenceId, Incremented(100))
      _      <- journal.persistEvent(persistenceId, Incremented(200))
      events <- journal.getEvents(persistenceId)
    } yield events.foreach(println)
  }
}
