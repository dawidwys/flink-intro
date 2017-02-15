import sbt._

object FlinkDependencies {
  val flinkVersions = new{
    val mainVersion = "1.2.0"
  }

  val flinkDependencies = Seq(
    "org.apache.flink" %% "flink-scala" % flinkVersions.mainVersion % "provided",
    "org.apache.flink" %% "flink-streaming-scala" % flinkVersions.mainVersion % "provided"
  )

  val flinkKafkaDependencies = Seq(
    "org.apache.flink" %% "flink-connector-kafka-0.10" % flinkVersions.mainVersion
  )

  val flinkCepDependencies = Seq(
    "org.apache.flink" %% "flink-cep-scala" % flinkVersions.mainVersion
  )

  val flinkSqlDependencies = Seq(
    "org.apache.flink" %% "flink-table" % flinkVersions.mainVersion
  )

}