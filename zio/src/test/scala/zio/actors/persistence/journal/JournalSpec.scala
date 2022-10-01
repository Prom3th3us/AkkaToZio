package zio.actors.persistence.journal

import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import zio.Unsafe
import zio.actors.persistence.PersistenceId

class JournalSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  override protected def beforeAll(): Unit = {
    import scala.sys.process._
    val script = "cassandra.setup.sh"
    s"""bash scripts/$script""".!!
    super.beforeAll()
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    import scala.sys.process._
    val script = "cassandra.cleanup.sh"
    s"""bash scripts/$script""".!!
    ()
  }

  "Persistent actors should be recover state after complete system shutdown" in {

    case class Incremented(amount: Int)

    val db            = CassandraClient()
    val journal       = new zio.actors.persistence.journal.CassandraJournal[Incremented](db)
    val persistenceId = PersistenceId("PersistentCounterActor-A")

    val runtime = zio.Runtime.default
    Unsafe.unsafe { implicit unsafe =>
      runtime.unsafe
        .runToFuture(
          for {
            _      <- journal.persistEvent(persistenceId, Incremented(100))
            events <- journal.getEvents(persistenceId)
          } yield events
        )
        .future
        .map(_ contains Incremented(100) should be(true))
    }
  }
}
