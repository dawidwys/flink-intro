name := "flink-queryable-job"

libraryDependencies ++= Seq(
  "org.rogach" %% "scallop" % "2.0.6",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test"
)

mainClass in assembly := Some("com.craft.computing.QueryableJob")

// make run command include the provided dependencies
run in Compile <<=
  Defaults.runTask(fullClasspath in Compile, mainClass in(Compile, run), runner in(Compile, run))

// exclude Scala library from assembly
assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

