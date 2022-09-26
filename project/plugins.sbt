// Versioning
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.11.0")
addSbtPlugin("com.dwijnand" % "sbt-dynver"    % "4.1.1")

// Packing
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.0")

// Linting & Styling
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.10.1")

addDependencyTreePlugin

// Kamon
addSbtPlugin("com.lightbend.sbt" % "sbt-javaagent"     % "0.1.6")
addSbtPlugin("io.kamon"          % "sbt-kanela-runner" % "2.0.14")
