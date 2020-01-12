inThisBuild(
  Seq(
    organization := "de.heikoseeberger",
    homepage := Some(url("https://github.com/hseeberger/akka-http-json")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    scmInfo := Some(
      ScmInfo(url("https://github.com/hseeberger/akka-http-json"),
              "git@github.com:hseeberger/akka-http-json.git")
    ),
    developers := List(
      Developer("hseeberger",
                "Heiko Seeberger",
                "mail@heikoseeberger.de",
                url("https://github.com/hseeberger"))
    ),
  )
)

// *****************************************************************************
// Projects
// *****************************************************************************

lazy val `akka-http-json` =
  project
    .in(file("."))
    .aggregate(
      `akka-http-argonaut`,
      `akka-http-avro4s`,
//      `akka-http-avsystem-gencodec`,
      `akka-http-circe`,
      `akka-http-jackson`,
      `akka-http-json4s`,
      `akka-http-jsoniter-scala`,
      `akka-http-play-json`,
      `akka-http-upickle`,
    )
    .settings(settings)
    .settings(
      Compile / unmanagedSourceDirectories := Seq.empty,
      Test / unmanagedSourceDirectories    := Seq.empty,
      publishArtifact := false
    )

lazy val `akka-http-argonaut`=
  project
    .enablePlugins(AutomateHeaderPlugin)
    .settings(settings)
    .settings(
      libraryDependencies ++= Seq(
        library.akkaHttp,
        library.akkaStream,
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
        library.akkaStream,
        library.circe,
        library.circeParser,
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
        library.akkaStream,
        library.akkaHttpJacksonJava,
        library.jacksonModuleScala,
        "org.scala-lang" % "scala-reflect" % scalaVersion.value,
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
        library.akkaStream,
        library.json4sCore,
        library.json4sJackson % Test,
        library.json4sNative  % Test,
        library.scalaTest     % Test
      )
    )

lazy val `akka-http-jsoniter-scala` =
  project
    .enablePlugins(AutomateHeaderPlugin)
    .settings(settings)
    .settings(
      libraryDependencies ++= Seq(
        library.akkaHttp,
        library.akkaStream,
        library.jsoniterScalaCore,
        library.jsoniterScalaMacros % Test,
        library.scalaTest % Test
      )
    )

lazy val `akka-http-play-json` =
  project
    .enablePlugins(AutomateHeaderPlugin)
    .settings(settings)
    .settings(
      libraryDependencies ++= Seq(
        library.akkaHttp,
        library.akkaStream,
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
        library.akkaStream,
        library.upickle,
        library.scalaTest % Test
      )
    )

lazy val `akka-http-avro4s` =
  project
    .enablePlugins(AutomateHeaderPlugin)
    .settings(settings)
    .settings(
      libraryDependencies ++= Seq(
        library.akkaHttp,
        library.akkaStream,
        library.avro4sJson,
        library.scalaTest     % Test
      )
    )

// lazy val `akka-http-avsystem-gencodec` =
//   project
//   .enablePlugins(AutomateHeaderPlugin)
//   .settings(settings)
//   .settings(
//     libraryDependencies ++= Seq(
//       library.akkaHttp,
//       library.akkaStream,
//       library.avsystemCommons,
//       library.scalaTest % Test
//     )
//   )

// *****************************************************************************
// Library dependencies
// *****************************************************************************

lazy val library =
  new {
    object Version {
      val akka               = "2.6.1"
      val akkaHttp           = "10.1.11"
      val argonaut           = "6.2.3"
      val avro4s             = "3.0.4"
      val circe              = "0.12.3"
      val jacksonModuleScala = "2.10.2"
      val jsoniterScala      = "2.1.3"
      val json4s             = "3.6.7"
      val play               = "2.8.1"
      val scalaTest          = "3.1.0"
      val upickle            = "0.9.7"
      val avsystemCommons    = "1.40.0"
    }
    val akkaHttp            = "com.typesafe.akka"                     %% "akka-http"             % Version.akkaHttp
    val akkaHttpJacksonJava = "com.typesafe.akka"                     %% "akka-http-jackson"     % Version.akkaHttp
    val akkaStream          = "com.typesafe.akka"                     %% "akka-stream"           % Version.akka
    val argonaut            = "io.argonaut"                           %% "argonaut"              % Version.argonaut
    val avro4sJson          = "com.sksamuel.avro4s"                   %% "avro4s-json"           % Version.avro4s
    val avsystemCommons     = "com.avsystem.commons"                  %% "commons-core"          % Version.avsystemCommons
    val circe               = "io.circe"                              %% "circe-core"            % Version.circe
    val circeParser         = "io.circe"                              %% "circe-parser"          % Version.circe
    val circeGeneric        = "io.circe"                              %% "circe-generic"         % Version.circe
    val jacksonModuleScala  = "com.fasterxml.jackson.module"          %% "jackson-module-scala"  % Version.jacksonModuleScala
    val json4sCore          = "org.json4s"                            %% "json4s-core"           % Version.json4s
    val json4sJackson       = "org.json4s"                            %% "json4s-jackson"        % Version.json4s
    val json4sNative        = "org.json4s"                            %% "json4s-native"         % Version.json4s
    val jsoniterScalaCore   = "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core"   % Version.jsoniterScala
    val jsoniterScalaMacros = "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % Version.jsoniterScala
    val playJson            = "com.typesafe.play"                     %% "play-json"             % Version.play
    val scalaTest           = "org.scalatest"                         %% "scalatest"             % Version.scalaTest
    val upickle             = "com.lihaoyi"                           %% "upickle"               % Version.upickle
  }

// *****************************************************************************
// Settings
// *****************************************************************************

lazy val settings =
  commonSettings ++
  gitSettings ++
  scalafmtSettings ++
  publishSettings

lazy val commonSettings =
  Seq(
    scalaVersion := "2.13.1",
    crossScalaVersions := Seq(scalaVersion.value, "2.12.10"),
    organizationName := "Heiko Seeberger",
    startYear := Some(2015),
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-language:_",
      "-target:jvm-1.8",
      "-encoding", "UTF-8"
    ),
    Compile / unmanagedSourceDirectories := Seq((Compile / scalaSource).value),
    Test / unmanagedSourceDirectories := Seq((Test / scalaSource).value)
)

lazy val gitSettings =
  Seq(
    git.useGitDescribe := true
  )

lazy val scalafmtSettings =
  Seq(
    scalafmtOnCompile := true
  )

lazy val publishSettings =
  Seq(
    pomIncludeRepository := (_ => false),
  )
