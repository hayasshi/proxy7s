lazy val akkaHttpVersion = "10.2.10"
lazy val akkaVersion     = "2.6.21"

val commonSettings = Seq(
  organization := "com.github.hayasshi",
  scalaVersion := "2.13.12"
)

lazy val root = (project in file("."))
  .settings(commonSettings)
  .aggregate(akkaHttp)

lazy val akkaHttp = (project in file("akka-http"))
  .settings(commonSettings)
  .settings(
    name := "proxy7s-akka-http",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"         % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream"       % akkaVersion,
      "ch.qos.logback"     % "logback-classic"   % "1.4.11",
      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
      "org.scalatest"     %% "scalatest"         % "3.2.17"        % Test
    )
  )
