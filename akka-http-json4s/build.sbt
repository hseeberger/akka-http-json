libraryDependencies ++= List(
  Library.akkaHttp,
  Library.json4sCore,
  Library.json4sJackson % "test",
  Library.json4sNative  % "test",
  Library.scalaTest     % "test"
)

initialCommands := """|import de.heikoseeberger.akkahttpjson4s._""".stripMargin
