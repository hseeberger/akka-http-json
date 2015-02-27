name := "akka-http-json-spray"

libraryDependencies ++= List(
  Library.akkaHttp,
  Library.sprayJson,
  Library.scalaTest % "test"
)

initialCommands := """|import de.heikoseeberger.akkahttpjsonspray._""".stripMargin
