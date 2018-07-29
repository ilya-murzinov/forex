lazy val IntegrationTest = config("it") extend Test

lazy val transfers = project
  .in(file("."))
  .configs(IntegrationTest)
  .settings(
    name := "forex",
    version := "0.1.0",
    scalaVersion := "2.12.6",
    cancelable in Global := true,
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
      "com.typesafe.akka"          %% "akka-actor"           % "2.4.19",
      "com.typesafe.akka"          %% "akka-http"            % "10.0.10",
      "de.heikoseeberger"          %% "akka-http-circe"      % "1.18.1",
      "io.circe"                   %% "circe-core"           % "0.8.0",
      "io.circe"                   %% "circe-generic"        % "0.8.0",
      "io.circe"                   %% "circe-generic-extras" % "0.8.0",
      "io.circe"                   %% "circe-java8"          % "0.8.0",
      "io.circe"                   %% "circe-jawn"           % "0.8.0",
      "org.atnos"                  %% "eff"                  % "4.5.0",
      "org.atnos"                  %% "eff-monix"            % "4.5.0",
      "org.typelevel"              %% "cats-core"            % "0.9.0",
      "org.zalando"                %% "grafter"              % "2.3.0",
      "ch.qos.logback"             % "logback-classic"       % "1.2.3",
      "com.typesafe.scala-logging" %% "scala-logging"        % "3.7.2",
      "com.beachape"               %% "enumeratum"           % "1.5.13",
      "com.typesafe.akka"          %% "akka-http-testkit"    % "10.1.3" % "test, it",
      "org.scalatest"              %% "scalatest"            % "3.0.5" % "test, it",
      "org.scalacheck"             %% "scalacheck"           % "1.14.0" % "test, it",
      "org.scalamock"              %% "scalamock"            % "4.1.0" % "test",
      compilerPlugin("org.spire-math"  %% "kind-projector" % "0.9.4"),
      compilerPlugin("org.scalamacros" %% "paradise"       % "2.1.1" cross CrossVersion.full)
    ),
    Defaults.itSettings,
    parallelExecution in IntegrationTest := false,
    fork in IntegrationTest := true,
    javaOptions in IntegrationTest += "-Dlogback.configurationFile=disable.logs.xml",
    javaOptions in IntegrationTest += "-Dakka.loglevel=ERROR"
  )
  .enablePlugins(JavaAppPackaging)
