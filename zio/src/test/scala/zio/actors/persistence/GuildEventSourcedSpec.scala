package zio.actors.persistence

import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import zio.actors._
import zio.actors.{ ActorSystem, Context, Supervisor }
import zio.actors.persistence._
import zio.UIO
import zio._
import zio.Unsafe

import java.io.File

class GuildEventSourcedSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {
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
    import example.complex.GuildEventSourced._
    val io = for {
      actorSystem <- ActorSystem("testSystem2", Some(new File("zio/src/main/resources/application.conf")))
      // Scenario 1
      user1 <- Random.nextUUID.map(_.toString)
      user2 <- Random.nextUUID.map(_.toString)
      persistenceId1 = "guild1"
      guild1 <- actorSystem.make[Any, GuildState, GuildMessage](
        persistenceId1,
        Supervisor.none,
        GuildState.empty,
        handler(persistenceId1)
      )
      _        <- guild1 ? Join(user1)
      _        <- guild1 ? Join(user2)
      members1 <- guild1 ? Get
      _        <- Console.printLine(s"members1: $members1")
      _        <- guild1.stop
      // Scenario 2
      persistenceId2 = "guild2"
      guild2 <- actorSystem.make[Any, GuildState, GuildMessage](
        persistenceId2,
        Supervisor.none,
        GuildState.empty,
        handler(persistenceId2)
      )
      _        <- guild2 ? Join(user1)
      _        <- guild2 ? Join(user2)
      members2 <- guild2 ? Get
      _        <- Console.printLine(s"members2: $members2")
      _        <- guild2.stop
      // Scenario 3
      //   guild1      <- actorSystem.make("guild1", Supervisor.none, 0, ESCounterHandler)
      //   user3       <- Random.nextUUID.map(_.toString)
      //   user4       <- Random.nextUUID.map(_.toString)
      //   _           <- guild1 ? Join(user3)
      //   _           <- guild1 ? Join(user4)
      //   members1    <- guild1 ? Get
      //   _           <- guild1.stop
    } yield members1 == members2

    val runtime = zio.Runtime.default
    Unsafe.unsafe { implicit unsafe =>
      runtime.unsafe
        .runToFuture(io)
        .future
        .map(_ should be(true))
    }
  }
}
