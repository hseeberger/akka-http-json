import sbt._

object Version {
  val akkaHttp  = "1.0-RC4"
  val json4s    = "3.3.0.RC2"
  val play      = "2.4.0"
  val scala     = "2.11.7"
  val scalaTest = "2.2.5"
}

object Library {
  val akkaHttp      = "com.typesafe.akka" %% "akka-http-experimental" % Version.akkaHttp
  val json4sCore    = "org.json4s"        %% "json4s-core"            % Version.json4s
  val json4sJackson = "org.json4s"        %% "json4s-jackson"         % Version.json4s
  val json4sNative  = "org.json4s"        %% "json4s-native"          % Version.json4s
  val playJson      = "com.typesafe.play" %% "play-json"              % Version.play
  val scalaTest     = "org.scalatest"     %% "scalatest"              % Version.scalaTest
}
