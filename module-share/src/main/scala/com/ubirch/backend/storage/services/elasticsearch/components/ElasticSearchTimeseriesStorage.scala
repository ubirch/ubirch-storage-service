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

import java.io.InputStreamReader
import java.net.{URI, URLEncoder}
import java.text.SimpleDateFormat
import java.util.{Date, Locale, TimeZone}

import com.typesafe.scalalogging.LazyLogging
import org.json4s.JsonDSL._
import org.json4s.native.Serialization._
import org.json4s.native.JsonMethods._
import org.apache.http.client.methods.{HttpDelete, HttpPost, HttpUriRequest}
import org.apache.http.entity.{ContentType, StringEntity}
import org.apache.http.impl.client.{CloseableHttpClient, HttpClients}
import org.json4s.DefaultFormats
import org.json4s.JsonAST.{JInt, JValue}

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
trait ElasticSearchTimeseriesStorage extends TimesSeriesStorageComponent[JValue] with LazyLogging {
  val uri: URI
  val timestamp: String = "date"
  val httpClient: CloseableHttpClient = HttpClients.createDefault()

  /**
    * Simple conversion check for a timestamp. If it contains the timestamp,
    * return the data point unchanged, otherwise merge a timestamp into the
    * datapoint and return it.
    *
    * @param v the current data point
    * @return the data point with a timestamp
    */
  protected def validateTimestamp(v: JValue): JValue = (v \ timestamp).toOption match {
    case Some(JInt(ts)) => v.merge(timestamp -> threadSafeDateFormat.get.format(ts): JValue)
    case None => v.merge(timestamp -> threadSafeDateFormat.get.format(new Date().getTime): JValue)
    case _ => v
  }

  /**
    * Format a given date into the required timestamp formatted string.
    *
    * @param timestamp the date to format
    * @return the formatted string
    */
  protected def format(timestamp: Long): String = threadSafeDateFormat.get.format(timestamp)

  /**
    * The timestamp parser format for this storage.
    *
    * @return a SimpleDateFormat instance used to parse and format timestamps.
    */
  protected def timestampFormat: SimpleDateFormat = {
    val df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH)
    df.setTimeZone(TimeZone.getTimeZone("UTC"))
    df
  }

  private val threadSafeDateFormat = new ThreadLocal[SimpleDateFormat]() {
    override def initialValue() = timestampFormat
  }

  override protected lazy val storage = new ElasticSearchStorage(httpClient, uri)

  class ElasticSearchStorage(client: CloseableHttpClient, uri: URI) extends Storage[JValue] {
    implicit val context = scala.concurrent.ExecutionContext.Implicits.global
    implicit val formats = DefaultFormats

    override def fetch(key: String): Future[Option[JValue]] = Future {
      val query = uri.resolve(URLEncoder.encode(key, "UTF-8") + "/").resolve("_search")
      val queryBody: JValue = "sort" -> Seq(timestamp -> ("order" -> "desc"): JValue)
      val postRequest = new HttpPost(query)
      postRequest.setEntity(new StringEntity(write(queryBody), ContentType.APPLICATION_JSON))
      execute(postRequest) match {
        case (Some(r), 200) => (r \ "hits" \ "hits").extract[Array[JValue]].headOption.map(_ \ "_source")
        case (Some(r), status) if status != 404 && status > 300 => throw new Exception((r \ "error").extract[String])
        case (_, _) => None
      }
    }

    override def fetch(key: String, count: Int): Future[Iterable[JValue]] = Future {
      val query = uri.resolve(URLEncoder.encode(key, "UTF-8") + "/").resolve("_search")
      val queryBody: JValue = "sort" -> Seq(timestamp -> ("order" -> "desc"): JValue)
      val postRequest = new HttpPost(query)
      postRequest.setEntity(new StringEntity(write(queryBody), ContentType.APPLICATION_JSON))
      execute(postRequest) match {
        case (Some(r), s) if s != 404 && s > 300 => throw new Exception((r \ "error").extract[String])
        case (Some(r), _) => (r \ "hits" \ "hits").extract[Array[JValue]].take(count).map(_ \ "_source")
        case _ => Iterable.empty
      }
    }

    override def fetch(key: String, from: Long, to: Long): Future[Iterable[JValue]] = Future {
      val query = uri.resolve(URLEncoder.encode(key, "UTF-8") + "/").resolve("_search")
      val queryBody: JValue = ("sort" -> Seq(timestamp -> ("order" -> "desc"): JValue)) ~
        ("query" -> ("range" -> (timestamp -> ("gte" -> format(from)) ~ ("lt" -> format(to)))))

      val postRequest = new HttpPost(query)
      postRequest.setEntity(new StringEntity(write(queryBody), ContentType.APPLICATION_JSON))
      execute(postRequest) match {
        case (Some(r), s) if s != 404 && s > 300 => throw new Exception((r \ "error").extract[String])
        case (Some(r), _) => (r \ "hits" \ "hits").extract[Array[JValue]].map(_ \ "_source")
        case _ => Iterable.empty
      }
    }

    override def store(key: String, value: JValue): Future[Option[JValue]] = Future {
      val postRequest = new HttpPost(uri.resolve(URLEncoder.encode(key, "UTF-8")))
      postRequest.setEntity(new StringEntity(write(validateTimestamp(value)), ContentType.APPLICATION_JSON))
      execute(postRequest) match {
        case (Some(r), status) if status != 404 && status > 300 =>
          throw new Exception((r \ "error").extract[String])
        case _ =>
          Some(value)
      }
    }

    override def delete(key: String): Future[Boolean] = Future {
      val deleteRequest: HttpDelete = new HttpDelete(uri.resolve(URLEncoder.encode(key, "UTF-8")))
      execute(deleteRequest) match {
        case (Some(r), status) if status == 200 =>
          r \ "acknowledged" extractOrElse false
        case (Some(r), status) if status != 404 && status > 300 =>
          throw new Exception((r \ "error").extract[String])
        case _ => false
      }
    }

    private def execute(request: HttpUriRequest): (Option[JValue], Int) = {
      logger.debug(s"execute($request)")
      val response = client.execute(request)
      try {
        val parsed = try {
          Some(parse(new InputStreamReader(response.getEntity.getContent, "UTF-8")))
        } catch {
          case e: Exception =>
            logger.error(s"error while executing http request: $request", e)
            None
        }
        logger.debug(write(parsed))
        (parsed, response.getStatusLine.getStatusCode)
      } finally {
        response.close()
      }
    }
  }

}
