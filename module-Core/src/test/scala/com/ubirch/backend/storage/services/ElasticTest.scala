package com.ubirch.backend.storage.services

import java.net.URI

import com.roundeights.hasher.Implicits._
import com.typesafe.scalalogging.LazyLogging
import com.ubirch.backend.storage.services.elasticsearch._
import com.ubirch.backend.storage.services.elasticsearch.components.ElasticSearchKeyValueStorage
import com.ubirch.backend.storage.config.ServerConfig
import com.ubirch.backend.util.{JsonUtil, UUIDUtil}
import org.json4s._
import org.scalatest.{FeatureSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by derMicha on 05/08/16.
  */
case class Dupi(id: String, name: String, wumms: String)

class ElasticTest extends FeatureSpec
  with Matchers
  with LazyLogging {

  val hashValue = UUIDUtil.uuidStr.sha256.hex

  implicit val formats = DefaultFormats.lossless ++ org.json4s.ext.JodaTimeSerializers.all

  val dupiId = UUIDUtil.uuidStr
  val dupi = Dupi(id = dupiId, name = "Susi", wumms = "summs")
  val dupiJval = JsonUtil.any2jvalue(dupi).get
  val dupiStr = JsonUtil.jvalue2String(dupiJval)

  feature("Elasticsearch") {

    scenario("store hash") {
      Await.result(ElasticStore.store(dupi.id, dupiJval), 10 seconds) match {
        case Some(res) =>
          (res \ "_id").extract[String] shouldBe dupi.id
        case None =>
          fail("store failed")
      }
    }

    scenario("fetch hash") {
      Await.result(ElasticStore.fetch(dupi.id), 10 seconds) match {
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
      val res1 = Await.result(ElasticStore.delete(dupi.id), 10 seconds)
      res1 shouldBe true

      val res2 = Await.result(ElasticStore.fetch(dupi.id), 10 seconds)
      res2.isDefined shouldBe false
    }
  }
}


object ElasticStore extends KeyValueStorage[JValue] with ElasticSearchKeyValueStorage with LazyLogging {

  override val baseUrl = ServerConfig.esUrl

  override val index = "test-data"

  override val datatype: String = "dupi"

  logger.debug(s"current uri: $uri")
}