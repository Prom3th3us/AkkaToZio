// Global Settings
ThisBuild / scalaVersion    := "2.13.9"
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
    libraryDependencies ++= Seq(
      Dependencies.Prometheus.all,
      Dependencies.Kamon.all
    ).flatten,
    javaAgents += Dependencies.Kamon.agent
  )
  .enablePlugins(DockerPlugin, AshScriptPlugin, JavaAgent)

lazy val zio = project
  .settings(
    name := "zio"
  )
  .settings(commonSettings, scalafixSettings)
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.Zio.all,
      Dependencies.Circee.all,
      Dependencies.Quill.all,
      Dependencies.Cassandra.all
    ).flatten
  )
  .settings(dockerSettings)
  .enablePlugins(DockerPlugin, AshScriptPlugin)
