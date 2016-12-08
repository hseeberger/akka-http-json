import sbt._

object Version {
  final val Akka         = "2.4.14"
  final val AkkaHttp     = "10.0.0"
  final val Argonaut     = "6.2-RC2"
  final val Circe        = "0.7.0-M1"
  final val JacksonScala = "2.8.4"
  final val Json4s       = "3.5.0"
  final val Play         = "2.6.0-M1"
  final val Scala        = "2.12.1"
  final val ScalaTest    = "3.0.1"
  final val Upickle      = "0.4.4"
}

object Library {
  val akkaHttp            = "com.typesafe.akka"            %% "akka-http"            % Version.AkkaHttp
  val akkaHttpJacksonJava = "com.typesafe.akka"            %% "akka-http-jackson"    % Version.AkkaHttp
  val argonaut            = "io.argonaut"                  %% "argonaut"             % Version.Argonaut
  val circe               = "io.circe"                     %% "circe-core"           % Version.Circe
  val circeJawn           = "io.circe"                     %% "circe-jawn"           % Version.Circe
  val circeGeneric        = "io.circe"                     %% "circe-generic"        % Version.Circe
  val jacksonScala        = "com.fasterxml.jackson.module" %% "jackson-module-scala" % Version.JacksonScala
  val json4sCore          = "org.json4s"                   %% "json4s-core"          % Version.Json4s
  val json4sJackson       = "org.json4s"                   %% "json4s-jackson"       % Version.Json4s
  val json4sNative        = "org.json4s"                   %% "json4s-native"        % Version.Json4s
  val playJson            = "com.typesafe.play"            %% "play-json"            % Version.Play
  val scalaTest           = "org.scalatest"                %% "scalatest"            % Version.ScalaTest
  val upickle             = "com.lihaoyi"                  %% "upickle"              % Version.Upickle
}
