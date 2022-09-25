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
  .settings(commonSettings)
  .dependsOn(akka)

lazy val akka = project
  .settings(
    name := "akka"
  )
  .settings(commonSettings)
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
