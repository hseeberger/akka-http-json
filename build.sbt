// *****************************************************************************
// Build settings
// *****************************************************************************

inThisBuild(
  Seq(
    organization     := "de.heikoseeberger",
    organizationName := "Heiko Seeberger",
    startYear        := Some(2015),
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
    homepage := Some(url("https://github.com/hseeberger/akka-http-json")),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/hseeberger/akka-http-json"),
        "git@github.com:hseeberger/akka-http-json.git"
      )
    ),
    developers := List(
      Developer(
        "hseeberger",
        "Heiko Seeberger",
        "mail@heikoseeberger.de",
        url("https://github.com/hseeberger")
      )
    ),
    scalaVersion       := "2.13.10",
    crossScalaVersions := Seq(scalaVersion.value, "2.12.17"),
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-language:_",
      "-encoding",
      "UTF-8",
      "-Ywarn-unused:imports",
      "-target:jvm-1.8"
    ),
    scalafmtOnCompile := true,
    dynverSeparator   := "_" // the default `+` is not compatible with docker tags,
  )
)

val withScala3 = Seq(
  crossScalaVersions += "3.2.1",
)

// *****************************************************************************
// Projects
// *****************************************************************************

lazy val `akka-http-json` =
  project
    .in(file("."))
    .disablePlugins(MimaPlugin)
    .aggregate(
      `akka-http-argonaut`,
      `akka-http-avro4s`,
      `akka-http-circe`,
      `akka-http-jackson`,
      `akka-http-json4s`,
      `akka-http-jsoniter-scala`,
      `akka-http-ninny`,
      `akka-http-play-json`,
      `akka-http-upickle`,
      `akka-http-zio-json`,
    )
    .settings(commonSettings)
    .settings(
      Compile / unmanagedSourceDirectories := Seq.empty,
      Test / unmanagedSourceDirectories    := Seq.empty,
      publishArtifact                      := false,
    )

lazy val `akka-http-argonaut` =
  project
    .enablePlugins(AutomateHeaderPlugin)
    .settings(commonSettings, withScala3)
    .settings(
      libraryDependencies ++= Seq(
        library.akkaHttp,
        library.argonaut,
        library.akkaStream % Provided,
        library.scalaTest  % Test,
      )
    )

lazy val `akka-http-circe` =
  project
    .enablePlugins(AutomateHeaderPlugin)
    .settings(commonSettings)
    .settings(
      libraryDependencies ++= Seq(
        library.akkaHttp,
        library.circe,
        library.circeParser,
        library.akkaStream   % Provided,
        library.circeGeneric % Test,
        library.scalaTest    % Test,
      )
    )

lazy val `akka-http-jackson` =
  project
    .enablePlugins(AutomateHeaderPlugin)
    .settings(commonSettings)
    .settings(
      libraryDependencies ++= Seq(
        library.akkaHttp,
        library.akkaHttpJacksonJava,
        library.jacksonModuleScala,
        "org.scala-lang"   % "scala-reflect" % scalaVersion.value,
        library.akkaStream % Provided,
        library.scalaTest  % Test,
      )
    )

lazy val `akka-http-json4s` =
  project
    .enablePlugins(AutomateHeaderPlugin)
    .settings(commonSettings, withScala3)
    .settings(
      libraryDependencies ++= Seq(
        library.akkaHttp,
        library.json4sCore,
        library.akkaStream    % Provided,
        library.json4sJackson % Test,
        library.json4sNative  % Test,
        library.scalaTest     % Test,
      )
    )

lazy val `akka-http-jsoniter-scala` =
  project
    .enablePlugins(AutomateHeaderPlugin)
    .settings(commonSettings, withScala3)
    .settings(
      libraryDependencies ++= Seq(
        library.akkaHttp,
        library.jsoniterScalaCore,
        library.akkaStream          % Provided,
        library.jsoniterScalaMacros % Test,
        library.scalaTest           % Test,
      )
    )

lazy val `akka-http-ninny` =
  project
    .enablePlugins(AutomateHeaderPlugin)
    .settings(commonSettings)
    .settings(
      libraryDependencies ++= Seq(
        library.akkaHttp,
        library.ninny,
        library.akkaStream % Provided,
        library.scalaTest  % Test,
      )
    )

