// Global Settings
ThisBuild / scalaVersion    := "2.13.8"
ThisBuild / organization    := "Prom3theus"
ThisBuild / versionScheme   := Some("early-semver")
ThisBuild / dynverSeparator := "-"
ThisBuild / conflictManager := ConflictManager.latestRevision
ThisBuild / javacOptions ++= Seq("-source", "17", "-target", "17")
ThisBuild / scalacOptions ++= Seq("-Ymacro-annotations", "-target:jvm-17")

lazy val commonSettings = Seq(
  run / fork                := true,
  Test / testForkedParallel := true,
  libraryDependencies ++= Seq(
    Dependencies.Logging.all,
    Dependencies.TypeSafe.all,
    Dependencies.Testing.all
  ).flatten
)

lazy val scalafixSettings: Seq[Setting[_]] = Seq(
  addCompilerPlugin(scalafixSemanticdb),
  semanticdbEnabled := true,
  scalafixOnCompile := true
)

lazy val dockerSettings = Seq(
  dockerUsername              := sys.props.get("docker.username"),
  dockerRepository            := sys.props.get("docker.registry"),
  Docker / version            := "latest",
  Docker / organization       := "miguelemos",
  Docker / dockerBaseImage    := "openjdk",
  Docker / packageName        := "miguelemos/akka-to-zio",
  Docker / dockerExposedPorts := Seq(9095)
)

lazy val e2e = project
  .settings(
    name := "e2e"
  )
  .settings(commonSettings, scalafixSettings)
  .dependsOn(akka)

lazy val akka = project
  .settings(
    name := "akka"
  )
  .settings(commonSettings, scalafixSettings)
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.Akka.all,
      Dependencies.AkkaTyped.all
    ).flatten
  )
  .settings(dockerSettings)
  .settings(
    libraryDependencies ++= Dependencies.Prometheus.all,
    libraryDependencies ++= Dependencies.Kamon.all,
    javaAgents += Dependencies.Kamon.agent
  )
  .enablePlugins(DockerPlugin, AshScriptPlugin, JavaAgent)

lazy val zio = project
  .settings(
    name := "zio"
  )
  .settings(commonSettings, scalafixSettings)
  .settings(
    // https://mvnrepository.com/artifact/dev.zio/zio-actors
    // https://mvnrepository.com/artifact/dev.zio/zio-actors-persistence
    libraryDependencies += "dev.zio"               %% "zio"                    % "2.0.2",
    libraryDependencies += "dev.zio"               %% "zio-test"               % "2.0.0",
    libraryDependencies += "dev.zio"               %% "zio-actors-persistence" % "0.0.9",
    libraryDependencies += "dev.zio"               %% "zio-actors"             % "0.0.9",
    libraryDependencies += "io.getquill"           %% "quill-cassandra"        % "4.4.1",
    libraryDependencies += "com.datastax.cassandra" % "cassandra-driver-core"  % "3.11.3",
    libraryDependencies += "io.circe"              %% "circe-core"             % "0.15.0-M1",
    libraryDependencies += "io.circe"              %% "circe-parser"           % "0.15.0-M1",
    libraryDependencies += "io.circe"              %% "circe-generic"          % "0.15.0-M1"
  )
