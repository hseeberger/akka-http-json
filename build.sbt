lazy val akkaHttpJson = project
  .in(file("."))
  .aggregate(akkaHttpJsonPlay, akkaHttpJsonSpray)

lazy val akkaHttpJsonPlay = project
  .in(file("akka-http-json-play"))

lazy val akkaHttpJsonSpray = project
  .in(file("akka-http-json-spray"))

name := "akka-http-json"

unmanagedSourceDirectories in Compile := Nil
unmanagedSourceDirectories in Test := Nil

publishArtifact := false
