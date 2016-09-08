libraryDependencies ++= List(
  Library.akkaHttp,
  Library.akkaHttpJacksonJava,
  Library.jacksonScala,
  Library.scalaTest % "test"
)

initialCommands := """|import de.heikoseeberger.akkahttpjackson._""".stripMargin
