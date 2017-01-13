package com.craft.computing.stateserver.data

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val queryRangeFormat = jsonFormat2(QueryRange)
  implicit val queryTargetFormat = jsonFormat2(QueryTarget)
  implicit val queryFormat = jsonFormat7(Query)
  implicit val queryResponseFormat = jsonFormat2(QueryResponse)
}

