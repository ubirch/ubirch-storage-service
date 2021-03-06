/*
 * Copyright 2015 ubirch GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.ubirch.backend.storage.services.elasticsearch.components

import java.net.{URI, URL}

import com.typesafe.scalalogging.LazyLogging
import com.ubirch.util.json.Json4sUtil
import uk.co.bigbeeconsultants.http._
import uk.co.bigbeeconsultants.http.header.MediaType._
import uk.co.bigbeeconsultants.http.request.RequestBody
import uk.co.bigbeeconsultants.http.response.Status._

import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

import scala.concurrent.Future

/**
  * An elastic search based implementation of the timeseries storage.
  * It requires a timestamp field to be present which is by default '@timestamp',
  * just as used by Kibana. That makes it very simple to store data that is
  * then indexed and used in [Kibana](https://www.elastic.co/products/kibana).
  *
  * This storage client works asynchronously and returns futures for all
  * operations. Please be aware that it always takes a small amount of time until
  * a data point saved to ElasticSearch is indexed and thus available for search.
  *
  * You need to provide the base URI to the REST endpoint of ElasticSearch as well
  * as an http client implementation.
  *
  * @author Matthias L. Jugel
  */

trait ElasticSearchKeyValueStorage extends KeyValueStorageComponent[JValue] with LazyLogging {

  val baseUrl: String

  val index: String

  val datatype: String

  lazy val uri: URI = new URL(s"$baseUrl/$index/$datatype/").toURI

  val httpClient = new HttpClient

  override protected lazy val storage = new ElasticSearchKeyValueStorage(httpClient, uri)

  class ElasticSearchKeyValueStorage(client: HttpClient, uri: URI) extends KeyValueStorage[JValue] {
    implicit val context = scala.concurrent.ExecutionContext.Implicits.global
    implicit val formats = DefaultFormats

    override def fetch(key: String): Future[Option[JValue]] = Future {

      val cUrl = uri.resolve(s"$key/_source").toURL
      logger.debug(s"current fetch url: $cUrl")
      val response = httpClient.get(cUrl)

      response match {
        case r if !r.status.isSuccess =>
          logger.error(s"invalid response for key: $key")
          None
        case r =>
          Json4sUtil.string2JValue(r.body.asString) match {
            case Some(respJval) =>
              Some(respJval)
            case None =>
              logger.error(s"could not parse response: ${r.body}")
              None
          }
      }
    }

    /**
      *
      * @param limit     limits the reuslts, 0 means no limit
      * @param sortedBy  valid field name for ordering
      * @param sortOrder asc|desc, asc is default
      * @param filter    valid filter expression for filtering, e.g. hash:oPuXzuOUticwd3iFJjc
      * @return
      */
    override def fetchAll(limit: Int = 10000, sortedBy: Option[String] = None, sortOrder: String = "asc", filter: Option[String]): Future[Option[List[JValue]]] = Future {

      val filterJVal: JValue = if (filter.isDefined) {
        val f = filter.get.split(":")
        if (f.size == 2) {
          parse(
            s"""
               |{
               |  "query" :
               |  {
               |    "term" :
               |    {
               |      "${f(0)}" : "${f(1)}"
               |    }
               |  }
               |}
             """.stripMargin
          )
        } else Json4sUtil.string2JValue("{}")
      } else Json4sUtil.string2JValue("{}")

      val sortJVal: JValue = if (sortedBy.isDefined) {
        parse(
          s"""
             |{
             | "sort" :
             | {
             | "${sortedBy.get}":
             |   {
             |     "order" : "$sortOrder"
             |   }
             | }
             |}
          """.stripMargin
        )
      } else parse("{}")

      val jQueryStr: JValue = parse(
        s"""
           |{"size" : $limit}
        """.stripMargin
      )

      val cUrl = uri.resolve("_search").toURL

      val reqBody = compact(render(filterJVal merge sortJVal merge jQueryStr)).stripMargin

      val jsonBody = RequestBody(
        reqBody
        , APPLICATION_JSON)

      logger.debug(s"current fetch url: $cUrl")
      val response = httpClient.post(cUrl, Some(jsonBody))

      response match {
        case r if !r.status.isSuccess =>
          logger.error(s"invalid response")
          None
        case r =>
          Json4sUtil.string2JValue(r.body.asString) match {
            case Some(respJval) =>
              (respJval \ "hits" \ "hits").extractOpt[List[JValue]] match {
                case Some(r) =>
                  Some(
                    r.map { e =>
                      (e \ "_source").extract[JValue]
                    })
                case None => None
              }
            case None =>
              logger.error(s"could not parse response: ${r.body}")
              None
          }
      }
    }

    override def store(key: String, value: JValue): Future[Option[JValue]] = Future {

      val valueStr = Json4sUtil.jvalue2String(value)
      val jsonBody = RequestBody(valueStr, APPLICATION_JSON)
      logger.debug(s"store with key $key: $valueStr")
      val cUrl = uri.resolve(s"$key").toURL
      httpClient.put(cUrl, jsonBody) match {
        case resp if resp.status.isSuccess =>
          Json4sUtil.string2JValue(resp.body.asString) match {
            case Some(respJval) =>
              Some(respJval)
            case None =>
              logger.error(s"could not parse repsonse as JValue: ${resp.body}")
              None
          }
        case resp if !resp.status.isSuccess =>
          logger.error(s"invalid response: ${resp.body}")
          None
        case _ =>
          logger.error("could not parse repsonse")
          None
      }
    }

    override def delete(key: String): Future[Boolean] = Future {
      val response = httpClient.delete(uri.resolve(key).toURL)
      response.status match {
        case S200_OK => true
        case _ => false
      }
    }
  }

}
