import sbt.Keys._
import sbt.Resolver

packagedArtifacts in file(".") := Map.empty // disable publishing of root/default project

scalaVersion in ThisBuild := "2.11.8"

//maintainer := "Michael Merz <dermicha@ubirch.com>"

name := "ubirch-storage-service"

homepage := Some(url("http://ubirch.com"))

resolvers in ThisBuild ++= Seq(
  Opts.resolver.sonatypeSnapshots // ubirch
  //  Opts.resolver.sonatypeReleases // ubirch
)

lazy val testConfiguration = "-Dconfig.resource=" + Option(System.getProperty("test.config")).getOrElse("application.dev.conf")

lazy val commonSettings = Seq(
  organization := "com.ubirch.backend.storage",
  version := "0.1.0-SNAPSHOT",
  test in assembly := {},
  parallelExecution in ThisBuild := false,
  javaOptions in Test += testConfiguration,
  fork in Test := true,
  updateOptions := updateOptions.value.withLatestSnapshots(false),
  // in ThisBuild is important to run tests of each subproject sequential instead parallelizing them
  testOptions in ThisBuild ++= Seq(
    Tests.Argument(TestFrameworks.ScalaTest, "-u", "target/test-reports"),
    Tests.Argument(TestFrameworks.ScalaTest, "-o")
  )
)

lazy val ubirchShare = (project in file("ubirch-share"))
  .settings(commonSettings: _*)
  .dependsOn(model)
  .settings(libraryDependencies ++= commonDependencies ++ mqttDependencies ++ json4s ++ testDependencies
    :+ typesafeConfig :+ ubirchUtilUUID :+ ubirchUtilJson :+ hasher % "test" :+ beeClient % "test" :+ ubirchUtilDate % "test")
  .settings(resolvers ++= Seq(
    resolverHasher,
    resolverBeeClient
  ))

