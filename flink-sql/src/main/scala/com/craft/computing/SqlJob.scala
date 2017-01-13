package com.craft.computing

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Properties

import com.craft.computing.events.{ButtonClickEvent, ClickEvent, Element}
import org.apache.flink.api.common.typeinfo.{TypeHint, TypeInformation}
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010
import org.apache.flink.streaming.util.serialization.TypeInformationSerializationSchema
import org.apache.flink.table.api.TableEnvironment
import org.rogach.scallop.ScallopConf
import org.apache.flink.api.scala._
import org.apache.flink.table.api.scala._

object SqlJob {

  case class QueryableButtonClickEvent(uuid: String, elem: String, timestamp: Long)

  class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
    val kafkaBootstrap = opt[String](required = true, descr = "Kafka bootstrap server")
    val kafkaTopic = trailArg[String](
      descr = "Kafka topic to write to",
      default = Some("ClickStream"),
      required = false)
    verify()
  }

  def kafkaProperties(bootstrapServer: String): Properties = {
    val properties = new Properties()
    properties.setProperty("bootstrap.servers", bootstrapServer)
    properties
  }

  def main(args: Array[String]) {

    val conf = new Conf(args)
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val tableEnv = TableEnvironment.getTableEnvironment(env)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    val deserializationSchema = new TypeInformationSerializationSchema(
      TypeInformation
        .of(new TypeHint[ClickEvent] {}),
      env.getConfig)

    val kafkaConsumer = new FlinkKafkaConsumer010[ClickEvent](
      conf.kafkaTopic(),
      deserializationSchema,
      kafkaProperties(conf.kafkaBootstrap())).assignTimestampsAndWatermarks(
      new AscendingTimestampExtractor[ClickEvent] {

        override def extractAscendingTimestamp(element: ClickEvent) = element.timestamp
      })

    val eventStream = env.addSource(kafkaConsumer).flatMap(
      x => x match {
        case t: ButtonClickEvent => Some(
          QueryableButtonClickEvent(
            t.userId.toString,
            t.elem.toString,
            t.timestamp))
        case _ => None
      })

    tableEnv.registerDataStream("events", eventStream)

    eventStream.toTable(tableEnv).where('elem === "AddCredit")
      .toDataStream[QueryableButtonClickEvent].print()

    tableEnv.sql("select * from events where elem = 'AddCredit'")
      .toDataStream[QueryableButtonClickEvent].print()

    env.execute("Sql job")
  }

}
