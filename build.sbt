lazy val `akka-http-json` = project
  .in(file("."))
  .aggregate(
    `akka-http-argonaut`,
    `akka-http-circe`,
    `akka-http-jackson`,
    `akka-http-json4s`,
    `akka-http-play-json`,
    `akka-http-upickle`
  )
  .enablePlugins(GitVersioning)

lazy val `akka-http-argonaut` = project
  .in(file("akka-http-argonaut"))
  .enablePlugins(AutomateHeaderPlugin)

lazy val `akka-http-circe` = project
  .in(file("akka-http-circe"))
  .enablePlugins(AutomateHeaderPlugin)

lazy val `akka-http-jackson` = project
  .in(file("akka-http-jackson"))
  .enablePlugins(AutomateHeaderPlugin)

lazy val `akka-http-json4s` = project
  .copy(id = "akka-http-json4s")
  .in(file("akka-http-json4s"))
  .enablePlugins(AutomateHeaderPlugin)

lazy val `akka-http-play-json` = project
  .in(file("akka-http-play-json"))
  .enablePlugins(AutomateHeaderPlugin)

lazy val `akka-http-upickle` = project
  .in(file("akka-http-upickle"))
  .enablePlugins(AutomateHeaderPlugin)

unmanagedSourceDirectories.in(Compile) := Vector.empty
unmanagedSourceDirectories.in(Test)    := Vector.empty

publishArtifact := false
