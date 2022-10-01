package example.complex

import com.devsisters.shardcake.Messenger.Replier
import com.devsisters.shardcake.{ EntityType, Sharding }
import example.complex.GuildEventSourced.GuildMessage
import zio.actors.ActorRef
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
  ): RIO[Sharding with ActorRef[GuildMessage], Nothing] =
    ZIO.serviceWithZIO[ActorRef[GuildMessage]](actor =>
      ZIO.logInfo(s"Started entity $entityId") *>
        messages.take.flatMap(handleMessage(entityId, actor, _)).forever
    )

  def handleMessage(
      entityId: String,
      actor: ActorRef[GuildMessage],
      message: GuildESMessage
  ): RIO[Sharding, Unit] =
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
