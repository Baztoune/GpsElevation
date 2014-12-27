name := """GpsElevation"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.4"

libraryDependencies ++= Seq(
  "com.google.maps" % "google-maps-services" % "0.1.4",
  jdbc,
  anorm,
  cache,
  ws
)

Keys.fork in Test := false