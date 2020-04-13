name := "recipes"

version := "0.1"

scalaVersion := "2.13.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.1.11",
  "de.heikoseeberger" %% "akka-http-circe" % "1.31.0",
  "io.circe" %% "circe-generic" % "0.13.0",
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "org.typelevel" %% "cats-core" % "2.0.0",
  "org.typelevel" %% "cats-effect" % "2.1.2",
  "com.softwaremill.sttp.client" %% "async-http-client-backend-cats" % "2.0.7",
  "com.softwaremill.sttp.client" %% "circe" % "2.0.7",

  "org.scalatest" %% "scalatest" % "3.0.8" % Test,
  "org.scalamock" %% "scalamock" % "4.4.0" % Test
)

scalacOptions ++= Seq(
  "-Ywarn-dead-code", // Warn when dead code is identified.
  "-Ywarn-value-discard", // Warn when non-Unit expression results are unused.

  "-unchecked",
  "-feature",
  "-deprecation:false",
  "-Xfatal-warnings",
)