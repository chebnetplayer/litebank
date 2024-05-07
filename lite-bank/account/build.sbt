import Dependencies._
ThisBuild / scalaVersion := "2.13.8"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "ru.misis"
ThisBuild / organizationName := "misis"

val akkaVersion = "2.6.18"
val akkaHttpVersion = "10.2.7"
val akkaHttpJsonVersion = "1.39.2"
val circeVersion = "0.14.1"
val slickVersion = "3.4.1"
val postgresVersion = "42.5.0"

lazy val root = (project in file("."))
  .settings(
    name := "account",
    Compile / run / mainClass := Some("misis.AccountApp"),
    libraryDependencies ++= Seq(
      //Json
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      //Http / Rest api
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream-kafka" % "3.0.0",
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "de.heikoseeberger" %% "akka-http-circe" % akkaHttpJsonVersion,
      // Db
      "com.typesafe.slick" %% "slick" % slickVersion,
      "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
      "org.postgresql" % "postgresql" % postgresVersion,
      //Logging
      "ch.qos.logback" % "logback-classic" % "1.2.3"
    )
  )

enablePlugins(JavaAppPackaging)