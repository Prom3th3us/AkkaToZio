import sbt._

object Dependencies {
  object Versions {
    val typeSafeConfigVersion     = "1.4.2"
    val akkaVersion               = "2.6.19"
    val scalaTestVersion          = "3.2.13"
    val logbackVersion            = "1.2.11"
    val logbackEncoderVersion     = "7.2"
    val jacksonScalaModuleVersion = "2.13.3"
  }

  object TypeSafe {
    val config = "com.typesafe" % "config" % Versions.typeSafeConfigVersion

    val all = Seq(
      config
    )
  }

  object Akka {
    val akkaActor        = "com.typesafe.akka" %% "akka-actor"         % Versions.akkaVersion
    val akkaCluster      = "com.typesafe.akka" %% "akka-cluster"       % Versions.akkaVersion
    val akkaClusterTools = "com.typesafe.akka" %% "akka-cluster-tools" % Versions.akkaVersion
    val akkaClusterSharding =
      "com.typesafe.akka" %% "akka-cluster-sharding" % Versions.akkaVersion

    val akkaPersistence          = "com.typesafe.akka" %% "akka-persistence"           % Versions.akkaVersion
    val akkaPersistenceCassandra = "com.typesafe.akka" %% "akka-persistence-cassandra" % "1.0.6"

    val akkaSlf4j   = "com.typesafe.akka" %% "akka-slf4j"                 % Versions.akkaVersion
    val akkaKryo    = "io.altoo"          %% "akka-kryo-serialization"    % "2.4.3"
    val akkaJackson = "com.typesafe.akka" %% "akka-serialization-jackson" % Versions.akkaVersion

    val akkaKfka = "com.typesafe.akka" %% "akka-stream-kafka" % "3.0.0"

    val all = Seq(
      akkaActor,
      akkaCluster,
      akkaClusterTools,
      akkaClusterSharding,
      akkaPersistence,
      akkaPersistenceCassandra,
      akkaSlf4j,
      akkaKryo,
      akkaJackson,
      akkaKfka
    )
  }

  object AkkaTyped {
    val akkaActorTyped       = "com.typesafe.akka" %% "akka-actor-typed"            % Versions.akkaVersion
    val akkaPersistenceTyped = "com.typesafe.akka" %% "akka-persistence-typed"      % Versions.akkaVersion
    val akkaShardingTyped    = "com.typesafe.akka" %% "akka-cluster-sharding-typed" % Versions.akkaVersion

    val all = Seq(
      akkaActorTyped,
      akkaPersistenceTyped,
      akkaShardingTyped
    )
  }

  object Testing {
    val scalaTest =
      "org.scalatest" %% "scalatest" % Versions.scalaTestVersion % Test

    val all = Seq(
      scalaTest
    )
  }

  object Logging {
    val logback = "ch.qos.logback" % "logback-classic" % Versions.logbackVersion
    val logbackEncoder =
      "net.logstash.logback" % "logstash-logback-encoder" % Versions.logbackEncoderVersion % Runtime
    val jacksonScalaModule =
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % Versions.jacksonScalaModuleVersion % Runtime

    val all = Seq(
      logback,
      logbackEncoder,
      jacksonScalaModule
    )
  }

  object Prometheus {
    val prometheus = "io.prometheus" % "simpleclient_httpserver" % "0.15.0"

    val all = Seq(
      prometheus
    )
  }

  object Kamon {
    val version    = "2.5.7"
    val bundle     = "io.kamon" %% "kamon-bundle"     % version
    val prometheus = "io.kamon" %% "kamon-prometheus" % version
    val jaeger     = "io.kamon" %% "kamon-jaeger"     % version
    val logback    = "io.kamon" %% "kamon-logback"    % version
    val agent      = "io.kamon"  % "kanela-agent"     % "1.0.15"

    val all = Seq(Kamon.bundle, Kamon.prometheus, Kamon.jaeger, Kamon.logback)
  }
}
