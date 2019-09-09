addSbtPlugin("com.geirsson"      % "sbt-ci-release" % "1.3.1")
addSbtPlugin("org.scalameta"      % "sbt-scalafmt"   % "2.0.0")
addSbtPlugin("de.heikoseeberger" % "sbt-header"     % "5.2.0")

libraryDependencies += "org.slf4j" % "slf4j-nop" % "1.7.28" // Needed by sbt-git
