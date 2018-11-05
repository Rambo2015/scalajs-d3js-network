import org.scalajs.sbtplugin.ScalaJSPlugin.AutoImport.{jsDependencies, jsEnv}
import sbt.Keys.scalaVersion


enablePlugins(ScalaJSPlugin)
enablePlugins(BintrayPlugin)

releaseEarlyEnableSyncToMaven := true

lazy val root = (project in file("."))
  .settings(
    organization := "io.jorand",
    name := "scalajs-d3js-network",
    version := "0.1.1",
    scalaVersion := "2.12.7",
    crossScalaVersions := Seq("2.12.7"),
    parallelExecution := false,
    pgpPublicRing := file("./travis/local.pubring.asc"),
    pgpSecretRing := file("./travis/local.secring.asc"),
    // This is an application with a main method
    scalaJSUseMainModuleInitializer := true,
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
    ),

    jsDependencies ++= Seq(
      "org.webjars" % "jquery" % "2.2.1" / "jquery.js" minified "jquery.min.js"
    ),

    jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv(),

    skip in packageJSDependencies := false,

    bintrayRepository := "scala",
    bintrayOrganization := None,
    publishMavenStyle := true,
    licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
    pomExtra := <scm>
      <url>git@github.com:iPomme/scalajs-d3js-network.git</url>
      <connection>scm:git:git@github.com:iPomme/scalajs-d3js-network.git</connection>
    </scm>
      <developers>
        <developer>
          <id>iPomme</id>
          <name>Nicolas Jorand</name>
          <url>https://nicolas.jorand.io</url>
        </developer>
      </developers>,
    homepage := Some(url("https://www.jorand.io"))
  )