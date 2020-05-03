name := "recipes"

version := "0.1"

scalaVersion := "2.13.1"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-generic" % "0.13.0",

  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",

  "org.typelevel" %% "cats-effect" % "2.1.2",

  "com.softwaremill.sttp.client" %% "async-http-client-backend-cats" % "2.0.7",
  "com.softwaremill.sttp.client" %% "circe" % "2.0.7",

  "org.rudogma" %% "supertagged" % "2.0-RC1",

  "org.http4s" %% "http4s-dsl" % "0.21.3",
  "org.http4s" %% "http4s-blaze-server" % "0.21.3",
  "org.http4s" %% "http4s-blaze-client" % "0.21.3",

  "org.tpolecat" %% "doobie-core"      % "0.8.8",
  "org.tpolecat" %% "doobie-hikari"    % "0.8.8",
  "org.tpolecat" %% "doobie-postgres"  % "0.8.8",
  "org.tpolecat" %% "doobie-scalatest" % "0.8.8" % Test,

  "org.tpolecat" %% "doobie-scalatest" % "0.8.8" % Test,
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