package infrastructure.actor

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.persistence.typed.PersistenceId
//import akka.persistence.typed.state.scaladsl.{DurableStateBehavior, Effect}
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}

object ExampleActor {
  case class Increment(replyTo: ActorRef[Int]) extends JsonSerializable
  case class Incremented(amount: Int) extends JsonSerializable

  object CounterActor {
    def empty = CounterActor(0)

    def apply(state: Int): Behaviors.Receive[Increment] =
      Behaviors.receiveMessage[Increment] { case Increment(replyTo) =>
        replyTo ! state
        CounterActor.apply(state + 1)
      }
  }

  object PersistentCounterActor {
    def apply(id: String): Behavior[Increment] = {
      Behaviors.setup { context =>
        EventSourcedBehavior[Increment, Incremented, Int](
          persistenceId =
            PersistenceId.ofUniqueId(s"PersistentCounterActor-${id}"),
          emptyState = 0,
          commandHandler = { (state: Int, command: Increment) =>
            Effect.persist(Incremented(1)).thenRun { state =>
              command.replyTo ! state
            }
          },
          eventHandler = { case (state, Incremented(amount)) => state + amount }
        )
      }
    }
  }

}
