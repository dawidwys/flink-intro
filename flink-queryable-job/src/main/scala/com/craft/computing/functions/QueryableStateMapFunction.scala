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
package com.craft.computing.functions

import java.util

import com.jgrier.flinkstuff.data.KeyedDataPoint
import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.api.common.state.ValueStateDescriptor
import org.apache.flink.util.Collector

class QueryableStateMapFunction() extends RichFlatMapFunction[
  KeyedDataPoint[Integer]
  , KeyedDataPoint[Integer]] {

  private val queryableStateDescriptor =
    new ValueStateDescriptor[util.ArrayList[KeyedDataPoint[Integer]]](
      "type-time-series-count",
      classOf[util.ArrayList[KeyedDataPoint[Integer]]],
      new util.ArrayList[KeyedDataPoint[Integer]]())
  private lazy val state = getRuntimeContext.getState(queryableStateDescriptor)
  queryableStateDescriptor.setQueryable("type-time-series-count")

  override def flatMap(value: KeyedDataPoint[Integer], out: Collector[KeyedDataPoint[Integer]])
  : Unit = {
    val list = state.value()
    list.add(value)
    state.update(list)
  }
}

object QueryableStateMapFunction {
  def apply(): QueryableStateMapFunction = new QueryableStateMapFunction()
}

