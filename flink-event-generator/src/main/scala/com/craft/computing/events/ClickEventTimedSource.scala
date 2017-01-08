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
package com.craft.computing.events

import java.time.Instant
import java.util.UUID

import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction
import org.apache.flink.streaming.api.functions.source.SourceFunction.SourceContext
import org.apache.flink.streaming.api.watermark.Watermark

class ClickEventTimedSource extends RichParallelSourceFunction[ClickEvent] {

  @volatile private var running = true
  private val maxInterval = 1000

  override def cancel(): Unit = {
    running = false
  }

  override def run(ctx: SourceContext[ClickEvent]): Unit = {
    while (running) {
      val now = Instant.now().toEpochMilli
      ctx.collectWithTimestamp(generateEvent(now), now)
      ctx.emitWatermark(new Watermark(now))
      Thread.sleep((Math.random() * maxInterval).toLong)
    }
  }

  private def generateEvent(now: Long): ClickEvent = {
    Math.random() match {
      case x if x > 0.9 => LoginClickEvent(UUID.randomUUID(), now)
      case x if x > 0.8 => LogoutClickEvent(UUID.randomUUID(), now)
      case x if x > 0.5 => ButtonClickEvent(UUID.randomUUID(), Element.AddCredit, now)
      case _ => ButtonClickEvent(UUID.randomUUID(), Element.RequestCard, now)
    }
  }
}
