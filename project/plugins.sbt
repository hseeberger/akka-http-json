resolvers += Resolver.bintrayIvyRepo("sbt", "sbt-plugin-releases")

addSbtPlugin("com.geirsson"      % "sbt-ci-release"  % "1.5.2")
addSbtPlugin("com.typesafe"      % "sbt-mima-plugin" % "0.7.0")
addSbtPlugin("de.heikoseeberger" % "sbt-header"      % "5.5.0")
addSbtPlugin("org.scalameta"     % "sbt-scalafmt"    % "2.3.2")
