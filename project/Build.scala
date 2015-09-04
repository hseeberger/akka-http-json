import bintray.BintrayKeys
import com.typesafe.sbt.GitPlugin
import com.typesafe.sbt.SbtPgp
import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import de.heikoseeberger.sbtheader.HeaderPlugin
import de.heikoseeberger.sbtheader.license.Apache2_0
import sbt._
import sbt.Keys._
import scalariform.formatter.preferences._

object Build extends AutoPlugin {

  override def requires = plugins.JvmPlugin && HeaderPlugin && GitPlugin && SbtPgp

  override def trigger = allRequirements

  override def projectSettings =
    // Core settings
    List(
      organization := "de.heikoseeberger",
      licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
      homepage := Some(url("https://github.com/hseeberger/akka-http-json")),
      pomIncludeRepository := (_ => false),
      pomExtra := <scm>
                    <url>https://github.com/hseeberger/akka-http-json</url>
                    <connection>scm:git:git@github.com:hseeberger/akka-http-json.git</connection>
                  </scm>
                  <developers>
                    <developer>
                      <id>hseeberger</id>
                      <name>Heiko Seeberger</name>
                      <url>http://heikoseeberger.de</url>
                    </developer>
                  </developers>,
      scalaVersion := Version.scala,
      crossScalaVersions := List(scalaVersion.value),
      scalacOptions ++= List(
        "-unchecked",
        "-deprecation",
        "-language:_",
        "-target:jvm-1.7",
        "-encoding", "UTF-8"
      ),
      unmanagedSourceDirectories.in(Compile) := List(scalaSource.in(Compile).value),
      unmanagedSourceDirectories.in(Test) := List(scalaSource.in(Test).value)
    ) ++
    // Scalariform settings
    List(
      SbtScalariform.autoImport.preferences := SbtScalariform.autoImport.preferences.value
        .setPreference(AlignSingleLineCaseStatements, true)
        .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
        .setPreference(DoubleIndentClassDeclaration, true)
    ) ++
    // Git settings
    List(
      GitPlugin.autoImport.git.baseVersion := "1.2.0"
    ) ++
    // Header settings
    List(
      HeaderPlugin.autoImport.headers := Map("scala" -> Apache2_0("2015", "Heiko Seeberger"))
    ) ++
    // Bintray settings
    List (
      BintrayKeys.bintrayPackage := "akka-http-json"
    )
}
