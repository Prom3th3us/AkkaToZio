import zio.{ Scope, ZIO, ZIOAppArgs }
import zio.actors.persistence.PersistenceId
import io.circe._
import io.circe.generic.semiauto._

object Example extends zio.ZIOAppDefault {

  case class Incremented(amount: Int)
  implicit val encoder: Encoder[Incremented] = deriveEncoder
  implicit val decoder: Decoder[Incremented] = deriveDecoder

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    val journal = new zio.actors.persistence.journal.CassandraJournal[Incremented]()
    val persistenceId = PersistenceId("PersistentCounterActor-A")
    for {
      _      <- journal.persistEvent(persistenceId, Incremented(100))
      _      <- journal.persistEvent(persistenceId, Incremented(200))
      events <- journal.getEvents(persistenceId)
    } yield events.foreach(println)

  }
}
