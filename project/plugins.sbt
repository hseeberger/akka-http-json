resolvers += Resolver.bintrayIvyRepo("sbt", "sbt-plugin-releases")

addSbtPlugin("com.github.sbt"    % "sbt-ci-release"  % "1.5.10")
addSbtPlugin("com.typesafe"      % "sbt-mima-plugin" % "1.1.1")
addSbtPlugin("de.heikoseeberger" % "sbt-header"      % "5.6.5")
addSbtPlugin("org.scalameta"     % "sbt-scalafmt"    % "2.4.6")
