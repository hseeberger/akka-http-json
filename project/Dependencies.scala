import sbt._

object Version {
  final val Akka      = "2.4.2-RC3"
  final val Argonaut  = "6.1"
  final val Circe     = "0.2.1"
  final val Json4s    = "3.3.0"
  final val Play      = "2.4.6"
  final val Scala     = "2.11.7"
  final val ScalaTest = "2.2.6"
  final val Upickle   = "0.3.8"
}

object Library {
  val akkaHttp      = "com.typesafe.akka" %% "akka-http-experimental" % Version.Akka
  val argonaut      = "io.argonaut"       %% "argonaut"               % Version.Argonaut
  val circe         = "io.circe"          %% "circe-core"             % Version.Circe
  val circeJawn     = "io.circe"          %% "circe-jawn"             % Version.Circe
  val circeGeneric  = "io.circe"          %% "circe-generic"          % Version.Circe
  val json4sCore    = "org.json4s"        %% "json4s-core"            % Version.Json4s
  val json4sJackson = "org.json4s"        %% "json4s-jackson"         % Version.Json4s
  val json4sNative  = "org.json4s"        %% "json4s-native"          % Version.Json4s
  val playJson      = "com.typesafe.play" %% "play-json"              % Version.Play
  val scalaTest     = "org.scalatest"     %% "scalatest"              % Version.ScalaTest
  val upickle       = "com.lihaoyi"       %% "upickle"                % Version.Upickle
}
