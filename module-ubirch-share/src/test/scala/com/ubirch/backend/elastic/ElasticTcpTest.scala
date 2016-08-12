package com.ubirch.backend.elastic

import java.net.InetAddress

import com.typesafe.scalalogging.LazyLogging
import com.ubirch.backend.util.{JsonUtil, UUIDUtil}
import com.roundeights.hasher.Implicits._
import com.roundeights.hasher.{Digest, Hash}
import org.scalatest.{FeatureSpec, Matchers}

//import org.elasticsearch.client.Client
//import org.elasticsearch.client.transport.TransportClient
//import org.elasticsearch.common.transport.InetSocketTransportAddress
//import org.elasticsearch.common.xcontent.XContentFactory._
import org.joda.time.DateTime

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by derMicha on 05/08/16.
  */
//case class Supi(name: String, wumms: String)
//
//class ElasticTcpTest extends FeatureSpec
//  with Matchers
//  with LazyLogging {
//
//  val hashValue = UUIDUtil.uuidStr.sha256.hex
//
//  feature("Elasticsearch") {
//
//    scenario("store and fetch a hash") {
//      val client = TransportClient.builder().build()
//        .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9200))
//
//
//      val sup = Supi(name = "Otto", wumms = "humms")
//      val id = UUIDUtil.uuidStr
//      info(s"id: $id")
//      JsonUtil.any2jvalue(sup) match {
//        case Some(supJObj) =>
//          val resp = client
//            .prepareIndex("data", "datum")
//            .setSource(
//              JsonUtil.jvalue2String(supJObj)
//              //            jsonBuilder()
//              //            .startObject()
//              //            .field("user", "kimchy")
//              //            .field("postDate", new DateTime())
//              //            .field("message", "trying out Elasticsearch")
//              //            .endObject()
//            )
//            .setId(id)
//            .get()
//
//          resp.isCreated shouldBe true
//          resp.getId shouldBe id
//        case None =>
//          fail("mist!")
//      }
//      // on shutdown
//      client.close()
//    }
//  }
//}
