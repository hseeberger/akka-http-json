import sbt._

object Version {
  val akkaHttp  = "1.0-M5"
  val play      = "2.4.0-RC1"
  val scala     = "2.11.6"
  val scalaTest = "2.2.4"
  val sprayJson = "1.3.1"
}

object Library {
  val akkaHttp  = "com.typesafe.akka" %% "akka-http-experimental" % Version.akkaHttp
  val playJson  = "com.typesafe.play" %% "play-json"              % Version.play
  val scalaTest = "org.scalatest"     %% "scalatest"              % Version.scalaTest
  val sprayJson = "io.spray"          %% "spray-json"             % Version.sprayJson
}
