name := "recipes"

version := "0.1"

scalaVersion := "2.13.1"

libraryDependencies ++= Seq(
  //akka
  "com.typesafe.akka" %% "akka-http" % "10.1.11",
  "de.heikoseeberger" %% "akka-http-circe" % "1.31.0",

  //logging
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",

  //cats
  "org.typelevel" %% "cats-effect" % "2.1.2"

)

scalacOptions ++= Seq(
  "-Ywarn-dead-code",                  // Warn when dead code is identified.
  "-Ywarn-value-discard",              // Warn when non-Unit expression results are unused.

  "-unchecked",
  "-feature",
  "-deprecation:false",
  "-Xfatal-warnings",
)