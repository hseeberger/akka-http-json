import bintray.BintrayPlugin
import bintray.BintrayPlugin.autoImport._
import com.typesafe.sbt.GitPlugin
import com.typesafe.sbt.GitPlugin.autoImport._
import com.typesafe.sbt.SbtPgp
import de.heikoseeberger.sbtheader.HeaderPlugin
import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport._
import de.heikoseeberger.sbtheader.license.Apache2_0
import org.scalafmt.sbt.ScalaFmtPlugin
import org.scalafmt.sbt.ScalaFmtPlugin.autoImport._
import sbt._
import sbt.plugins.JvmPlugin
import sbt.Keys._

object Build extends AutoPlugin {

  override def requires =
    JvmPlugin && HeaderPlugin && GitPlugin && ScalaFmtPlugin && BintrayPlugin

  override def trigger = allRequirements

  override def projectSettings =
    reformatOnCompileSettings ++
    Vector(
      // Core settings
      organization := "de.heikoseeberger",
      licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
      scalaVersion := Version.Scala,
      crossScalaVersions := Vector(scalaVersion.value, "2.11.8"),
      scalacOptions ++= Vector(
        "-unchecked",
        "-deprecation",
        "-language:_",
        "-target:jvm-1.8",
        "-encoding", "UTF-8"
      ),
      unmanagedSourceDirectories.in(Compile) := Vector(scalaSource.in(Compile).value),
      unmanagedSourceDirectories.in(Test) := Vector(scalaSource.in(Test).value),
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

      // scalafmt settings
      formatSbtFiles := false,
      scalafmtConfig := Some(baseDirectory.in(ThisBuild).value / ".scalafmt.conf"),
      ivyScala       := ivyScala.value.map(_.copy(overrideScalaVersion = sbtPlugin.value)), // TODO Remove once this workaround no longer needed (https://github.com/sbt/sbt/issues/2786)!

      // Git settings
      git.useGitDescribe := true,

      // Header settings
      headers := Map("scala" -> Apache2_0("2015", "Heiko Seeberger")),

      // Bintray settings
      bintrayPackage := "akka-http-json"
    )
}
