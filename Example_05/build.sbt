name := "Example_05"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.16",
  "com.typesafe.akka" %% "akka-persistence" % "2.4.16",
  "org.iq80.leveldb"            % "leveldb"          % "0.7",
  "org.fusesource.leveldbjni"   % "leveldbjni-all"   % "1.8",
  "com.twitter"              %% "chill-akka"                  % "0.8.0",
  "com.typesafe.akka" %% "akka-persistence-query" % "2.5.3"
  )
    