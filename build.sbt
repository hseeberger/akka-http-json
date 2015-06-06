lazy val akkaHttpJson = project
  .in(file("."))
  .aggregate(akkaHttpJsonPlay)
  .enablePlugins(GitVersioning)

lazy val akkaHttpJsonPlay = project
  .in(file("akka-http-json-play"))
  .enablePlugins(AutomateHeaderPlugin)

name := "akka-http-json"

unmanagedSourceDirectories in Compile := Nil
unmanagedSourceDirectories in Test := Nil

publishArtifact := false
