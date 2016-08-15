logLevel := Level.Warn

//resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"
//libraryDependencies += "com.spotify" % "docker-client" % "3.5.13"

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.1.4")

//addSbtPlugin("com.softwaremill.clippy" % "plugin-sbt" % "0.3.0")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")

//lazy val root = Project("plugins", file(".")).dependsOn(plugin)

//lazy val plugin = file("../").getCanonicalFile.toURI
