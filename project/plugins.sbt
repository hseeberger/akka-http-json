addSbtPlugin("com.geirsson"      % "sbt-ci-release" % "1.3.1")
addSbtPlugin("com.geirsson"      % "sbt-scalafmt"   % "1.5.1")
addSbtPlugin("de.heikoseeberger" % "sbt-header"     % "5.2.0")

libraryDependencies += "org.slf4j" % "slf4j-nop" % "1.7.28" // Needed by sbt-git
