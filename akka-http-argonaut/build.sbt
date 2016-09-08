libraryDependencies ++= List(
  Library.akkaHttp,
  Library.argonaut,
  Library.scalaTest % "test"
)

initialCommands := """|import de.heikoseeberger.akkahttpargonaut._""".stripMargin
