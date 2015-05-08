name := "akka-http-json-4s-native"

libraryDependencies ++= List(
  Library.akkaHttp,
  Library.json4sNative,
  Library.scalaTest % "test"
)

initialCommands := """|import de.heikoseeberger.akkahttpjson4snative._""".stripMargin
