package infrastructure.actor

import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import infrastructure.actor.ExampleActor._

class ShardedActorSpec extends ActorTestSuite {
  implicit val system = ActorSystem.start
  implicit lazy val sharding = ClusterSharding.apply(system)

  "Sharded actors should be segregated by id" in {

    val sharded = ShardedActor[Increment](
      uniqueName = "CounterActor",
      behavior = id => CounterActor.empty
    )

    for {
      a <- sharded.ask("A")(Increment)
      b <- sharded.ask("B")(Increment)
      c <- sharded.ask("C")(Increment)
      d <- sharded.ask("C")(Increment)
      e <- sharded.ask("C")(Increment)
    } yield Seq(a, b, c, d, e) should be(Seq(0, 0, 0, 1, 2))

  }
}
