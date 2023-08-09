// *****************************************************************************
// Build settings
// *****************************************************************************

inThisBuild(
  Seq(
    organization := "com.github.pjfanning",
    startYear    := Some(2023),
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
    homepage := Some(url("https://github.com/pjfanning/pekko-http-json")),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/pjfanning/pekko-http-json"),
        "git@github.com:pjfanning/pekko-http-json.git"
      )
    ),
    developers := List(
      Developer(
        "hseeberger",
        "Heiko Seeberger",
        "mail@heikoseeberger.de",
        url("https://github.com/hseeberger")
      ),
      Developer(
        "pjfanning",
        "PJ Fanning",
        "",
        url("https://github.com/pjfanning")
      ),
    ),
    scalaVersion       := "2.13.11",
    crossScalaVersions := Seq(scalaVersion.value, "2.12.18"),
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
    dynverSeparator   := "_" // the default `+` is not compatible with docker tags
  )
)

val withScala3 = Seq(
  crossScalaVersions += "3.3.0",
)

// *****************************************************************************
// Projects
// *****************************************************************************

lazy val `pekko-http-json` =
  project
    .in(file("."))
    .disablePlugins(MimaPlugin)
    .aggregate(
      `pekko-http-argonaut`,
      `pekko-http-avro4s`,
      `pekko-http-circe`,
      `pekko-http-jackson`,
      `pekko-http-json4s`,
      `pekko-http-jsoniter-scala`,
      `pekko-http-ninny`,
      `pekko-http-play-json`,
      `pekko-http-upickle`,
      `pekko-http-zio-json`,
    )
    .settings(commonSettings)
    .settings(
      Compile / unmanagedSourceDirectories := Seq.empty,
      Test / unmanagedSourceDirectories    := Seq.empty,
      publishArtifact                      := false,
    )

lazy val `pekko-http-argonaut` =
  project
    .settings(commonSettings, withScala3)
    .settings(
      libraryDependencies ++= Seq(
        library.pekkoHttp,
        library.argonaut,
        library.pekkoStream % Provided,
        library.scalaTest   % Test,
      )
    )

lazy val `pekko-http-circe` =
  project
    .settings(commonSettings, withScala3)
    .settings(
      libraryDependencies ++= Seq(
        library.pekkoHttp,
        library.circe,
        library.circeParser,
        library.pekkoStream  % Provided,
        library.circeGeneric % Test,
        library.scalaTest    % Test,
      )
    )

lazy val `pekko-http-jackson` =
  project
    .settings(commonSettings, withScala3)
    .settings(
      libraryDependencies ++= Seq(
        library.pekkoHttp,
        library.pekkoHttpJacksonJava,
        library.jacksonModuleScala,
        library.pekkoStream % Provided,
        library.scalaTest   % Test,
      )
    )

lazy val `pekko-http-json4s` =
  project
    .settings(commonSettings, withScala3)
    .settings(
      libraryDependencies ++= Seq(
        library.pekkoHttp,
        library.json4sCore,
        library.pekkoStream   % Provided,
        library.json4sJackson % Test,
        library.json4sNative  % Test,
        library.scalaTest     % Test,
      )
    )

lazy val `pekko-http-jsoniter-scala` =
  project
    .settings(commonSettings, withScala3)
    .settings(
      libraryDependencies ++= Seq(
        library.pekkoHttp,
        library.jsoniterScalaCore,
        library.pekkoStream         % Provided,
        library.jsoniterScalaMacros % Test,
        library.scalaTest           % Test,
      )
    )

lazy val `pekko-http-ninny` =
  project
    .settings(commonSettings)
    .settings(
      libraryDependencies ++= Seq(
        library.pekkoHttp,
        library.ninny,
        library.pekkoStream % Provided,
        library.scalaTest   % Test,
      )
    )

lazy val `pekko-http-play-json` =
  project
    .settings(commonSettings)
    .settings(
      libraryDependencies ++= Seq(
        library.pekkoHttp,
        library.playJson,
        library.pekkoStream % Provided,
        library.scalaTest   % Test,
      )
    )

lazy val `pekko-http-upickle` =
  project
    .settings(commonSettings, withScala3)
    .settings(
      libraryDependencies ++= Seq(
        library.pekkoHttp,
        library.upickle,
        library.pekkoStream % Provided,
        library.scalaTest   % Test,
      )
    )

lazy val `pekko-http-avro4s` =
  project
    .settings(commonSettings)
    .settings(
      libraryDependencies ++= Seq(
        library.pekkoHttp,
        library.avro4sJson,
        library.pekkoStream % Provided,
        library.scalaTest   % Test,
      )
    )

lazy val `pekko-http-zio-json` =
  project
    .settings(commonSettings, withScala3)
    .settings(
      libraryDependencies ++= Seq(
        library.pekkoHttp,
        library.zioJson,
        library.pekkoStream % Provided,
        library.scalaTest   % Test
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
      val pekko              = "1.0.1"
      val pekkoHttp          = "1.0.0"
      val argonaut           = "6.3.8"
      val avro4s             = "4.0.12"
      val circe              = "0.14.1"
      val jacksonModuleScala = "2.15.2"
      val json4s             = "4.0.6"
      val jsoniterScala      = "2.17.9"
      val ninny              = "0.7.0"
      val play               = "2.9.2"
      val scalaTest          = "3.2.16"
      val upickle            = "1.6.0"
      val zioJson            = "0.3.0"
    }
    // format: off
    val pekkoHttp            = "org.apache.pekko"                      %% "pekko-http"            % Version.pekkoHttp
    val pekkoHttpJacksonJava = "org.apache.pekko"                      %% "pekko-http-jackson"    % Version.pekkoHttp
    val pekkoStream          = "org.apache.pekko"                      %% "pekko-stream"          % Version.pekko
    val argonaut             = "io.argonaut"                           %% "argonaut"              % Version.argonaut
    val avro4sJson           = "com.sksamuel.avro4s"                   %% "avro4s-json"           % Version.avro4s
    val circe                = "io.circe"                              %% "circe-core"            % Version.circe
    val circeGeneric         = "io.circe"                              %% "circe-generic"         % Version.circe
    val circeParser          = "io.circe"                              %% "circe-parser"          % Version.circe
    val jacksonModuleScala   = "com.fasterxml.jackson.module"          %% "jackson-module-scala"  % Version.jacksonModuleScala
    val json4sCore           = "org.json4s"                            %% "json4s-core"           % Version.json4s
    val json4sJackson        = "org.json4s"                            %% "json4s-jackson"        % Version.json4s
    val json4sNative         = "org.json4s"                            %% "json4s-native"         % Version.json4s
    val jsoniterScalaCore    = "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core"   % Version.jsoniterScala
    val jsoniterScalaMacros  = "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % Version.jsoniterScala
    val ninny                = "tk.nrktkt"                             %% "ninny"                 % Version.ninny
    val playJson             = "com.typesafe.play"                     %% "play-json"             % Version.play
    val scalaTest            = "org.scalatest"                         %% "scalatest"             % Version.scalaTest
    val upickle              = "com.lihaoyi"                           %% "upickle"               % Version.upickle
    val zioJson              = "dev.zio"                               %% "zio-json"              % Version.zioJson
    // format: on
  }
