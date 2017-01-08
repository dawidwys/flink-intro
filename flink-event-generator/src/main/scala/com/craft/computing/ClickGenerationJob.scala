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

import com.craft.computing.events.{ClickEvent, ClickEventTimedSource}
import org.apache.flink.api.common.typeinfo.{TypeHint, TypeInformation}
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010
import org.apache.flink.streaming.util.serialization.TypeInformationSerializationSchema
import org.rogach.scallop.ScallopConf

object ClickGenerationJob {

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
    val stream = env.addSource(new ClickEventTimedSource)

    val kafkaProducerConfig = FlinkKafkaProducer010.writeToKafkaWithTimestamps(
      stream.javaStream,
      conf.kafkaTopic(),
      new TypeInformationSerializationSchema(
        TypeInformation.of(new TypeHint[ClickEvent] {}), env.getConfig),
      kafkaProperties(conf.kafkaBootstrap()))
    kafkaProducerConfig.setWriteTimestampToKafka(true)


    env.execute("Click Event Generator")
  }

}
