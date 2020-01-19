name := "lucsslwordcloud"

version := "0.1"

scalaVersion := "2.13.1"

scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Ywarn-dead-code")

libraryDependencies ++= Seq(
  "com.softwaremill.sttp.client" %% "core" % "2.0.0-RC6",
  "org.json4s" %% "json4s-native" % "3.7.0-M2",
  "org.mongodb.scala" %% "mongo-scala-driver" % "2.8.0"
)

enablePlugins(JavaAppPackaging)
