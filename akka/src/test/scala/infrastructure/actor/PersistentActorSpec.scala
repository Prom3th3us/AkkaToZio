package infrastructure.actor

import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import infrastructure.actor.ExampleActor._
import org.scalatest.BeforeAndAfterAll

import scala.sys.process._
import scala.util.{Failure, Success, Try}

class PersistentActorSpec extends ActorTestSuite with BeforeAndAfterAll {

  override protected def beforeAll(): Unit = {
    val script = "cassandra.setup.sh"
    Seq(
      Try(s"""bash src/main/resources/$script""".!!),
      Try(s"""bash akka/src/main/resources/$script""".!!)
    )

    super.beforeAll()
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    val script = "cassandra.cleanup.sh"
    Seq(
      Try(s"""bash src/main/resources/$script""".!!),
      Try(s"""bash akka/src/main/resources/$script""".!!)
    )
  }

  "Persistent actors should be recover state after complete system shutdown" in {

    {
      implicit val system = ActorSystem.start
      implicit lazy val sharding = ClusterSharding.apply(system)
      val sharded = ShardedActor[Increment](
        uniqueName = "PersistentCounterActor",
        behavior = PersistentCounterActor.apply
      )
      for {
        a <- sharded.ask("A")(Increment)
        b <- sharded.ask("B")(Increment)
        done <- ActorSystem.stop(system)
      } yield {
        Seq(a, b) should be(Seq(1, 1))
      }
    }

    Thread.sleep(5000)

    {
      implicit val system = ActorSystem.start
      implicit lazy val sharding = ClusterSharding.apply(system)
      val sharded = ShardedActor[Increment](
        uniqueName = "PersistentCounterActor",
        behavior = PersistentCounterActor.apply
      )
      for {
        a <- sharded.ask("A")(Increment)
        b <- sharded.ask("B")(Increment)
        done <- ActorSystem.stop(system)
      } yield Seq(a, b) should be(Seq(2, 2))
    }

  }
}
