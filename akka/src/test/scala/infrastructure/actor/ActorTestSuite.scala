package infrastructure.actor

import akka.actor.typed.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

import java.util.concurrent.atomic.AtomicInteger

trait ActorTestSuite extends AsyncWordSpec with Matchers {

  val config: Config = ConfigFactory.load
  val actorSystemName = this.getClass.getSimpleName

  object ActorSystem {
    def start =
      ActorTestSuite.ActorSystemBuilder
        .isolatedSystem(actorSystemName, config)

    def stop(system: ActorSystem[Nothing]) = {
      system.terminate()
      system.whenTerminated
    }
    def restart(system: ActorSystem[Nothing]) =
      stop(system) map { done =>
        start
      }
  }

}

object ActorTestSuite {

  import akka.actor.typed.scaladsl.adapter._

  object ActorSystemBuilder {
    implicit lazy val commonSystem =
      akka.actor
        .ActorSystem(
          "test",
          ConfigFactory.load
        )
        .toTyped

    def isolatedSystem(name: String, config: Config) = {
      val port: Int = nextPort()
      akka.actor
        .ActorSystem(
          name,
          Seq(
            ConfigFactory.parseString(
              s"""
                   |  akka {
                   |    remote.artery {
                   |      transport = tcp
                   |      canonical.hostname = "0.0.0.0"
                   |      canonical.port = ${port}
                   |    }
                   |    cluster.seed-nodes = ["akka://${name}@0.0.0.0:${port}"]
                   |    
                   |  }
                   |""".stripMargin
            ),
            config,
            ConfigFactory.load
          ).reduce(_ withFallback _)
        )
        .toTyped
    }
  }

  private val nextPort: () => Int = {
    val port = new AtomicInteger()
    port.set(2552)
    port.incrementAndGet _
  }

}
