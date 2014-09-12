name := """student-workflow"""

version := "1.0"

scalaVersion := "2.11.1"

// Change this to another test framework if you prefer
libraryDependencies += "org.scalatest" %% "scalatest" % "2.1.6" % "test"

// Uncomment to use Akka
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.6",
  "com.typesafe.akka" %% "akka-persistence-experimental" % "2.3.6",
  "mysql" % "mysql-connector-java" % "5.1.26",
  "com.github.dnvriend" %% "akka-persistence-jdbc" % "1.0.5"
 )

