lazy val IntegrationTest = config("it") extend Test

val circeVersion = "0.9.3"
val effVersion = "5.3.0"
val akkaVersion = "10.1.3"

lazy val transfers = project
  .in(file("."))
  .configs(IntegrationTest)
  .settings(
    name := "forex",
    version := "0.1.0",
    scalaVersion := "2.12.6",
    scalacOptions ++= Seq(
      "-deprecation",
      "-encoding",
      "UTF-8",
      "-feature",
      "-language:existentials",
      "-language:higherKinds",
      "-Ypartial-unification",
      "-language:experimental.macros",
      "-language:implicitConversions"
    ),
    resolvers +=
      "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    libraryDependencies ++= Seq(
      "com.github.pureconfig"      %% "pureconfig"           % "0.9.1",
      "com.softwaremill.quicklens" %% "quicklens"            % "1.4.11",
      "com.typesafe.akka"          %% "akka-actor"           % "2.5.14",
      "com.typesafe.akka"          %% "akka-http"            % akkaVersion,
      "de.heikoseeberger"          %% "akka-http-circe"      % "1.21.0",
      "io.circe"                   %% "circe-core"           % circeVersion,
      "io.circe"                   %% "circe-generic"        % circeVersion,
      "io.circe"                   %% "circe-generic-extras" % circeVersion,
      "io.circe"                   %% "circe-java8"          % circeVersion,
      "io.circe"                   %% "circe-jawn"           % circeVersion,
      "org.atnos"                  %% "eff"                  % effVersion,
      "org.atnos"                  %% "eff-monix"            % effVersion,
      "org.typelevel"              %% "cats-core"            % "1.1.0",
      "org.zalando"                %% "grafter"              % "2.6.1",
      "ch.qos.logback"             % "logback-classic"       % "1.2.3",
      "com.typesafe.scala-logging" %% "scala-logging"        % "3.7.2",
      "com.beachape"               %% "enumeratum"           % "1.5.13",
      "com.typesafe.akka"          %% "akka-http-testkit"    % akkaVersion % "test, it",
      "org.scalatest"              %% "scalatest"            % "3.0.5" % "test, it",
      "org.scalacheck"             %% "scalacheck"           % "1.14.0" % "test, it",
      "org.scalamock"              %% "scalamock"            % "4.1.0" % "test",
      compilerPlugin("org.spire-math"  %% "kind-projector" % "0.9.4"),
      compilerPlugin("org.scalamacros" %% "paradise"       % "2.1.1" cross CrossVersion.full)
    ),
    Defaults.itSettings,
    parallelExecution in IntegrationTest := false,
    fork in IntegrationTest := true,
    javaOptions in IntegrationTest += "-Dakka.loglevel=ERROR"
  )
  .enablePlugins(JavaAppPackaging)
