// https://mvnrepository.com/artifact/com.typesafe.akka/akka-stream-kafka
libraryDependencies += "com.typesafe.akka" %% "akka-stream-kafka" % "3.0.0"
libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % "2.6.18"
// https://mvnrepository.com/artifact/com.typesafe.akka/akka-cluster
libraryDependencies += "com.typesafe.akka" %% "akka-cluster" % "2.6.18"
libraryDependencies += "io.altoo" %% "akka-kryo-serialization" % "2.4.3"
// https://mvnrepository.com/artifact/com.typesafe.akka/akka-stream
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.6.18"
// https://mvnrepository.com/artifact/com.typesafe.akka/akka-actor
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.6.18"
libraryDependencies += "com.typesafe.akka" %% "akka-cluster-sharding" % "2.6.18"

// https://mvnrepository.com/artifact/io.prometheus/simpleclient_httpserver
libraryDependencies += "io.prometheus" % "simpleclient_httpserver" % "0.15.0"

val AkkaVersion = "2.6.18"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-persistence-cassandra" % "1.0.6",
  "com.typesafe.akka" %% "akka-persistence" % AkkaVersion,
  "com.typesafe.akka" %% "akka-cluster-tools" % AkkaVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-cluster-sharding-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-persistence-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-serialization-jackson" % AkkaVersion,
  "org.scalatest" %% "scalatest" % "3.2.8" % Test
)

// https://mvnrepository.com/artifact/org.slf4j/slf4j-api
//libraryDependencies += "org.slf4j" % "slf4j-api" % "2.0.2"
// https://mvnrepository.com/artifact/org.slf4j/slf4j-simple
// libraryDependencies += "org.slf4j" % "slf4j-simple" % "2.0.2" % Test

libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
