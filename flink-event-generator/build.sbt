name := "flink-event-generator"

libraryDependencies ++= Seq(
  "org.rogach" %% "scallop" % "2.0.6"
)

mainClass in assembly := Some("com.craft.computing.ClickGenerationJob")

// make run command include the provided dependencies
run in Compile <<=
  Defaults.runTask(fullClasspath in Compile, mainClass in(Compile, run), runner in(Compile, run))

// exclude Scala library from assembly
assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

