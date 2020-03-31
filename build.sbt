enablePlugins(ScalaJSPlugin)

name := "descent-animator"

version := "0.1-SNAPSHOT"
scalaVersion := "2.12.8"
//scalaJSUseMainModuleInitializer := true

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.9.8"
)
