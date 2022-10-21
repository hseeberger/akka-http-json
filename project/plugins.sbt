resolvers += Resolver.bintrayIvyRepo("sbt", "sbt-plugin-releases")

addSbtPlugin("com.github.sbt"    % "sbt-ci-release"  % "1.5.11")
addSbtPlugin("com.typesafe"      % "sbt-mima-plugin" % "1.0.1")
addSbtPlugin("de.heikoseeberger" % "sbt-header"      % "5.6.5")
addSbtPlugin("org.scalameta"     % "sbt-scalafmt"    % "2.4.6")
