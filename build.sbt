import sbt._
import Keys.{libraryDependencies, _}
import com.typesafe.sbt.packager.docker.DockerPermissionStrategy

name := "hookla"

version := "0.1"

scalaVersion := "2.13.4"

lazy val hookla = (project in file("."))
  .enablePlugins(DockerPlugin, LauncherJarPlugin)
  .settings(dockerExposedPorts := Seq(8443))
  .settings(dockerRepository := Some("ghcr.io"))
  .settings(dockerUsername := Some("widgetbot-io"))
  .settings(packageName in Docker := "hookla")
  .settings(dockerUpdateLatest := true)
  .settings(dockerBaseImage := "gcr.io/distroless/java:11")
  .settings(daemonUserUid in Docker := None)
  .settings(daemonUser in Docker := "root")
  .settings(dockerPermissionStrategy := DockerPermissionStrategy.None)
  .settings(dockerEntrypoint := Seq("java", "-Xms1024m", "-Xmx1024m", "-XX:+AlwaysPreTouch", "-Dfile.encoding=UTF-8", "-jar",s"/opt/docker/lib/${(artifactPath in packageJavaLauncherJar).value.getName}"))
  .settings(dockerCmd := Seq.empty)

//////////////////////////////////////////////////////////////////
////////////////////////   DEPENDENCIES   ////////////////////////
//////////////////////////////////////////////////////////////////
val scalalikejdbcVersion = "3.5.0"
val h2DatabaseVersion = "1.4.200"
val circeConfVersion = "0.8.0"
val twitterVersion = "20.3.0"
val circeVersion = "0.13.0"
val akkaVersion = "2.6.6"
val log4sVersion = "1.8.2"
val finchVersion = "0.32.1"
val guiceScalaVersion = "4.2.10"
val guiceVersion = "4.2.3"
val flywayVersion = "5.1.0"

libraryDependencies ++= Seq(
  "com.github.finagle" %% "finchx-core" % finchVersion,
  "com.github.finagle" %% "finchx-circe" % finchVersion,
  "com.twitter" %% "twitter-server" % twitterVersion,
  "org.log4s" %% "log4s" % log4sVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "net.katsstuff" %% "ackcord-core" % "0.17.1",

  // Database
  "org.flywaydb" % "flyway-core" % flywayVersion,
  "org.postgresql" % "postgresql" % "9.3-1102-jdbc41",
  "io.getquill" %% "quill-async-postgres" % "3.5.2",

  // Circe
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "io.circe" %% "circe-config" % circeConfVersion,

  // MacWire stuff
  "com.softwaremill.macwire" %% "macros" % "2.3.7" % "provided",
  "com.softwaremill.macwire" %% "macrosakka" % "2.3.7" % "provided",
  "com.softwaremill.macwire" %% "util" % "2.3.7",
  "com.softwaremill.macwire" %% "proxy" % "2.3.7",
)

scalacOptions ++= Seq(
  "-language:implicitConversions",
  "-deprecation",
  "-encoding",
  "utf-8",
  "-explaintypes",
  "-feature",
  "-unchecked",
  "-Xcheckinit",
  "-Werror",
  "-Wdead-code",
  "-Yrangepos",
  "-Ymacro-annotations",
  "-Ybackend-parallelism",
  "6"
)
