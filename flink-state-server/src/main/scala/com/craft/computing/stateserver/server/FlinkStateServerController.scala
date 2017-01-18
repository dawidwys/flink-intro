package com.craft.computing.stateserver.server

import java.time.Instant
import java.util.concurrent.TimeUnit

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.craft.computing.stateserver.data.{JsonSupport, Query, QueryResponse}
import com.craft.computing.stateserver.queryclient.QueryClient

import scala.collection.JavaConverters._
import scala.util.matching.Regex

class FlinkStateServerController(queryClient: QueryClient) extends JsonSupport {

  val route: Route =
    pathSingleSlash {
      complete("Hi hello!")
    } ~ post {
      path("search") {
        complete(Seq("login", "logout", "button"))
      }
    } ~ post {
      path("query") {
        entity(as[Query]) { query =>

          val startTimeMs = Instant.parse(query.range.from).toEpochMilli
          val intervalMs = parseToMillis(query.interval)

          val response = query.targets.toArray.map {
            queryTarget => {
              QueryResponse(
                target = queryTarget.target,
                datapoints = queryFlink(queryTarget.target, startTimeMs, query.maxDataPoints.toLong, intervalMs)
              )
            }
          }
          complete(response)
        }
      }
    }

  def queryFlink(key: String, startTime: Long, numPoints: Long, interval: Long): Array[Array[Long]] = {
    val results = queryClient.executeQuery(key).asScala.toArray
    results.map { dataPoint =>
      Array(dataPoint.getValue.toLong, dataPoint.getTimeStampMs)
    }
  }

  def parseToMillis(interval: String): Long = {
    val unitMap = Map("ms" -> "MILLISECONDS", "s" -> "SECONDS")
    val intervalPattern = new Regex("""(\d*)(.*)""")
    val intervalPattern(period, unit) = interval
    val timeUnit = TimeUnit.valueOf(unitMap(unit))
    timeUnit.toMillis(period.toLong)
  }

}
