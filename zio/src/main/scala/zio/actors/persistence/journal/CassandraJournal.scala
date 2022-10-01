package zio.actors.persistence.journal

import io.circe.{ Decoder, Encoder }
import io.getquill.{ CassandraAsyncContext, SnakeCase }
import zio.Task
import zio.actors.persistence.PersistenceId.PersistenceId

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import com.datastax.driver.core.utils.UUIDs
import com.typesafe.config.ConfigFactory

object CassandraJournal {

  private[CassandraJournal] object low_level {
    lazy val config = ConfigFactory.parseString("""
        |keyspace=event_sourcing
        |preparedStatementCacheSize=100
        |session.contactPoint=127.0.0.1
        |session.queryOptions.consistencyLevel=ONE
        |""".stripMargin)
    lazy val db = new CassandraAsyncContext(SnakeCase, config)
    import db._

    case class Messages(
        persistence_id: String,
        partition_nr: Long,
        sequence_nr: Long,
        timestamp: UUID,
        event: Array[Byte]
    )

    val insertEvent = (m: Messages) =>
      db.run(quote { (m: Messages) =>
        query[Messages]
          .insertValue(m)
      }(lift(m)))

    val readEvents = (persistence_id: String, shardId: Long) =>
      db.run(quote { (persistence_id: String, shardId: Long) =>
        query[Messages]
          .filter(_.persistence_id == persistence_id)
          .filter(_.partition_nr == shardId)
          .sortBy(_.sequence_nr)
          .allowFiltering
      }(lift(persistence_id), lift(shardId)))
  }

  object EventSourcing {
    import low_level._

    def journal[A: io.circe.Encoder](persistence_id: String, shardId: Long, event: A): Future[Unit] = {
      import io.circe.syntax._
      val message = Messages(
        persistence_id,
        event = event.asJson.toString.getBytes,
        sequence_nr = 100,
        timestamp = UUIDs.timeBased(),
        partition_nr = shardId
      )
      insertEvent(message)
    }

    def recovery[A: io.circe.Decoder](persistence_id: String, shardId: Long): Future[List[A]] = {
      readEvents(persistence_id, shardId)
        .map(messages =>
          messages
            .map(message => new String(message.event))
            .map { (event: String) =>
              import io.circe.parser.decode
              decode[A](event)
            }
            .collect { case Right(event) =>
              event
            }
        )
    }
  }
}

final class CassandraJournal[Ev: Decoder: Encoder]() extends Journal[Ev] {

  import CassandraJournal._

  object Sharding {
    def shardId: String => Long = _ => 0L // _.hashCode % 3
  }

  override def persistEvent(persistenceId: PersistenceId, event: Ev): Task[Unit] =
    zio.ZIO.fromFuture { _ =>
      val entityId = persistenceId.value
      val shardId  = Sharding.shardId(entityId)
      EventSourcing.journal[Ev](persistenceId.value, shardId, event)
    }

  override def getEvents(persistenceId: PersistenceId): Task[Seq[Ev]] = {
    zio.ZIO.fromFuture { _ =>
      val entityId = persistenceId.value
      val shardId  = Sharding.shardId(entityId)
      EventSourcing.recovery[Ev](entityId, shardId)
    }
  }
}