lazy val config = (project in file("config"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= commonDependencies)

lazy val model = (project in file("model"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= commonDependencies ++ joda ++ testDependencies :+ ubirchUtilDate)

lazy val share = (project in file("share"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= commonDependencies ++ testDependencies :+ ubirchUtilDate)
  .dependsOn(ubirchShare)
  .dependsOn(config)
  .dependsOn(model)

lazy val core = (project in file("core"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= commonDependencies ++ akkaDependencies ++ apacheHttpDependencies ++ testDependencies
    :+ hasher % "test" :+ ubirchUtilDate)
  .settings(resolvers ++= Seq(
    resolverHasher,
    resolverBeeClient
  ))
  .dependsOn(share)
  .dependsOn(model)
  .dependsOn(ubirchShare)
  .dependsOn(testUtil)

lazy val server = (project in file("server"))
  .enablePlugins(DockerPlugin)
  .settings(commonSettings: _*)
  .settings(
    dockerfile in docker := {
      // The assembly task generates a fat JAR file
      val artifact: File = assembly.value
      val artifactTargetPath = s"/app/${artifact.name}"

      new Dockerfile {
        from("ubirch/java")
        add(artifact, artifactTargetPath)
        entryPoint("java", "-jar", artifactTargetPath, "-D")
      }
    }
  )
  .settings(mergeStrategy: _*)
  .settings(libraryDependencies ++= commonDependencies ++ akkaHttpDependencies ++ testDependencies :+ ubirchUtilJsonAutoConvert)
  .dependsOn(share)
  .dependsOn(core)
  .dependsOn(model)

lazy val client = (project in file("client"))
  .settings(commonSettings: _*)
  .settings(scmInfo := Some(ScmInfo(url("https://github.com/ubirch/ubirch-storage-service"), "git@github.com:ubirch/ubirch-storage-service.git")))
  .settings(libraryDependencies := commonDependencies ++ joda ++ akkaHttpDependencies ++ testDependencies)
  .dependsOn(share)
  .dependsOn(core)
  .dependsOn(model)

lazy val testUtil = (project in file("test-util"))
  .settings(commonSettings: _*)
  .settings(
    name := "test-util",
    libraryDependencies ++= Seq(typesafeLogging) :+ beeClient,
    resolvers ++= Seq(
      resolverBeeClient
    )
  )
  .dependsOn(share)

val scalaV = "2.11.8"
val akkaV = "2.4.8"
val scalaTestV = "2.2.6"
val json4sV = "3.4.0"
val configV = "1.3.0"
val elasticV = "2.3.5"

lazy val commonDependencies = Seq(
  //scala
  "org.scala-lang.modules" %% "scala-xml" % "1.0.5",
  "org.scala-lang" % "scala-compiler" % scalaV,
  "org.scala-lang" % "scala-library" % scalaV,
  "org.scala-lang" % "scala-reflect" % scalaV,

  // logging
  typesafeLogging,
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "ch.qos.logback" % "logback-core" % "1.1.3",
  "org.slf4j" % "slf4j-api" % "1.7.12",
  "com.internetitem" % "logback-elasticsearch-appender" % "1.3",

  //Apache Commons
  "commons-io" % "commons-io" % "2.4",
  "commons-logging" % "commons-logging" % "1.2"

)

lazy val testDependencies = Seq(
  "org.scalatest" %% "scalatest" % scalaTestV % "test"
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

lazy val beeClient = "uk.co.bigbeeconsultants" %% "bee-client" % "0.29.1"

lazy val hasher = "com.roundeights" %% "hasher" % "1.2.0"

lazy val typesafeLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0"

lazy val apacheHttpDependencies = Seq(
  "org.apache.httpcomponents" % "httpclient" % "4.5.2"
)

lazy val json4s = Seq(
  json4sCore,
  json4sJackson,
  json4sExt,
  json4sNative
)
lazy val json4sJackson = "org.json4s" %% "json4s-jackson" % json4sV
lazy val json4sCore = "org.json4s" %% "json4s-core" % json4sV
lazy val json4sExt = "org.json4s" %% "json4s-ext" % json4sV
lazy val json4sNative = "org.json4s" %% "json4s-native" % json4sV

lazy val joda = Seq(jodaTime, jodaConvert)
lazy val jodaTime = "joda-time" % "joda-time" % "2.9.4"
lazy val jodaConvert = "org.joda" % "joda-convert" % "1.8"

lazy val typesafeConfig = "com.typesafe" % "config" % configV

lazy val ubirchUtilsDependencies = Seq(
  "com.ubirch.util" %% "crypto" % "0.2-SNAPSHOT"
)

lazy val ubirchUtilDate = "com.ubirch.util" %% "date" % "0.1-SNAPSHOT"
lazy val ubirchUtilJson = "com.ubirch.util" %% "json" % "0.1-SNAPSHOT"
lazy val ubirchUtilJsonAutoConvert = "com.ubirch.util" %% "json-auto-convert" % "0.1-SNAPSHOT"
lazy val ubirchUtilUUID = "com.ubirch.util" %% "uuid" % "0.1-SNAPSHOT"

lazy val resolverHasher = "RoundEights" at "http://maven.spikemark.net/roundeights"
lazy val resolverBeeClient = Resolver.bintrayRepo("rick-beton", "maven")

lazy val mergeStrategy = Seq(
  assemblyMergeStrategy in assembly := {
    case PathList("org", "joda", "time", xs@_*) => MergeStrategy.first
    case m if m.toLowerCase.endsWith("manifest.mf") => MergeStrategy.discard
    case m if m.toLowerCase.matches("meta-inf.*\\.sf$") => MergeStrategy.discard
    case m if m.toLowerCase.endsWith("application.conf") => MergeStrategy.concat
    case m if m.toLowerCase.endsWith("application.dev.conf") => MergeStrategy.first
    case m if m.toLowerCase.endsWith("application.base.conf") => MergeStrategy.first
    case "reference.conf" => MergeStrategy.concat
    case _ => MergeStrategy.first
  }
)
