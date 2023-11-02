Global / excludeLintKeys +=   test / fork
Global / excludeLintKeys += run / mainClass

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"
ThisBuild / scalacOptions += "-Ytasty-reader"
javacOptions ++= Seq("-source", "1.8", "-target", "1.8")
lazy val root = (project in file("."))
  .settings(
    name := "Spark-project"
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
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.5.0",
  "org.apache.spark" %% "spark-sql" % "3.5.0",
  "org.apache.spark" %% "spark-graphx" % "3.5.0",
  "com.typesafe" % "config" % "1.4.2",
)

