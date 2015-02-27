lazy val akkaHttpJson = project.in(file("."))

name := "akka-http-json"

libraryDependencies ++= List(
)

initialCommands := """|import de.heikoseeberger.akkahttpjson._""".stripMargin
