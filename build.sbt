import sbt.Keys.libraryDependencies

import scala.language.postfixOps
Global / excludeLintKeys +=   test / fork
Global / excludeLintKeys += run / mainClass

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.18"
javacOptions ++= Seq("-source", "1.8", "-target", "1.8")
lazy val root = (project in file("."))
  .settings(
    name := "Spark-project",
  )
compileOrder := CompileOrder.JavaThenScala
test / fork := true
run / fork := true
run / javaOptions ++= Seq(
  "-Xms8G",
  "-Xmx100G",
  "-XX:+UseG1GC"
)
Compile / mainClass := Some("com.lsc.Main")
run / mainClass := Some("com.lsc.Main")
assembly / mainClass := Some("com.lsc.Main")
val jarName = "Cloud-jar.jar"
assembly/assemblyJarName := jarName
ThisBuild / assemblyMergeStrategy := {
  case PathList("META-INF", _*) => MergeStrategy.discard
  case "application.conf" => MergeStrategy.concat
  case _ => MergeStrategy.first
}
val json4sVersion = "3.6.1"
libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-json" % "2.9.4",
  "org.apache.spark" %% "spark-core" % "3.4.1",
  "org.apache.spark" %% "spark-sql" % "3.4.1",
  "org.apache.spark" %% "spark-graphx" % "3.4.1",
  "com.typesafe" % "config" % "1.4.2",
  "org.json4s" %% "json4s-native" % json4sVersion,
  "org.scalatest" %% "scalatest" % "3.2.15" % "test",
  "org.scalatestplus" %% "scalatestplus-mockito" % "1.0.0-M2" % Test,
  "org.mockito" % "mockito-core" % "3.11.2" % Test,
  "org.scalamock" %% "scalamock" % "5.1.0" % Test,
)

