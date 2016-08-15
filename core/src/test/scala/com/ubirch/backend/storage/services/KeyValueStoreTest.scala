package com.ubirch.backend.storage.services

import java.net.{URI, URL}

import com.roundeights.hasher.Implicits._
import com.typesafe.scalalogging.LazyLogging
import com.ubirch.backend.storage.services.elasticsearch._
import com.ubirch.backend.storage.services.elasticsearch.components.ElasticSearchKeyValueStorage
import com.ubirch.backend.storage.config.ServerConfig
import com.ubirch.backend.util.{JsonUtil, UUIDUtil}
import org.joda.time.DateTime
import org.json4s._
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FeatureSpec, Matchers}
import uk.co.bigbeeconsultants.http.HttpClient

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by derMicha on 05/08/16.
  */
case class Dupi(id: String, name: String, wumms: String, created: DateTime = DateTime.now) {
  def asJvalue: JValue = JsonUtil.any2jvalue(this).get

  def asJsonString: String = JsonUtil.jvalue2String(asJvalue)
}

class KeyValueStoreTest extends FeatureSpec
  with Matchers
  with BeforeAndAfterAll
  with LazyLogging {

  val hashValue = UUIDUtil.uuidStr.sha256.hex

  implicit val formats = DefaultFormats.lossless ++ org.json4s.ext.JodaTimeSerializers.all

  val dupiId = UUIDUtil.uuidStr
  val dupi = Dupi(id = dupiId, name = "Susi", wumms = "summs")

  val dupiIds = List(UUIDUtil.uuidStr, UUIDUtil.uuidStr, UUIDUtil.uuidStr)
  val dupis = dupiIds.map { did =>
    Dupi(id = did, name = s"Susi-$did", wumms = s"summs-$did")
  }

  val httpClient = new HttpClient

  override protected def beforeAll(): Unit = {
    logger.info("start clean up before")
    httpClient.delete(new URL(s"${KVStore1.baseUrl}/${KVStore1.index}"))
    httpClient.delete(new URL(s"${KVStore2.baseUrl}/${KVStore2.index}"))
    httpClient.delete(new URL(s"${KVStore3.baseUrl}/${KVStore3.index}"))

    dupis.foreach { d =>
      Await.result(KVStore2.store(d.id, d.asJvalue), 10 seconds)
      Await.result(KVStore3.store(d.id, d.asJvalue), 10 seconds)
    }
    //@TODO try to remove that ugly sleep
    Thread.sleep(1000)
  }

  override protected def afterAll(): Unit = {
    logger.info("start clean up after")
    httpClient.delete(new URL(s"${KVStore1.baseUrl}/${KVStore1.index}"))
    httpClient.delete(new URL(s"${KVStore2.baseUrl}/${KVStore2.index}"))
    httpClient.delete(new URL(s"${KVStore3.baseUrl}/${KVStore3.index}"))
  }

  feature("Elasticsearch") {

    scenario("store a doc") {
      Await.result(KVStore1.store(dupi.id, dupi.asJvalue), 10 seconds) match {
        case Some(res) =>
          (res \ "_id").extract[String] shouldBe dupi.id
        case None =>
          fail("store failed")
      }
    }

    scenario("fetch a doc") {
      Await.result(KVStore1.fetch(dupi.id), 10 seconds) match {
        case Some(res) =>
          res.extractOpt[Dupi] match {
            case Some(d) =>
              d.id shouldBe dupi.id
              d.name shouldBe dupi.name
              d.wumms shouldBe dupi.wumms
            case None =>
              fail(s"could not fetch key: ${dupi.id}")
          }
        case None =>
          fail("fetch failed")
      }
    }

    scenario("delete hash") {
      val res1 = Await.result(KVStore1.delete(dupi.id), 10 seconds)
      res1 shouldBe true

      val res2 = Await.result(KVStore1.fetch(dupi.id), 10 seconds)
      res2.isDefined shouldBe false
    }

    scenario("fetch all docs") {
      val allDupis = Await.result(KVStore2.fetchAll(limit = 0), 10 seconds)
      allDupis.isDefined shouldBe true
      logger.info(s"allDupis size: ${allDupis.get.size}")
      val allDupisMap: Map[String, Dupi] = allDupis.get.map { jv =>
        val ad = jv.extract[Dupi]
        ad.id -> ad
      }.toMap[String, Dupi]

      logger.info(s"allDupisMap size: ${allDupisMap.size}")
      allDupisMap.keys.size shouldBe dupiIds.size

      dupiIds.foreach { id =>
        allDupisMap.keys.toList.contains(id)
      }
    }

    scenario("fetch all docs with limit") {
      val allDupis = Await.result(KVStore3.fetchAll(limit = 1), 10 seconds)

      allDupis.isDefined shouldBe true
      allDupis.get.size shouldBe 1

      val aDupi = allDupis.get.head.extract[Dupi]
      dupiIds.contains(aDupi.id) shouldBe true
    }

    scenario("fetch all docs with limit ordered") {
      val allDupis = Await.result(KVStore3.fetchAll(ordered = Some("created"), order = "desc"), 10 seconds)

      allDupis.isDefined shouldBe true
      allDupis.get.size shouldBe dupiIds.size

      val aDupi = allDupis.get.take(dupiIds.size).head.extract[Dupi]
      dupiIds.contains(aDupi.id) shouldBe true
    }
  }
}


object KVStore1 extends KeyValueStorage[JValue] with ElasticSearchKeyValueStorage with LazyLogging {

  override val baseUrl = ServerConfig.esUrl

  override val index = "test-data1"

  override val datatype: String = "dupi"

  logger.debug(s"current uri: $uri")
}

object KVStore2 extends KeyValueStorage[JValue] with ElasticSearchKeyValueStorage with LazyLogging {

  override val baseUrl = ServerConfig.esUrl

  override val index = "test-data2"

  override val datatype: String = "dupi"

  logger.debug(s"current uri: $uri")
}

object KVStore3 extends KeyValueStorage[JValue] with ElasticSearchKeyValueStorage with LazyLogging {

  override val baseUrl = ServerConfig.esUrl

  override val index = "test-data3"

  override val datatype: String = "dupi"

  logger.debug(s"current uri: $uri")
}