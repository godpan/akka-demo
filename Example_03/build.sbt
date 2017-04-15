name := "Example_03"

version := "1.0"

scalaVersion := "2.11.8"

val akkaVersion   = "2.4.16"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "org.scalatest"     %% "scalatest"     % "3.0.0"
)

    