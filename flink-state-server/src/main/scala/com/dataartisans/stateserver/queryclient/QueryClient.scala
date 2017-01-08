package com.dataartisans.stateserver.queryclient

import java.{lang, util}

import com.jgrier.flinkstuff.data.KeyedDataPoint
import org.apache.flink.api.common.typeinfo.{TypeHint, TypeInformation}
import org.apache.flink.api.common.typeutils.TypeSerializer
import org.apache.flink.api.common.{ExecutionConfig, JobID}
import org.apache.flink.configuration.GlobalConfiguration
import org.apache.flink.runtime.query.QueryableStateClient
import org.apache.flink.runtime.query.netty.message.KvStateRequestSerializer
import org.apache.flink.runtime.state.{VoidNamespace, VoidNamespaceSerializer}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, duration}

class QueryClient {
  private val client = getQueryableStateClient()
  private val JobId: String = "f83efc0b6f7e9dccf9565bcc7f526bdf"

  def executeQuery(key: String): util.List[KeyedDataPoint[lang.Integer]] = {
    val jobId = JobID.fromHexString(JobId)

    // Serialize request
    val seralizedKey = getSeralizedKey(key)

    // Query Flink state
    val future = client.getKvState(jobId, "type-time-series-count", key.hashCode, seralizedKey)

    // Await async result
    val serializedResult: Array[Byte] = Await.result(
      future, new FiniteDuration(
        10,
        duration.SECONDS))

    // Deserialize response
    val results = deserializeResponse(serializedResult)

    results
  }

  private def deserializeResponse(serializedResult: Array[Byte]): util.List[KeyedDataPoint[lang
  .Integer]] = {
    KvStateRequestSerializer.deserializeList(serializedResult, getValueSerializer())
  }

  private def getQueryableStateClient(): QueryableStateClient = {
    val execConfig = new ExecutionConfig
    val client: QueryableStateClient = new QueryableStateClient(
      GlobalConfiguration
        .loadConfiguration("infrastructure/flink-conf"))
    client
  }

  private def getValueSerializer(): TypeSerializer[KeyedDataPoint[java.lang.Integer]] = {
    TypeInformation.of(new TypeHint[KeyedDataPoint[lang.Integer]]() {})
      .createSerializer(new ExecutionConfig)
  }

  private def getSeralizedKey(key: String): Array[Byte] = {
    val keySerializer: TypeSerializer[String] = TypeInformation.of(new TypeHint[String]() {})
      .createSerializer(null)
    val serializedKey: Array[Byte] =
      KvStateRequestSerializer.serializeKeyAndNamespace(
        key,
        keySerializer,
        VoidNamespace.INSTANCE,
        VoidNamespaceSerializer.INSTANCE)
    serializedKey
  }
}

object QueryClient {

  def apply(): QueryClient = {
    new QueryClient
  }
}
