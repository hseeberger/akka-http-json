// *****************************************************************************
// Projects
// *****************************************************************************

lazy val `akka-http-json` =
  project
    .in(file("."))
    .enablePlugins(GitVersioning)
    .aggregate(
      `akka-http-argonaut`,
      `akka-http-circe`,
      `akka-http-jackson`,
      `akka-http-json4s`,
      `akka-http-play-json`,
      `akka-http-upickle`
    )
    .settings(settings)
    .settings(
      unmanagedSourceDirectories.in(Compile) := Seq.empty,
      unmanagedSourceDirectories.in(Test)    := Seq.empty,
      publishArtifact := false
    )

lazy val `akka-http-argonaut`=
  project
    .enablePlugins(AutomateHeaderPlugin)
    .settings(settings)
    .settings(
      libraryDependencies ++= Seq(
        library.akkaHttp,
        library.argonaut,
        library.scalaTest % Test
      )
    )

lazy val `akka-http-circe` =
  project
    .enablePlugins(AutomateHeaderPlugin)
    .settings(settings)
    .settings(
      libraryDependencies ++= Seq(
        library.akkaHttp,
        library.circe,
        library.circeJawn,
        library.circeGeneric % Test,
        library.scalaTest    % Test
      )
    )

lazy val `akka-http-jackson` =
  project
    .enablePlugins(AutomateHeaderPlugin)
    .settings(settings)
    .settings(
      libraryDependencies ++= Seq(
        library.akkaHttp,
        library.akkaHttpJacksonJava,
        library.jacksonScala,
        library.scalaTest % Test
      )
    )

lazy val `akka-http-json4s` =
  project
    .enablePlugins(AutomateHeaderPlugin)
    .settings(settings)
    .settings(
      libraryDependencies ++= Seq(
        library.akkaHttp,
        library.json4sCore,
        library.json4sJackson % Test,
        library.json4sNative  % Test,
        library.scalaTest     % Test
      )
    )

lazy val `akka-http-play-json` =
  project
    .enablePlugins(AutomateHeaderPlugin)
    .settings(settings)
    .settings(
      libraryDependencies ++= Seq(
        library.akkaHttp,
        library.playJson,
        library.scalaTest % Test
      )
    )

lazy val `akka-http-upickle` =
  project
    .enablePlugins(AutomateHeaderPlugin)
    .settings(settings)
    .settings(
      libraryDependencies ++= Seq(
        library.akkaHttp,
        library.upickle,
        library.scalaTest % Test
      )
    )

// *****************************************************************************
// Library dependencies
// *****************************************************************************

lazy val library =
  new {
    object Version {
      final val akka         = "2.4.17"
      final val akkaHttp     = "10.0.5"
      final val argonaut     = "6.2-RC2"
      final val circe        = "0.7.1"
      final val jacksonScala = "2.8.7"
      final val json4s       = "3.5.1"
      final val play         = "2.6.0-M6"
      final val scala        = "2.12.1"
      final val scalaTest    = "3.0.1"
      final val upickle      = "0.4.4"
    }
    val akkaHttp            = "com.typesafe.akka"            %% "akka-http"            % Version.akkaHttp
    val akkaHttpJacksonJava = "com.typesafe.akka"            %% "akka-http-jackson"    % Version.akkaHttp
    val argonaut            = "io.argonaut"                  %% "argonaut"             % Version.argonaut
    val circe               = "io.circe"                     %% "circe-core"           % Version.circe
    val circeJawn           = "io.circe"                     %% "circe-jawn"           % Version.circe
    val circeGeneric        = "io.circe"                     %% "circe-generic"        % Version.circe
    val jacksonScala        = "com.fasterxml.jackson.module" %% "jackson-module-scala" % Version.jacksonScala
    val json4sCore          = "org.json4s"                   %% "json4s-core"          % Version.json4s
    val json4sJackson       = "org.json4s"                   %% "json4s-jackson"       % Version.json4s
    val json4sNative        = "org.json4s"                   %% "json4s-native"        % Version.json4s
    val playJson            = "com.typesafe.play"            %% "play-json"            % Version.play
    val scalaTest           = "org.scalatest"                %% "scalatest"            % Version.scalaTest
    val upickle             = "com.lihaoyi"                  %% "upickle"              % Version.upickle
  }

// *****************************************************************************
// Settings
// *****************************************************************************

lazy val settings =
  commonSettings ++
  gitSettings ++
  headerSettings ++
  publishSettings

lazy val commonSettings =
  Seq(
    // scalaVersion and crossScalaVersions from .travis.yml via sbt-travisci
    // scalaVersion := "2.12.1",
    // crossScalaVersions := Seq(scalaVersion.value, "2.11.8"),
    organization := "de.heikoseeberger",
    licenses += ("Apache 2.0",
                 url("http://www.apache.org/licenses/LICENSE-2.0")),
    mappings.in(Compile, packageBin) += baseDirectory.in(ThisBuild).value / "LICENSE" -> "LICENSE",
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-language:_",
      "-target:jvm-1.8",
      "-encoding", "UTF-8"
    ),
    javacOptions ++= Seq(
      "-source", "1.8",
      "-target", "1.8"
    ),
    unmanagedSourceDirectories.in(Compile) := Seq(scalaSource.in(Compile).value),
    unmanagedSourceDirectories.in(Test) := Seq(scalaSource.in(Test).value),
    shellPrompt in ThisBuild := { state =>
      val project = Project.extract(state).currentRef.project
      s"[$project]> "
    }

)

lazy val gitSettings =
  Seq(
    git.useGitDescribe := true
  )

import de.heikoseeberger.sbtheader.license._
lazy val headerSettings =
  Seq(
    headers := Map("scala" -> Apache2_0("2015", "Heiko Seeberger"))
  )

lazy val publishSettings =
  Seq(
    homepage := Some(url("https://github.com/hseeberger/akka-http-json")),
    scmInfo := Some(ScmInfo(url("https://github.com/hseeberger/akka-http-json"),
                            "git@github.com:hseeberger/akka-http-json.git")),
    developers += Developer("hseeberger",
                            "Heiko Seeberger",
                            "mail@heikoseeberger.de",
                            url("https://github.com/hseeberger")),
    pomIncludeRepository := (_ => false),
    bintrayPackage := "akka-http-json"
  )
