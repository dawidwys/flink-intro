import java.lang

import com.jgrier.flinkstuff.data.KeyedDataPoint
import org.apache.flink.api.common.ExecutionConfig
import org.apache.flink.api.common.typeinfo.{TypeHint, TypeInformation}
import org.apache.flink.api.common.typeutils.TypeSerializer
import org.apache.flink.runtime.query.netty.message.KvStateRequestSerializer
import org.scalatest.WordSpec
import scala.collection.JavaConverters._

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

class Test extends WordSpec {
  "Serializer" should {
    "work" in {
      val serializedResult = KvStateRequestSerializer
        .serializeList[KeyedDataPoint[java.lang.Integer]](
        Seq(
          new KeyedDataPoint[java.lang.Integer]("login", 1232314122L, 12),
          new KeyedDataPoint[java.lang.Integer]("login", 1232314122L, 12),
          new KeyedDataPoint[java.lang.Integer]("logout", 1232314122L, 12)).asJava,
        getValueSerializer())
      KvStateRequestSerializer.deserializeList(serializedResult, getValueSerializer())
    }
  }

  private def getValueSerializer(): TypeSerializer[KeyedDataPoint[java.lang.Integer]] = {
    TypeInformation.of(new TypeHint[KeyedDataPoint[lang.Integer]]() {})
      .createSerializer(new ExecutionConfig)
  }
}
