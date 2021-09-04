lazy val akkaHttpVersion = "10.2.6"
lazy val akkaVersion    = "2.6.16"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.github.hayasshi",
      scalaVersion    := "2.13.6"
    )),
    name := "proxy7s",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"                % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream"              % akkaVersion,
      "ch.qos.logback"    % "logback-classic"           % "1.2.3",

      "com.typesafe.akka" %% "akka-http-testkit"        % akkaHttpVersion % Test,
      "org.scalatest"     %% "scalatest"                % "3.1.4"         % Test
    )
  )
