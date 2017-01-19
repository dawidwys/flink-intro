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

import com.craft.computing.events._
import org.apache.flink.api.common.typeinfo.{TypeHint, TypeInformation}
import org.apache.flink.cep.scala.CEP
import org.apache.flink.cep.scala.pattern.Pattern
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010
import org.apache.flink.streaming.util.serialization.TypeInformationSerializationSchema
import org.rogach.scallop.ScallopConf

object CepAlertJob {

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

    val eventStream = env.addSource(kafkaConsumer).map(e => (e.userId, e)).keyBy(0).map(_._2)

    val shortSessionPattern = Pattern.begin[ClickEvent]("login")
      .subtype(classOf[LoginClickEvent]).followedBy("logout").subtype(classOf[LogoutClickEvent]).within(Time.seconds(5))

    val creditCardRequest = Pattern.begin[ClickEvent]("credit")
      .subtype(classOf[ButtonClickEvent]).where(_.elem == Element.AddCredit)
      .followedBy("card").subtype(classOf[ButtonClickEvent]).where(_.elem == Element.RequestCard)

    CEP.pattern(eventStream, shortSessionPattern).select(
      events =>
        s"User: ${events("login").userId} spent just ${
          (events("logout").timestamp - events("login").timestamp) / 1000
        } seconds"
    ).print()

    CEP.pattern(eventStream, creditCardRequest).select(
      events =>
        s"User: ${events("credit").userId} probably wants a credit"
    ).print()


    env.execute("Cep alert job")
  }

}
