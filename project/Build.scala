import com.typesafe.sbt.SbtGit._
import com.typesafe.sbt.SbtScalariform._
import de.heikoseeberger.sbtheader.SbtHeader.autoImport._
import sbt._
import sbt.Keys._
import scalariform.formatter.preferences._

object Build extends AutoPlugin {

  override def requires = plugins.JvmPlugin

  override def trigger = allRequirements

  override def projectSettings =
    scalariformSettings ++
    versionWithGit ++
    inConfig(Compile)(compileInputs.in(compile) <<= compileInputs.in(compile).dependsOn(createHeaders.in(compile))) ++
    inConfig(Test)(compileInputs.in(compile) <<= compileInputs.in(compile).dependsOn(createHeaders.in(compile))) ++
    List(
      // Core settings
      organization := "de.heikoseeberger",
      scalaVersion := Version.scala,
      crossScalaVersions := List(scalaVersion.value),
      scalacOptions ++= List(
        "-unchecked",
        "-deprecation",
        "-language:_",
        "-target:jvm-1.7",
        "-encoding", "UTF-8"
      ),
      unmanagedSourceDirectories in Compile := List((scalaSource in Compile).value),
      unmanagedSourceDirectories in Test := List((scalaSource in Test).value),
      // Scalariform settings
      ScalariformKeys.preferences := ScalariformKeys.preferences.value
        .setPreference(AlignArguments, true)
        .setPreference(AlignParameters, true)
        .setPreference(AlignSingleLineCaseStatements, true)
        .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
        .setPreference(DoubleIndentClassDeclaration, true),
      // Git settings
      git.baseVersion := "0.1.0",
      // Header settings
      headers := Map(
        "scala" -> (
          HeaderPattern.cStyleBlockComment,
          """|/*
             | * Copyright 2015 Heiko Seeberger
             | *
             | * Licensed under the Apache License, Version 2.0 (the "License");
             | * you may not use this file except in compliance with the License.
             | * You may obtain a copy of the License at
             | *
             | *    http://www.apache.org/licenses/LICENSE-2.0
             | *
             | * Unless required by applicable law or agreed to in writing, software
             | * distributed under the License is distributed on an "AS IS" BASIS,
             | * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
             | * See the License for the specific language governing permissions and
             | * limitations under the License.
             | */
             |
             |""".stripMargin
        )
      )
    )
}
