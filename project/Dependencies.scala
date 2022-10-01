import sbt._

object Dependencies {
  object Versions {
    val typeSafeConfig          = "1.4.2"
    val akka                    = "2.6.19"
    val akkaPersstanceCassandra = "1.0.6"
    val akkaKryoSerialization   = "2.4.3"
    val akkaStreamKafka         = "3.0.0"
    val scalaTest               = "3.2.13"
    val logback                 = "1.2.11"
    val logbackEncoder          = "7.2"
    val jacksonScalaModule      = "2.13.3"
    val kamon                   = "2.5.7"
    val kanelaAgent             = "1.0.15"
    val prometheus              = "0.15.0"
    val zio                     = "2.0.2"
    val zioActors               = "0.1.0"
    val zioTest                 = "2.0.0"
    val zioGrpc                 = "0.6.0-test4"
    val circee                  = "0.15.0-M1"
    val quill                   = "4.4.1"
    val cassandraDatastax       = "3.11.3"
    val shardcake               = "2.0.0"
  }

  object TypeSafe {
    val config = "com.typesafe" % "config" % Versions.typeSafeConfig

    val all = Seq(
      config
    )
  }

  object Akka {
    val akkaActor        = "com.typesafe.akka" %% "akka-actor"         % Versions.akka
    val akkaCluster      = "com.typesafe.akka" %% "akka-cluster"       % Versions.akka
    val akkaClusterTools = "com.typesafe.akka" %% "akka-cluster-tools" % Versions.akka
    val akkaClusterSharding =
      "com.typesafe.akka" %% "akka-cluster-sharding" % Versions.akka

    val akkaPersistence = "com.typesafe.akka" %% "akka-persistence" % Versions.akka
    val akkaPersistenceCassandra =
      "com.typesafe.akka" %% "akka-persistence-cassandra" % Versions.akkaPersstanceCassandra

    val akkaSlf4j   = "com.typesafe.akka" %% "akka-slf4j"                 % Versions.akka
    val akkaKryo    = "io.altoo"          %% "akka-kryo-serialization"    % Versions.akkaKryoSerialization
    val akkaJackson = "com.typesafe.akka" %% "akka-serialization-jackson" % Versions.akka

    val akkaKfka = "com.typesafe.akka" %% "akka-stream-kafka" % Versions.akkaStreamKafka

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
    val akkaActorTyped       = "com.typesafe.akka" %% "akka-actor-typed"            % Versions.akka
    val akkaPersistenceTyped = "com.typesafe.akka" %% "akka-persistence-typed"      % Versions.akka
    val akkaShardingTyped    = "com.typesafe.akka" %% "akka-cluster-sharding-typed" % Versions.akka

    val all = Seq(
      akkaActorTyped,
      akkaPersistenceTyped,
      akkaShardingTyped
    )
  }

  object Testing {
    val scalaTest =
      "org.scalatest" %% "scalatest" % Versions.scalaTest % Test

    val all = Seq(
      scalaTest
    )
  }

  object Logging {
    val logback = "ch.qos.logback" % "logback-classic" % Versions.logback
    val logbackEncoder =
      "net.logstash.logback" % "logstash-logback-encoder" % Versions.logbackEncoder % Runtime
    val jacksonScalaModule =
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % Versions.jacksonScalaModule % Runtime

    val all = Seq(
      logback,
      logbackEncoder,
      jacksonScalaModule
    )
  }

  object Prometheus {
    val prometheus = "io.prometheus" % "simpleclient_httpserver" % Versions.prometheus

    val all = Seq(
      prometheus
    )
  }

  object Kamon {
    val bundle     = "io.kamon" %% "kamon-bundle"     % Versions.kamon
    val prometheus = "io.kamon" %% "kamon-prometheus" % Versions.kamon
    val jaeger     = "io.kamon" %% "kamon-jaeger"     % Versions.kamon
    val logback    = "io.kamon" %% "kamon-logback"    % Versions.kamon
    val agent      = "io.kamon"  % "kanela-agent"     % Versions.kanelaAgent

    val all = Seq(Kamon.bundle, Kamon.prometheus, Kamon.jaeger, Kamon.logback)
  }

  object Zio {
    val zio                  = "dev.zio"                       %% "zio"                    % Versions.zio
    val zioActors            = "dev.zio"                       %% "zio-actors"             % Versions.zioActors
    val zioActorsPersistence = "dev.zio"                       %% "zio-actors-persistence" % Versions.zioActors
    val zioTest              = "dev.zio"                       %% "zio-test"               % Versions.zioTest % Test
    val zioGrpc              = "com.thesamet.scalapb.zio-grpc" %% "zio-grpc-core"          % Versions.zioGrpc

    val all = Seq(
      zio,
      zioActors,
      zioActorsPersistence,
      zioTest,
      zioGrpc
    )
  }

  object Circee {
    val circeeCore    = "io.circe" %% "circe-core"    % Versions.circee
    val circeeParser  = "io.circe" %% "circe-parser"  % Versions.circee
    val circeeGeneric = "io.circe" %% "circe-generic" % Versions.circee

    val all = Seq(
      circeeCore,
      circeeParser,
      circeeGeneric
    )
  }

  object Quill {
    val quill = "io.getquill" %% "quill-cassandra" % Versions.quill

    val all = Seq(
      quill
    )
  }

  object Cassandra {
    val cassandraDatastax = "com.datastax.cassandra" % "cassandra-driver-core" % Versions.cassandraDatastax

    val all = Seq(
      cassandraDatastax
    )
  }

  object Shardcake {
    val shardcake         = "com.devsisters" %% "shardcake-core"               % Versions.shardcake
    val shardcakeEntities = "com.devsisters" %% "shardcake-entities"           % Versions.shardcake
    val shardcakeManager  = "com.devsisters" %% "shardcake-manager"            % Versions.shardcake
    val shardcakeK8s      = "com.devsisters" %% "shardcake-health-k8s"         % Versions.shardcake
    val shardcakeGrpc     = "com.devsisters" %% "shardcake-protocol-grpc"      % Versions.shardcake
    val shardcakeKryo     = "com.devsisters" %% "shardcake-serialization-kryo" % Versions.shardcake
    val shardcakeRedis    = "com.devsisters" %% "shardcake-storage-redis"      % Versions.shardcake

    val all = Seq(
      shardcake,
      shardcakeEntities,
      shardcakeManager,
      shardcakeK8s,
      shardcakeGrpc,
      shardcakeKryo,
      shardcakeRedis
    )
  }
}
