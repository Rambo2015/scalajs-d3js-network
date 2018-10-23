organization := "io.jorand"

name := "scalajs-components"

version := "0.1"

scalaVersion := "2.12.7"


enablePlugins(ScalaJSPlugin)

// This is an application with a main method
scalaJSUseMainModuleInitializer := true

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.9.5",
  "com.lihaoyi" %%% "scalatags" % "0.6.7",
  "org.querki" %%% "jquery-facade" % "1.2",
  "io.circe" %%% "circe-core" % "0.9.3",
  "io.circe" %%% "circe-generic" % "0.9.3",
  "io.circe" %%% "circe-parser" % "0.9.3",
  "io.circe" %%% "circe-generic-extras" % "0.9.3",
  "org.singlespaced" %%% "scalajs-d3" % "0.3.4",
  "org.scalatest" %%% "scalatest" % "3.0.1" % "test"
)

jsDependencies ++= Seq(
  "org.webjars" % "jquery" % "2.2.1" / "jquery.js" minified "jquery.min.js"
)

jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv()

skip in packageJSDependencies := false