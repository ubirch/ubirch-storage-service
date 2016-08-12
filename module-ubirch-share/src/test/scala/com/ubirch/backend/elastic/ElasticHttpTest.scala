package com.ubirch.backend.elastic

import java.net.URL

import com.typesafe.scalalogging.LazyLogging
import com.ubirch.backend.util.{JsonUtil, UUIDUtil}
import com.roundeights.hasher.Implicits._

import org.scalatest.{FeatureSpec, Matchers}

import uk.co.bigbeeconsultants.http._
import uk.co.bigbeeconsultants.http.request.RequestBody
import uk.co.bigbeeconsultants.http.header.MediaType._
import uk.co.bigbeeconsultants.http.response.Status._

import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by derMicha on 05/08/16.
  */
case class Dupi(id: String, name: String, wumms: String)

class ElasticHttpTest extends FeatureSpec
  with Matchers
  with LazyLogging {

  val hashValue = UUIDUtil.uuidStr.sha256.hex

  implicit val formats = DefaultFormats.lossless ++ org.json4s.ext.JodaTimeSerializers.all

  val esHost = "http://search-ubirchtrackledata-4ubdfnk44h35mi2lbpfuztp3ui.us-east-1.es.amazonaws.com"
  val esPort = 80
  val esBaseUrl = s"$esHost:$esPort/twitter/tweet"

  val dupiId = UUIDUtil.uuidStr
  val dupi = Dupi(id = dupiId, name = "Susi", wumms = "summs")
  val dupiJval = JsonUtil.any2jvalue(dupi)
  val dupiStr = JsonUtil.jvalue2String(dupiJval.get)

  feature("Elasticsearch") {

    scenario("test SSL") {
      val url = "https://www.google.de"
      val httpClient = new HttpClient
      val response = httpClient.get(url)
      println(response.status)
      println(response.body)

      response.status shouldBe S200_OK
    }

    scenario("store hash") {
      val httpClient = new HttpClient
      val jsonBody = RequestBody(dupiStr, APPLICATION_JSON)
      val response = httpClient.put(new URL(s"$esBaseUrl/$dupiId"), jsonBody)

      println(response.status)
      println(response.body.asString)

      JsonUtil.string2JValue(response.body.asString) match {
        case Some(respJval) =>
          val respDupiId = (respJval \ "_id").extractOpt[String]
          respDupiId.isDefined shouldBe true
          respDupiId.get shouldBe dupiId
        case None =>
          fail("mist!")
      }
    }

    scenario("fetch hash") {
      val httpClient = new HttpClient
      val response = httpClient.get(new URL(s"$esBaseUrl/$dupiId/_source"))
      JsonUtil.string2JValue(response.body.asString) match {
        case Some(respDupiJval) =>
          respDupiJval.extractOpt[Dupi] match {
            case Some(respDupi) =>
              respDupi.id shouldBe dupi.id
              respDupi.name shouldBe dupi.name
              respDupi.wumms shouldBe dupi.wumms
            case None =>
              fail("mist")
          }
        case None =>
          fail("mist!")
      }
      1 shouldBe 1
    }
  }
}
