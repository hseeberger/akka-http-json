addSbtPlugin("com.geirsson"      % "sbt-ci-release" % "1.2.1")
addSbtPlugin("com.geirsson"      % "sbt-scalafmt"   % "1.5.0")
addSbtPlugin("de.heikoseeberger" % "sbt-header"     % "5.1.0")

libraryDependencies += "org.slf4j" % "slf4j-nop" % "1.7.25" // Needed by sbt-git
