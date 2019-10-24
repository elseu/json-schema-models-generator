name := "json-schema-models-generator"

organization := "nl.sdu.modelsgenerator"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-json" % "2.7.3"
)

// useful to enable while debugging
// libraryDependencies ++= Seq(
//   "com.lihaoyi" %% "pprint" % "0.5.3"
// )

// Code formatting
scalafmtOnCompile in ThisBuild := true
