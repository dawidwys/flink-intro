import FlinkDependencies._
import sbt.Keys.updateOptions

lazy val commonSettings = Seq(
  scalaVersion := "2.11.8",
  updateOptions := updateOptions.value.withLatestSnapshots(false)
)

lazy val ownSettings = Seq(
  organization := "com.craft.computing",
  version := "0.1-SNAPSHOT"
)

lazy val flinkIntroModel = (project in file("flink-intro-model"))
  .settings(commonSettings)
  .settings(ownSettings)

lazy val flinkStateServer = (project in file("flink-state-server"))
  .dependsOn(flinkIntroModel)
  .settings(commonSettings)
  .settings(ownSettings)
  .settings(libraryDependencies ++= flinkDependencies)

lazy val flinkEventGenerator = (project in file("flink-event-generator"))
  .dependsOn(flinkIntroModel)
  .settings(commonSettings)
  .settings(ownSettings)
  .settings(libraryDependencies ++= (flinkDependencies ++ flinkKafkaDependencies))

lazy val flinkCep = (project in file("flink-cep"))
  .dependsOn(flinkIntroModel)
  .settings(commonSettings)
  .settings(ownSettings)
  .settings(
    libraryDependencies ++= (flinkDependencies ++ flinkKafkaDependencies ++ flinkCepDependencies))

lazy val flinkQueryableJob = (project in file("flink-queryable-job"))
  .dependsOn(flinkIntroModel)
  .settings(commonSettings)
  .settings(ownSettings)
  .settings(libraryDependencies ++= (flinkDependencies ++ flinkKafkaDependencies))

def changeProvided(proj: Project) = libraryDependencies := (libraryDependencies in proj).value.map {
  module =>
    if (module.configurations.equals(Some("provided"))) {
      module.copy(configurations = None)
    } else {
      module
    }
}

lazy val mainRunnerFlinkEventGenerator = project.in(file("flink-event-generator/mainRunner"))
  .dependsOn(flinkEventGenerator)
  .settings(commonSettings)
  .settings(changeProvided(flinkEventGenerator))

lazy val mainRunnerFlinkQueryableJob = project.in(file("flink-queryable-job/mainRunner"))
  .dependsOn(flinkQueryableJob)
  .settings(commonSettings)
  .settings(changeProvided(flinkQueryableJob))

lazy val mainRunnerFlinkCep = project.in(file("flink-cep/mainRunner"))
  .dependsOn(flinkCep)
  .settings(commonSettings)
  .settings(changeProvided(flinkCep))

lazy val mainRunnerFlinkStateServer = project.in(file("flink-state-server/mainRunner"))
  .dependsOn(flinkStateServer)
  .settings(commonSettings)
  .settings(changeProvided(flinkStateServer))

lazy val root = project.in(file("."))
  .aggregate(flinkEventGenerator, flinkQueryableJob, flinkStateServer, flinkCep)

resolvers in ThisBuild ++= Seq(
  "Apache Development Snapshot Repository" at
    "https://repository.apache.org/content/repositories/snapshots/",
  Resolver.mavenLocal)
