libraryDependencies ++= List(
  Library.akkaHttp,
  Library.circe,
  Library.circeJawn,
  Library.circeGeneric,
  Library.scalaTest    % "test"
)

initialCommands := """|import de.heikoseeberger.akkahttpcirce._""".stripMargin
