lazy val akkaHttpJson = project
  .in(file("."))
  .aggregate(akkaHttpJsonPlay, akkaHttpJsonSpray)
  .enablePlugins(GitVersioning)

lazy val akkaHttpJsonPlay = project
  .in(file("akka-http-json-play"))
  .enablePlugins(AutomateHeaderPlugin)

lazy val akkaHttpJsonSpray = project
  .in(file("akka-http-json-spray"))
  .enablePlugins(AutomateHeaderPlugin)

name := "akka-http-json"

unmanagedSourceDirectories in Compile := Nil
unmanagedSourceDirectories in Test := Nil

publishArtifact := false
