name := """projector-remote"""
organization := "com.fishuyo"

version := "0.1-SNAPSHOT"

val scalaV = "2.11.11"

// lazy val root = (project in file(".")).enablePlugins(PlayScala)

// Adds additional packages into Twirl
// TwirlKeys.templateImports += "com.fishuyo.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.fishuyo.binders._"


lazy val server = (project in file("server")).settings(
  scalaVersion := scalaV,
  scalaJSProjects := Seq(client),
  pipelineStages in Assets := Seq(scalaJSPipeline),
  pipelineStages := Seq(digest, gzip),
  // triggers scalaJSPipeline when using compile or continuous compilation
  compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
  resolvers += Resolver.sonatypeRepo("snapshots"),
  //updateOptions := updateOptions.value.withLatestSnapshots(false),
  libraryDependencies ++= Seq(
    "com.vmunier" %% "scalajs-scripts" % "1.1.1",
    guice,
    specs2 % Test,
    "org.webjars" %% "webjars-play" % "2.6.0",
    "org.webjars" % "jquery" % "3.2.1",
    "org.webjars" % "materializecss" % "0.99.0"
  )
  // Compile the project before generating Eclipse files, so that generated .scala or .class files for views and routes are present
  // EclipseKeys.preTasks := Seq(compile in Compile)
).enablePlugins(PlayScala).
  dependsOn(sharedJvm)


lazy val client = (project in file("client")).settings(
  scalaVersion := scalaV,
  scalaJSUseMainModuleInitializer := true,
  resolvers += Resolver.jcenterRepo,
  resolvers += Resolver.bintrayRepo("denigma", "denigma-releases"),

  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.9.1",
    "com.thoughtworks.binding" %%% "dom" % "latest.release",
    "org.querki" %%% "jquery-facade" % "1.0",
    "be.doeraene" %%% "scalajs-jquery" % "0.9.2",
    "com.definitelyscala" %%% "scala-js-materializecss" % "1.0.0",
    compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
  ),
  jsDependencies ++= Seq(
    "org.webjars" % "jquery" % "3.2.1" / "jquery.js" minified "jquery.min.js",
    "org.webjars" % "materializecss" % "0.99.0" / "materialize.js" minified "materialize.min.js" dependsOn "jquery.js"
  )
).enablePlugins(ScalaJSPlugin, ScalaJSWeb).
  dependsOn(sharedJs)


lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared")).settings(
    scalaVersion := scalaV,
    libraryDependencies ++= Seq(
      // "com.typesafe.play" %% "play-json" % "2.6.1",
      "org.julienrf" %%% "play-json-derived-codecs" % "4.0.0"
    )
  ).jsConfigure(_ enablePlugins ScalaJSWeb)

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

// loads the server project at sbt startup
onLoad in Global := (Command.process("project server", _: State)) compose (onLoad in Global).value



