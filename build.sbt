name := "tubi-hangout-server"
scalaVersion := "2.13.1"

val Versions = new {
  val AkkaHttp = "10.1.11"
  val Akka     = "2.6.4"
  val Spray    = "1.3.5"
}

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor"           % Versions.Akka,
  "com.typesafe.akka" %% "akka-http"            % Versions.AkkaHttp,
  "com.typesafe.akka" %% "akka-stream"          % Versions.Akka,
  "com.typesafe.akka" %% "akka-http-spray-json" % Versions.AkkaHttp,
  "io.spray"          %% "spray-json"           % Versions.Spray
)
