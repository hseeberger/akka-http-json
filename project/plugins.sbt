// addSbtPlugin("com.dwijnand"      % "sbt-travisci" % "1.1.0")
addSbtPlugin("com.jsuereth"      % "sbt-pgp"      % "1.1.0-M1")
addSbtPlugin("com.lucidchart"    % "sbt-scalafmt" % "1.10")
addSbtPlugin("com.typesafe.sbt"  % "sbt-git"      % "0.9.3")
addSbtPlugin("de.heikoseeberger" % "sbt-header"   % "3.0.0")
addSbtPlugin("org.foundweekends" % "sbt-bintray"  % "0.5.1")

libraryDependencies += "org.slf4j" % "slf4j-nop" % "1.7.25" // Needed by sbt-git
