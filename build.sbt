name := "dataTest"

version := "0.1"

scalaVersion := "2.11.4"

libraryDependencies ++= {
  val akkaV = "2.3.6"
  val sprayV = "1.3.2"
  Seq(
    "io.spray"            %%  "spray-can"     % sprayV,
    "io.spray"            %%  "spray-routing" % sprayV,
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe.akka" %% "akka-remote" % akkaV,
    "com.datastax.cassandra" % "cassandra-driver-core" % "2.1.4",
    "org.apache.cassandra" % "cassandra-all" % "2.1.2",
    "io.spray" %%  "spray-json" % sprayV
    //"io.spray"            %%  "spray-testkit" % sprayV  % "test"
    //com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test"
    //"org.specs2"          %%  "specs2-core"   % "2.3.7" % "test"
  )
}