lazy val akkaHttpJson = project
  .in(file("."))
  .aggregate(akkaHttpPlayJson, akkaHttpJson4s)
  .enablePlugins(GitVersioning)

lazy val akkaHttpPlayJson = project
  .in(file("akka-http-play-json"))
  .enablePlugins(AutomateHeaderPlugin)

lazy val akkaHttpJson4s = project
  .in(file("akka-http-json4s"))
  .enablePlugins(AutomateHeaderPlugin)

name := "akka-http-json"

unmanagedSourceDirectories in Compile := Nil
unmanagedSourceDirectories in Test := Nil

publishArtifact := false
