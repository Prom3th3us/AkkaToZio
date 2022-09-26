package infrastructure.actor

import akka.actor.typed.ActorSystem
import com.typesafe.config.{ Config, ConfigFactory }
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

trait ActorTestSuite extends AsyncWordSpec with Matchers {

  val config: Config  = ConfigFactory.load
  val actorSystemName = this.getClass.getSimpleName

  object ActorSystem {
    def start(port: Int = 2552) =
      ActorTestSuite.ActorSystemBuilder
        .isolatedSystem(actorSystemName, config, port)

    def stop(system: ActorSystem[Nothing]) = {
      system.terminate()
      system.whenTerminated
    }
  }

}

object ActorTestSuite {

  import akka.actor.typed.scaladsl.adapter._

  object ActorSystemBuilder {

    def isolatedSystem(name: String, config: Config, port: Int) = {
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

}
