name := "jira-timeline"

version := "1.0"

scalaVersion := "2.12.0"

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.1",
  "com.github.andr83" %% "scalaconfig" % "0.3",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.6",
  "org.scalaj" %% "scalaj-http" % "2.3.0"
)