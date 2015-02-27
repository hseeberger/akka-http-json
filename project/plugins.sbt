addSbtPlugin("com.danieltrinh"   % "sbt-scalariform" % "1.3.0")
addSbtPlugin("com.typesafe.sbt"  % "sbt-git"         % "0.6.4")
addSbtPlugin("de.heikoseeberger" % "sbt-header"      % "1.1.1")
addSbtPlugin("me.lessis"         % "bintray-sbt"     % "0.1.2")

resolvers += Resolver.url("bintraysbt-plugin-releases", url("http://dl.bintray.com/content/sbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)
