package zio.actors.persistence.journal

import com.typesafe.config.{ Config, ConfigFactory }
import io.getquill.{ CassandraAsyncContext, SnakeCase }

import java.util.UUID
import scala.concurrent.{ ExecutionContext, Future }

final class CassandraClient(db: CassandraAsyncContext[SnakeCase.type])(implicit ec: ExecutionContext) {
  import CassandraClient._
  import db._

  def insertEvent(m: Message): Future[Unit] =
    db.run(quote { (m: Message) =>
      query[Message]
        .insertValue(m)
    }(lift(m)))

  def readEvents(persistence_id: String, shardId: Long): Future[List[Message]] =
    db.run(quote { (persistence_id: String, shardId: Long) =>
      query[Message]
        .filter(_.persistence_id == persistence_id)
        .filter(_.partition_nr == shardId)
        .sortBy(_.sequence_nr)
        .allowFiltering
    }(lift(persistence_id), lift(shardId)))
}

object CassandraClient {
  case class Message(
      persistence_id: String,
      partition_nr: Long,
      sequence_nr: Long,
      timestamp: UUID,
      event: Array[Byte]
  )

  def apply(maybeConfig: Option[Config] = None)(implicit ec: ExecutionContext): CassandraClient = {
    val default = ConfigFactory.parseString(
      """
      |keyspace=event_sourcing
      |preparedStatementCacheSize=100
      |session.contactPoint=127.0.0.1
      |session.queryOptions.consistencyLevel=ONE
      |""".stripMargin
    )
    val dbConfig                                       = maybeConfig.getOrElse(default)
    val context: CassandraAsyncContext[SnakeCase.type] = new CassandraAsyncContext(SnakeCase, dbConfig)
    new CassandraClient(context)
  }
}
