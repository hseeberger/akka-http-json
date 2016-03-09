lazy val akkaHttpJson = project
  .copy(id = "akka-http-json")
  .in(file("."))
  .aggregate(akkaHttpPlayJson, akkaHttpJson4s, akkaHttpUpickle, akkaHttpCirce, akkaHttpArgonaut)
  .enablePlugins(GitVersioning)

lazy val akkaHttpArgonaut = project
  .copy(id = "akka-http-argonaut")
  .in(file("akka-http-argonaut"))
  .enablePlugins(AutomateHeaderPlugin)

lazy val akkaHttpCirce = project
  .copy(id = "akka-http-circe")
  .in(file("akka-http-circe"))
  .enablePlugins(AutomateHeaderPlugin)

lazy val akkaHttpJson4s = project
  .copy(id = "akka-http-json4s")
  .in(file("akka-http-json4s"))
  .enablePlugins(AutomateHeaderPlugin)

lazy val akkaHttpUpickle = project
  .copy(id = "akka-http-upickle")
  .in(file("akka-http-upickle"))
  .enablePlugins(AutomateHeaderPlugin)

lazy val akkaHttpPlayJson = project
  .copy(id = "akka-http-play-json")
  .in(file("akka-http-play-json"))
  .enablePlugins(AutomateHeaderPlugin)

name := "akka-http-json"

unmanagedSourceDirectories in Compile := Vector.empty
unmanagedSourceDirectories in Test    := Vector.empty

publishArtifact := false
