lazy val akkaHttpJson = project
  .in(file("."))
  .aggregate(akkaHttpJsonPlay, akkaHttpJson4s)
  .enablePlugins(GitVersioning)

lazy val akkaHttpJsonPlay = project
  .in(file("akka-http-json-play"))
  .enablePlugins(AutomateHeaderPlugin)

lazy val akkaHttpJson4s = project
  .in(file("akka-http-json-4s"))
  .enablePlugins(AutomateHeaderPlugin)

name := "akka-http-json"

unmanagedSourceDirectories in Compile := Nil
unmanagedSourceDirectories in Test := Nil

publishArtifact := false
