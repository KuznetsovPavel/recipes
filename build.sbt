name := "recipes"

version := "0.1"

scalaVersion := "2.13.1"

scalacOptions ++= Seq(
  "-Ywarn-dead-code",                  // Warn when dead code is identified.
  "-Ywarn-value-discard",              // Warn when non-Unit expression results are unused.

  "-unchecked",
  "-feature",
  "-deprecation:false",
  "-Xfatal-warnings",
)