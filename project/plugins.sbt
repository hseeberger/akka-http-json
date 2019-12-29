addSbtPlugin("com.geirsson"      % "sbt-ci-release" % "1.5.0")
addSbtPlugin("com.geirsson"      % "sbt-scalafmt"   % "1.5.1")
addSbtPlugin("de.heikoseeberger" % "sbt-header"     % "5.3.1")

libraryDependencies += "org.slf4j" % "slf4j-nop" % "1.7.30" // Needed by sbt-git
