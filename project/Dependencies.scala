import sbt._

object Version {
  final val Akka         = "3.0.0-RC1"
  final val Argonaut     = "6.1"
  final val Circe        = "0.6.0-RC1"
  final val JacksonScala = "2.8.3"
  final val Json4s       = "3.4.2"
  final val Play         = "2.5.9"
  final val Scala        = "2.11.8"
  final val ScalaTest    = "3.0.0"
  final val Upickle      = "0.4.3"
}

object Library {
  val akkaHttp            = "com.typesafe.akka"            %% "akka-http"            % Version.Akka
  val akkaHttpJacksonJava = "com.typesafe.akka"            %% "akka-http-jackson"    % Version.Akka
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
