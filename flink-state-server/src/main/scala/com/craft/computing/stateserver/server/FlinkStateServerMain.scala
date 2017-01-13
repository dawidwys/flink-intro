package com.craft.computing.stateserver.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.craft.computing.stateserver.queryclient.QueryClient
import org.rogach.scallop.ScallopConf

import scala.io.StdIn

object FlinkStateServerMain {

  class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
    val jobId = trailArg[String](required = true, descr = "Queryable job id")
    verify()
  }

  def main(args: Array[String]): Unit = {
    val conf = new Conf(args)
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val controller = new FlinkStateServerController(QueryClient(conf.jobId()))

    val bindingFuture = Http().bindAndHandle(controller.route, "localhost", 8888)
    println(s"Server online at http://localhost:8888/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }

}
