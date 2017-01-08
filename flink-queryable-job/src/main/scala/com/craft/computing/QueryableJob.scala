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

import com.craft.computing.events.{ButtonClickEvent, ClickEvent, LoginClickEvent, LogoutClickEvent}
import com.jgrier.flinkstuff.data.KeyedDataPoint
import org.apache.flink.api.common.state.ListStateDescriptor
import org.apache.flink.api.common.typeinfo.{TypeHint, TypeInformation}
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010
import org.apache.flink.streaming.util.serialization.TypeInformationSerializationSchema
import org.rogach.scallop.ScallopConf

object QueryableJob {

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

    type CountEvent = (String, Int, Long)
    env.addSource(kafkaConsumer).map(
      e => e match {
        case LoginClickEvent(_, t) => ("login", 1, t)
        case LogoutClickEvent(_, t) => ("logout", 1, t)
        case ButtonClickEvent(_, _, t) => ("button", 1, t)
      }).keyBy(0).timeWindow(Time.seconds(1))
      .reduce((e1, e2) => (e1._1, e1._2 + e2._2, Math.max(e1._3, e2._3)))
      .map(e => new KeyedDataPoint[java.lang.Integer](e._1, e._3, e._2))
      .keyBy("key")
      .asQueryableState(
        "type-time-series-count",
        new ListStateDescriptor[KeyedDataPoint[java.lang.Integer]](
          "type-time-series-count",
          classOf[KeyedDataPoint[java.lang.Integer]]))

    env.execute("Queryable Job")
  }

}
