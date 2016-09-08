libraryDependencies ++= List(
  Library.akkaHttp,
  Library.playJson,
  Library.scalaTest % "test"
)

initialCommands := """|import de.heikoseeberger.akkahttpplayjson._""".stripMargin
