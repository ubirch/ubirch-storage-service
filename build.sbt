import sbt.Keys._

scalaVersion in ThisBuild := "2.11.8"

maintainer := "Michael Merz <dermicha@ubirch.com>"

name := "ubirchStorageService"

version := "1.0"

lazy val testConfiguration = "-Dconfig.resource=" + Option(System.getProperty("test.config")).getOrElse("application.dev.conf")

lazy val commonSettings = Seq(
  organization := "com.ubirch",
  version := "0.0.1",
  test in assembly := {},
  parallelExecution in ThisBuild := false,
  javaOptions in Test += testConfiguration,
  fork in Test := true,
  // in ThisBuild is important to run tests of each subproject sequential instead parallelizing them
  testOptions in ThisBuild ++= Seq(
    Tests.Argument(TestFrameworks.ScalaTest, "-u", "target/test-reports"),
    Tests.Argument(TestFrameworks.ScalaTest, "-o")
  )
)

lazy val moduleServer = (project in file("module-server"))
  .settings(commonSettings: _*)
  .settings(mergeStrategy: _*)
  .settings(libraryDependencies ++= (commonDependencies ++ akkaHttpDependencies))
  .dependsOn(moduleShare)
  .dependsOn(moduleCore)
  .dependsOn(moduleModel)

lazy val moduleClient = (project in file("module-client"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= commonDependencies)
  .dependsOn(moduleShare)
  .dependsOn(moduleCore)
  .dependsOn(moduleModel)

lazy val moduleCore = (project in file("module-core"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= commonDependencies ++ akkaDependencies ++ apacheHttpDependencies ++ testDependencies)
  .dependsOn(moduleShare)
  .dependsOn(moduleModel)
  .dependsOn(moduleUbirchShare)

lazy val moduleShare = (project in file("module-share"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= commonDependencies ++ testDependencies)
  .dependsOn(moduleModel)
  .dependsOn(moduleUbirchShare)

lazy val moduleModel = (project in file("module-model"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= commonDependencies ++ testDependencies)
  .dependsOn(moduleUbirchShare)

lazy val moduleUbirchShare = (project in file("module-ubirch-share"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= commonDependencies ++ mqttDependencies ++ beeHttpDependencies ++ hasherDependencies ++ testDependencies)

val scalaV = "2.11.8"
val akkaV = "2.4.8"
val scalaTestV = "2.2.6"
val json4sV = "3.4.0"
val configV = "1.3.0"
val elasticV = "2.3.5"

resolvers += Resolver.bintrayRepo("rick-beton", "maven")
resolvers += Resolver.sonatypeRepo("snapshots")
resolvers += Resolver.sonatypeRepo("releases")

lazy val commonDependencies = Seq(
  //scala
  "org.scala-lang" % "scala-compiler" % scalaV,
  "org.scala-lang" % "scala-library" % scalaV,
  "org.scala-lang" % "scala-reflect" % scalaV,
  "org.scala-lang.modules" %% "scala-xml" % "1.0.5",

  //json4s
  "org.json4s" %% "json4s-core" % json4sV,
  "org.json4s" %% "json4s-native" % json4sV,
  "org.json4s" %% "json4s-ast" % json4sV,
  "org.json4s" %% "json4s-ext" % json4sV,
  "org.json4s" %% "json4s-jackson" % json4sV,

  // app config
  "com.typesafe" % "config" % configV,

  // logging
  "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "ch.qos.logback" % "logback-core" % "1.1.3",
  "org.slf4j" % "slf4j-api" % "1.7.12",
  "com.internetitem" % "logback-elasticsearch-appender" % "1.3",

  //Joda DateTime
  "joda-time" % "joda-time" % "2.9.4"
)

lazy val testDependencies = Seq(
  "org.scalatest" %% "scalatest" % scalaTestV
)

lazy val akkaDependencies = Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaV,
  "com.typesafe.akka" %% "akka-testkit" % akkaV
)

lazy val akkaHttpDependencies = Seq(
  "com.typesafe.akka" %% "akka-http-testkit" % akkaV,
  "com.typesafe.akka" %% "akka-http-experimental" % akkaV,
  "de.heikoseeberger" %% "akka-http-json4s" % "1.8.0"
)

lazy val awsDependencies = Seq(
  "com.amazonaws" % "aws-iot-device-sdk-java" % "1.0.1"
)

lazy val mqttDependencies = Seq(
  "com.fasterxml.jackson.core" % "jackson-core" % "2.8.0",
  "org.eclipse.paho" % "org.eclipse.paho.client.mqttv3" % "1.1.0"
)

lazy val elasticDependencies = Seq(
  "org.elasticsearch" % "elasticsearch" % elasticV exclude("joda-time", "joda-time")
)

lazy val beeHttpDependencies = Seq(
  "uk.co.bigbeeconsultants" %% "bee-client" % "0.29.1"
)

lazy val hasherDependencies = Seq(
  "com.roundeights" %% "hasher" % "1.2.0"
)

lazy val apacheHttpDependencies = Seq(
  "org.apache.httpcomponents" % "httpclient" % "4.5.2"
)

lazy val ubirchUtilsDependencies = Seq(
  "com.ubirch.util" %% "crypto-util" % "0.1"
)

lazy val mergeStrategy = Seq(
  assemblyMergeStrategy in assembly := {
    case PathList("org", "joda", "time", xs@_*) => MergeStrategy.first
    case m if m.toLowerCase.endsWith("manifest.mf") => MergeStrategy.discard
    case m if m.toLowerCase.matches("meta-inf.*\\.sf$") => MergeStrategy.discard
    case "application.conf" => MergeStrategy.concat
    case "reference.conf" => MergeStrategy.concat
    case _ => MergeStrategy.first
  }
)
