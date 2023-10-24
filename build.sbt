ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"
ThisBuild / scalacOptions += "-Ytasty-reader"
lazy val root = (project in file("."))
  .settings(
    name := "Spark-project"
  )
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.5.0", // Use the version compatible with your system
  "org.apache.spark" %% "spark-sql" % "3.5.0", // For Spark SQL functionality
  "org.apache.spark" %% "spark-graphx" % "3.5.0"
)
