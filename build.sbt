name := "hookla"

version := "0.1"

scalaVersion := "2.13.4"

//////////////////////////////////////////////////////////////////
////////////////////////   DEPENDENCIES   ////////////////////////
//////////////////////////////////////////////////////////////////
val scalalikejdbcVersion = "3.5.0"
val h2DatabaseVersion = "1.4.200"
val circeConfVersion = "0.8.0"
val twitterVersion = "20.3.0"
val circeVersion = "0.13.0"
val akkaVersion = "2.6.10"
val log4sVersion = "1.8.2"
val finchVersion = "0.32.1"
val guiceScalaVersion = "4.2.10"
val guiceVersion = "4.2.3"
val flywayVersion = "5.1.0"

libraryDependencies ++= Seq(
  "com.google.inject.extensions" % "guice-assistedinject" % guiceVersion,
  "com.github.finagle" %% "finchx-core" % finchVersion,
  "com.github.finagle" %% "finchx-circe" % finchVersion,
  "com.twitter" %% "twitter-server" % twitterVersion,
  "net.codingwell" %% "scala-guice" % guiceScalaVersion,
  "org.log4s" %% "log4s" % log4sVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",

  // flyway migration shit
  "org.flywaydb" % "flyway-core" % flywayVersion,
  "org.postgresql" % "postgresql" % "9.3-1102-jdbc41",

  // circe shit
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "io.circe" %% "circe-config" % circeConfVersion,

  "io.getquill" %% "quill-async-postgres" % "3.5.2",
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion
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
