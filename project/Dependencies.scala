import sbt._

object Version {
  val akkaHttp  = "1.0-RC3"
  val play      = "2.4.0"
  val scala     = "2.11.6"
  val scalaTest = "2.2.5"
}

object Library {
  val akkaHttp  = "com.typesafe.akka" %% "akka-http-experimental" % Version.akkaHttp
  val playJson  = "com.typesafe.play" %% "play-json"              % Version.play
  val scalaTest = "org.scalatest"     %% "scalatest"              % Version.scalaTest
}
