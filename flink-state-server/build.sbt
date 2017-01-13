name := "flink-state-server"
parallelExecution in ThisBuild := false

lazy val versions = new {
  val scalaTest = "3.0.0"
  val scalaCheck = "1.13.4"
  val akkaHttp = "2.4.11"
  val scallop = "2.0.6"
}

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http-core" % versions.akkaHttp,
  "com.typesafe.akka" %% "akka-http-experimental" % versions.akkaHttp,
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % versions.akkaHttp,
  "com.typesafe.akka" %% "akka-slf4j" % versions.akkaHttp,
  "org.rogach" %% "scallop" % versions.scallop,

  "org.scalacheck" %% "scalacheck" % versions.scalaCheck % "test",
  "org.scalatest" %% "scalatest" % versions.scalaTest % "test")

assemblyMergeStrategy in assembly := {
  case "BUILD" => MergeStrategy.discard
  case "META-INF/io.netty.versions.properties" => MergeStrategy.last
  case other => MergeStrategy.defaultMergeStrategy(other)
}

run in Compile <<=
  Defaults.runTask(fullClasspath in Compile, mainClass in(Compile, run), runner in(Compile, run))

resourceDirectory in Compile <<= javaSource in Compile