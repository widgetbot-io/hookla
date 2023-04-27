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

lazy val catsVersion    = "2.9.0"
lazy val zioVersion     = "2.0.6"
lazy val calibanVersion = "2.0.2"
lazy val doobieVersion  = "1.0.0-RC1"

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "net.katsstuff" %% "ackcord-core"    % "0.17.1",
  // Enumeratum
  "com.beachape" %% "enumeratum"       % "1.5.15",
  "com.beachape" %% "enumeratum-circe" % "1.5.23",
  // ZIO Rewrite
  "org.typelevel"         %% "cats-core"                 % catsVersion,
  "dev.zio"               %% "zio"                       % zioVersion,
  "dev.zio"               %% "zio-streams"               % zioVersion,
  "dev.zio"               %% "zio-json"                  % "0.4.2",
  "dev.zio"               %% "zio-config"                % "3.0.7",
  "dev.zio"               %% "zio-interop-cats"          % "23.0.0.0",
  "io.d11"                %% "zhttp"                     % "2.0.0-RC10",
  "com.github.ghostdogpr" %% "caliban"                   % calibanVersion,
  "com.github.ghostdogpr" %% "caliban-zio-http"          % calibanVersion,
  "io.getquill"           %% "quill-jasync-zio-postgres" % "4.6.0"
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
