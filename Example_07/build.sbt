import sbt._
import sbt.Keys._


lazy val allResolvers = Seq(
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
)

val akkaVersion = "2.5.3"

lazy val AllLibraryDependencies =
  Seq(
    "com.typesafe.akka" %% "akka-actor"         % akkaVersion,
    "com.typesafe.akka" %% "akka-remote"        % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster"       % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
    "com.typesafe.akka" %% "akka-contrib"       % akkaVersion,
    "com.twitter" %% "chill-akka" % "0.8.4"
  )


lazy val commonSettings = Seq(
  version := "1.0",
  scalaVersion := "2.11.8",
  resolvers := allResolvers,
  libraryDependencies := AllLibraryDependencies
)


lazy val root =(project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "Base"
  )
  .aggregate(common, frontend, backend)
  .dependsOn(common, frontend, backend)

lazy val common = (project in file("common")).
  settings(commonSettings: _*).
  settings(
    name := "common"
  )

lazy val frontend = (project in file("frontend")).
  settings(commonSettings: _*).
  settings(
    name := "frontend"
  )
  .aggregate(common)
  .dependsOn(common)

lazy val backend = (project in file("backend")).
  settings(commonSettings: _*).
  settings(
    name := "backend"
  )
  .aggregate(common)
  .dependsOn(common)