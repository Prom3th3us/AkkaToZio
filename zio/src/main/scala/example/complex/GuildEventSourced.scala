package example.complex

import zio.actors._
import zio.actors.{ ActorSystem, Context, Supervisor }
import zio.actors.persistence._
import zio.{ UIO, ZIO }
import scala.util.{ Try, Success, Failure }

object GuildEventSourced {
  sealed trait GuildMessage[+_]
  case class Join(userId: String)  extends GuildMessage[Try[Set[String]]]
  case class Leave(userId: String) extends GuildMessage[Unit]
  case object Get                  extends GuildMessage[GuildState]

  sealed trait GuildEvent
  case class JoinedEvent(userId: String) extends GuildEvent
  case class LeftEvent(userId: String)   extends GuildEvent

  case class GuildState(members: Set[String])
  object GuildState {
    def empty: GuildState = GuildState(members = Set.empty)
  }

  def handler(persistenceId: String) =
    new EventSourcedStateful[Any, GuildState, GuildMessage, GuildEvent](
      PersistenceId(persistenceId)
    ) {
      override def receive[A](
          state: GuildState,
          msg: GuildMessage[A],
          context: Context
      ): UIO[(Command[GuildEvent], GuildState => A)] =
        msg match {
          case Join(userId) =>
            if (state.members.size >= 5) {
              ZIO.succeed((Command.ignore, _ => Failure(new Exception("Guild is already full!"))))
            } else {
              ZIO.succeed((Command.persist(JoinedEvent(userId)), st => Success(st.members)))
            }
          case Leave(userId) => ZIO.succeed((Command.persist(LeftEvent(userId)), _ => ()))
          case Get           => ZIO.succeed((Command.ignore, _ => state))
        }

      override def sourceEvent(state: GuildState, event: GuildEvent): GuildState =
        event match {
          case JoinedEvent(userId) =>
            state.copy(
              members = state.members + userId
            )
          case LeftEvent(userId) =>
            state.copy(
              members = state.members - userId
            )
        }
    }
}
