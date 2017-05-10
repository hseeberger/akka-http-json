addSbtPlugin("com.dwijnand"      % "sbt-travisci" % "1.1.0")
addSbtPlugin("com.geirsson"      % "sbt-scalafmt" % "0.6.6")
addSbtPlugin("com.jsuereth"      % "sbt-pgp"      % "1.0.0")
addSbtPlugin("com.typesafe.sbt"  % "sbt-git"      % "0.9.2")
addSbtPlugin("de.heikoseeberger" % "sbt-header"   % "2.0.0")
addSbtPlugin("me.lessis"         % "bintray-sbt"  % "0.3.0")

libraryDependencies += "org.slf4j" % "slf4j-nop" % "1.7.25" // Needed by sbt-git
