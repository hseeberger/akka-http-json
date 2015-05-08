name := "akka-http-json-4s-jackson"

libraryDependencies ++= List(
  Library.akkaHttp,
  Library.json4sJackson,
  Library.scalaTest % "test"
)

initialCommands := """|import de.heikoseeberger.akkahttpjson4sjackson._""".stripMargin
