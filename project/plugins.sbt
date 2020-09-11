resolvers += Resolver.bintrayIvyRepo("sbt", "sbt-plugin-releases")

addSbtPlugin("com.geirsson"      % "sbt-ci-release"  % "1.5.3")
addSbtPlugin("com.typesafe"      % "sbt-mima-plugin" % "0.8.0")
addSbtPlugin("de.heikoseeberger" % "sbt-header"      % "5.6.0")
addSbtPlugin("org.scalameta"     % "sbt-scalafmt"    % "2.4.2")
