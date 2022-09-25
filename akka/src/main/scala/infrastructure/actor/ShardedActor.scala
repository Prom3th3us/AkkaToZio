package infrastructure.actor

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityTypeKey}
import akka.util.Timeout

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

case class ShardedActor[Command](
    uniqueName: String,
    behavior: String => Behavior[Command]
)(implicit
    sharding: ClusterSharding,
    system: ActorSystem[Nothing],
    timeout: Timeout = Timeout(20 seconds)
) {

  val shardActor: ActorRef[ShardingEnvelope[Command]] = {
    val entityTypeKey: EntityTypeKey[Command] =
      EntityTypeKey apply uniqueName
    sharding.init(Entity(entityTypeKey)(createBehavior = { context =>
      behavior(context.entityId)
    }))
  }

  import akka.actor.typed.scaladsl.AskPattern._

  def tell(id: String)(command: Command) =
    shardActor.tell(ShardingEnvelope(id, command))

  def ask[Res](id: String)(replyTo: ActorRef[Res] => Command) =
    shardActor.ask[Res](
      replyTo.andThen(command => ShardingEnvelope(id, command))
    )

}
