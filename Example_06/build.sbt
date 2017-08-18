import sbt._
import sbt.Keys._

lazy val AllLibraryDependencies =
  Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.5.3",
    "com.typesafe.akka" %% "akka-remote" % "2.5.3",
    "com.twitter" %% "chill-akka" % "0.8.4"
  )

lazy val commonSettings = Seq(
  name := "AkkaRemoting",
  version := "1.0",
  scalaVersion := "2.11.11",
  libraryDependencies := AllLibraryDependencies
)

lazy val remote = (project in file("remote"))
  .settings(commonSettings: _*)
  .settings(
    // other settings
  )

lazy val local = (project in file("local"))
  .settings(commonSettings: _*)
  .settings(
    // other settings
  )