lazy val `akka-http-play-json` =
  project
    .enablePlugins(AutomateHeaderPlugin)
    .settings(commonSettings)
    .settings(
      libraryDependencies ++= Seq(
        library.akkaHttp,
        library.playJson,
        library.akkaStream % Provided,
        library.scalaTest  % Test,
      )
    )

lazy val `akka-http-upickle` =
  project
    .enablePlugins(AutomateHeaderPlugin)
    .settings(commonSettings)
    .settings(
      libraryDependencies ++= Seq(
        library.akkaHttp,
        library.upickle,
        library.akkaStream % Provided,
        library.scalaTest  % Test,
      )
    )

lazy val `akka-http-avro4s` =
  project
    .enablePlugins(AutomateHeaderPlugin)
    .settings(commonSettings)
    .settings(
      libraryDependencies ++= Seq(
        library.akkaHttp,
        library.avro4sJson,
        library.akkaStream % Provided,
        library.scalaTest  % Test,
      )
    )

lazy val `akka-http-zio-json` =
  project
    .enablePlugins(AutomateHeaderPlugin)
    .settings(commonSettings, withScala3)
    .settings(
      libraryDependencies ++= Seq(
        library.akkaHttp,
        library.zioJson,
        library.akkaStream % Provided,
        library.scalaTest  % Test
      )
    )

// *****************************************************************************
// Project settings
// *****************************************************************************

lazy val commonSettings =
  Seq(
    // Also (automatically) format build definition together with sources
    Compile / scalafmt := {
      val _ = (Compile / scalafmtSbt).value
      (Compile / scalafmt).value
    }
  )

// *****************************************************************************
// Library dependencies
// *****************************************************************************

lazy val library =
  new {
    object Version {
      val akka               = "2.6.20"
      val akkaHttp           = "10.2.10"
      val argonaut           = "6.3.8"
      val avro4s             = "4.0.12"
      val circe              = "0.14.1"
      val jacksonModuleScala = "2.13.4"
      val json4s             = "4.0.6"
      val jsoniterScala      = "2.17.9"
      val ninny              = "0.7.0"
      val play               = "2.9.2"
      val scalaTest          = "3.2.11"
      val upickle            = "1.5.0"
      val zioJson            = "0.3.0"
    }
    // format: off
    val akkaHttp            = ("com.typesafe.akka"                     %% "akka-http"             % Version.akkaHttp).cross(CrossVersion.for3Use2_13)
    val akkaHttpJacksonJava = ("com.typesafe.akka"                     %% "akka-http-jackson"     % Version.akkaHttp).cross(CrossVersion.for3Use2_13)
    val akkaStream          = "com.typesafe.akka"                     %% "akka-stream"           % Version.akka
    val argonaut            = "io.argonaut"                           %% "argonaut"              % Version.argonaut
    val avro4sJson          = "com.sksamuel.avro4s"                   %% "avro4s-json"           % Version.avro4s
    val circe               = "io.circe"                              %% "circe-core"            % Version.circe
    val circeGeneric        = "io.circe"                              %% "circe-generic"         % Version.circe
    val circeParser         = "io.circe"                              %% "circe-parser"          % Version.circe
    val jacksonModuleScala  = "com.fasterxml.jackson.module"          %% "jackson-module-scala"  % Version.jacksonModuleScala
    val json4sCore          = "org.json4s"                            %% "json4s-core"           % Version.json4s
    val json4sJackson       = "org.json4s"                            %% "json4s-jackson"        % Version.json4s
    val json4sNative        = "org.json4s"                            %% "json4s-native"         % Version.json4s
    val jsoniterScalaCore   = "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core"   % Version.jsoniterScala
    val jsoniterScalaMacros = "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % Version.jsoniterScala
    val ninny               = "tk.nrktkt"                             %% "ninny"                 % Version.ninny
    val playJson            = "com.typesafe.play"                     %% "play-json"             % Version.play
    val scalaTest           = "org.scalatest"                         %% "scalatest"             % Version.scalaTest
    val upickle             = "com.lihaoyi"                           %% "upickle"               % Version.upickle
    val zioJson             = "dev.zio"                               %% "zio-json"              % Version.zioJson
    // format: on
  }
