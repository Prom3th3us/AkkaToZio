package example.complex

import com.devsisters.shardcake.Messenger.Replier
import com.devsisters.shardcake.{ EntityType, Sharding }
import example.complex.GuildEventSourced.GuildState
import zio.actors.{ ActorSystem, Supervisor }
import zio.{ Dequeue, RIO, ZIO }

import scala.util.Try

object GuildESBehavior {
  sealed trait GuildESMessage

  object GuildESMessage {
    case class Join(userId: String, replier: Replier[Try[Set[String]]]) extends GuildESMessage
    case class Leave(userId: String)                                    extends GuildESMessage
  }

  object GuildES extends EntityType[GuildESMessage]("guildES")

  def behavior(
      entityId: String,
      messages: Dequeue[GuildESMessage]
  ): RIO[Sharding with ActorSystem, Nothing] =
    ZIO.serviceWithZIO[ActorSystem](system =>
      ZIO.logInfo(s"Started entity $entityId") *>
        messages.take.flatMap(handleMessage(entityId, system, _)).forever
    )

  def handleMessage(
      entityId: String,
      actorSystem: ActorSystem,
      message: GuildESMessage
  ): RIO[Sharding, Unit] = {
    actorSystem
      .make(
        entityId,
        Supervisor.none,
        GuildState.empty,
        GuildEventSourced.handler(entityId)
      )
      .flatMap { actor =>
        message match {
          case GuildESMessage.Join(userId, replier) =>
            actor
              .?(GuildEventSourced.Join(userId))
              .flatMap { tryMembers =>
                replier.reply(tryMembers)
              }
          case GuildESMessage.Leave(userId) =>
            actor
              .?(GuildEventSourced.Leave(userId))
              .unit
        }
      }
  }
}
