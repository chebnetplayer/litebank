ThisBuild / scalaVersion := "2.13.8"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "misis"
ThisBuild / organizationName := "misis"

val akkaVersion = "2.6.18"
val akkaHttpVersion = "10.2.7"
val circeVersion = "0.14.1"
val AkkaHttpJsonVersion = "1.39.2"
lazy val slickVersion = "3.3.3"
lazy val postgresVersion = "42.3.1"

lazy val common = ProjectRef(base = file("../common"), id = "common")
lazy val account = ProjectRef(base = file("../account"), id = "account")

lazy val report = (project in file("."))
    .dependsOn(common)
    .dependsOn(account)
    .settings(
        name := "report",
        libraryDependencies ++= Seq(
            "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
            "de.heikoseeberger" %% "akka-http-circe" % AkkaHttpJsonVersion,
            "com.sksamuel.elastic4s" %% "elastic4s-client-esjava" % "7.17.2",

            "ch.qos.logback"     % "logback-classic"       % "1.2.3"
        )
    )


enablePlugins(JavaAppPackaging)