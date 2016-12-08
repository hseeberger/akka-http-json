lazy val `akka-http-json` =
  project
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

lazy val `akka-http-argonaut`  = project.enablePlugins(AutomateHeaderPlugin)
lazy val `akka-http-circe`     = project.enablePlugins(AutomateHeaderPlugin)
lazy val `akka-http-jackson`   = project.enablePlugins(AutomateHeaderPlugin)
lazy val `akka-http-json4s`    = project.enablePlugins(AutomateHeaderPlugin)
lazy val `akka-http-play-json` = project.enablePlugins(AutomateHeaderPlugin)
lazy val `akka-http-upickle`   = project.enablePlugins(AutomateHeaderPlugin)

unmanagedSourceDirectories.in(Compile) := Vector.empty
unmanagedSourceDirectories.in(Test)    := Vector.empty

publishArtifact := false
