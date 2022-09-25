ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "akka-to-zio"
  )

lazy val e2e = project.dependsOn(akka)
lazy val akka = project
