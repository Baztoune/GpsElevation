name := """GpsElevation"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.4"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "com.google.maps" % "google-maps-services" % "0.1.9",
  "org.webjars"     % "jquery"               % "2.1.3",
  "org.webjars"     % "bootstrap"            % "3.1.1",
  "org.webjars"     % "bootstrap-filestyle"  % "1.1.2"
)

Keys.fork in Test := false