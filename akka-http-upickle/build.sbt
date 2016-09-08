libraryDependencies ++= List(
  Library.akkaHttp,
  Library.upickle,
  Library.scalaTest % "test"
)

initialCommands := """|import de.heikoseeberger.akkahttpupickle._""".stripMargin
