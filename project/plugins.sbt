resolvers += Resolver.bintrayIvyRepo("sbt", "sbt-plugin-releases")

addSbtPlugin("com.geirsson"      % "sbt-ci-release"  % "1.5.0")
addSbtPlugin("com.typesafe"      % "sbt-mima-plugin" % "0.6.4")
addSbtPlugin("de.heikoseeberger" % "sbt-header"      % "5.4.0")
addSbtPlugin("org.scalameta"     % "sbt-scalafmt"    % "2.3.1")
