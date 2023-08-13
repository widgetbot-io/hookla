import sbt._
import Keys.{libraryDependencies, _}
import com.typesafe.sbt.packager.docker.DockerPermissionStrategy

name := "hookla"

version := "0.1"

scalaVersion := "2.13.8"

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
  .settings(
    dockerEntrypoint := Seq(
      "java",
      "-Xms1024m",
      "-Xmx1024m",
      "-XX:+AlwaysPreTouch",
      "-Dfile.encoding=UTF-8",
      "-jar",
      s"/opt/docker/lib/${(artifactPath in packageJavaLauncherJar).value.getName}"
    )
  )
  .settings(dockerCmd := Seq.empty)

//////////////////////////////////////////////////////////////////
////////////////////////   DEPENDENCIES   ////////////////////////
//////////////////////////////////////////////////////////////////
val circeConfVersion = "0.8.0"
val circeVersion     = "0.13.0"
val log4sVersion     = "1.8.2"

lazy val zioVersion     = "2.0.13"
lazy val calibanVersion = "2.2.1"
lazy val doobieVersion  = "1.0.0-RC1"

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.4.7",
  "net.katsstuff" %% "ackcord-core"    % "0.17.1",
  // Enumeratum
  "com.beachape" %% "enumeratum"       % "1.7.2",
  "com.beachape" %% "enumeratum-circe" % "1.7.2",
  // ZIO Rewrite
  "com.github.ghostdogpr"       %% "caliban"                   % calibanVersion,
  "com.github.ghostdogpr"       %% "caliban-zio-http"          % calibanVersion,
  "dev.zio"                     %% "zio"                       % zioVersion,
  "dev.zio"                     %% "zio-streams"               % zioVersion,
  "dev.zio"                     %% "zio-json"                  % "0.5.0",
  "dev.zio"                     %% "zio-config"                % "3.0.7",
  "dev.zio"                     %% "zio-interop-cats"          % "23.0.03",
  "dev.zio"                     %% "zio-http"                  % "3.0.0-RC1",
  "dev.zio"                     %% "zio-logging-slf4j2"        % "2.1.14",
  "io.getquill"                 %% "quill-jasync-zio-postgres" % "4.6.0",
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe"          % "1.2.11", // Circe
  // Flyway and Postgres Driver
  "org.flywaydb"   % "flyway-core" % "9.16.0",
  "org.postgresql" % "postgresql"  % "42.5.4",
  // Circe
  "io.circe" %% "circe-config" % "0.10.0"
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
