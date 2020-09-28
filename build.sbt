inThisBuild(
  Seq(
    organization := "de.heikoseeberger",
    homepage := Some(url("https://github.com/hseeberger/akka-http-json")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
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
  )
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
    )
    .settings(commonSettings)
    .settings(
      Compile / unmanagedSourceDirectories := Seq.empty,
      Test / unmanagedSourceDirectories := Seq.empty,
      publishArtifact := false,
    )

lazy val `akka-http-argonaut` =
  project
    .enablePlugins(AutomateHeaderPlugin)
    .settings(commonSettings)
    .settings(
      libraryDependencies ++= Seq(
        library.akkaHttp,
        library.argonaut,
        library.akkaStream % Provided,
        library.scalaTest  % Test,
      ),
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
      ),
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
        library.scalaReflect % scalaVersion.value,
        library.akkaStream   % Provided,
        library.scalaTest    % Test,
      ),
    )

lazy val `akka-http-json4s` =
  project
    .enablePlugins(AutomateHeaderPlugin)
    .settings(commonSettings)
    .settings(
      libraryDependencies ++= Seq(
        library.akkaHttp,
        library.json4sCore,
        library.akkaStream    % Provided,
        library.json4sJackson % Test,
        library.json4sNative  % Test,
        library.scalaTest     % Test,
      ),
    )

lazy val `akka-http-jsoniter-scala` =
  project
    .enablePlugins(AutomateHeaderPlugin)
    .settings(commonSettings)
    .settings(
      libraryDependencies ++= Seq(
        library.akkaHttp,
        library.jsoniterScalaCore,
        library.akkaStream          % Provided,
        library.jsoniterScalaMacros % Test,
        library.scalaTest           % Test,
      ),
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
      ),
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
      ),
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
      ),
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
      ),
    )

// *****************************************************************************
// Library dependencies
// *****************************************************************************

lazy val library =
  new {
    object Version {
      val akka               = "2.6.9"
      val akkaHttp           = "10.2.0"
      val argonaut           = "6.3.1"
      val avro4s             = "3.1.0"
      val circe              = "0.13.0"
      val jacksonModuleScala = "2.11.2"
      val jsoniterScala      = "2.6.0"
      val json4s             = "3.6.9"
      val ninny              = "0.2.0"
      val play               = "2.9.1"
      val scalaTest          = "3.2.2"
      val upickle            = "1.2.0"
    }
    val akkaHttp            = "com.typesafe.akka"                     %% "akka-http"             % Version.akkaHttp
    val akkaHttpJacksonJava = "com.typesafe.akka"                     %% "akka-http-jackson"     % Version.akkaHttp
    val akkaStream          = "com.typesafe.akka"                     %% "akka-stream"           % Version.akka
    val argonaut            = "io.argonaut"                           %% "argonaut"              % Version.argonaut
    val avro4sJson          = "com.sksamuel.avro4s"                   %% "avro4s-json"           % Version.avro4s
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
    val ninny               = "io.github.kag0"                        %% "ninny"                 % Version.ninny
    val scalaTest           = "org.scalatest"                         %% "scalatest"             % Version.scalaTest
    val upickle             = "com.lihaoyi"                           %% "upickle"               % Version.upickle
    val scalaReflect        = "org.scala-lang"                        %  "scala-reflect"
  }

// *****************************************************************************
// Settings
// *****************************************************************************

lazy val commonSettings =
  Seq(
    scalaVersion := "2.13.3",
    crossScalaVersions := Seq(scalaVersion.value, "2.12.12"),
    organizationName := "Heiko Seeberger",
    startYear := Some(2015),
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-language:_",
      "-target:jvm-1.8",
      "-encoding",
      "UTF-8",
    ),
    Compile / unmanagedSourceDirectories := Seq((Compile / scalaSource).value),
    Test / unmanagedSourceDirectories := Seq((Test / scalaSource).value),
    mimaPreviousArtifacts := previousStableVersion.value.map(organization.value %% name.value % _).toSet,
    git.useGitDescribe := true,
    scalafmtOnCompile := true,
    pomIncludeRepository := (_ => false),
  )
