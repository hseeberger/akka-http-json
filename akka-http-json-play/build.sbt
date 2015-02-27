name := "akka-http-json-play"

libraryDependencies ++= List(
  Library.akkaHttp,
  Library.playJson,
  Library.scalaTest % "test"
)

initialCommands := """|import de.heikoseeberger.akkahttpjsonplay._""".stripMargin
