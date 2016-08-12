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

import scala.collection.SortedSet
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * In-memory time series storage component that is based on a map.
  * It is more a test implementation that actually useful as it may fill up
  * memory.
  *
  * Access to the underlying map is synchronized, so this class should be
  * thread-safe.
  *
  * @author Matthias L. Jugel
  */
trait MapStorageComponent[T] extends TimesSeriesStorageComponent[T] {

  protected def timestamp(v: T): Long

  override protected lazy val storage = new MapStorage[T](timestamp)

  class MapStorage[A](timestamp: A => Long) extends Storage[A] {

    case class DataPoint(ts: Long, value: A)

    private implicit val ordering = Ordering.by[DataPoint, Long](-_.ts)

    private var index: Map[String, SortedSet[DataPoint]] = Map()

    override def fetch(key: String): Future[Option[A]] = Future {
      synchronized {
        index.get(key).flatMap(_.headOption).map(_.value)
      }
    }

    override def fetch(key: String, count: Int): Future[Iterable[A]] = Future {
      synchronized {
        index.get(key) match {
          case Some(values) => values.take(count).toSeq.map(_.value)
          case None => Iterable.empty
        }
      }
    }

    override def fetch(key: String, from: Long, to: Long): Future[Iterable[A]] = Future {
      synchronized {
        index.get(key) match {
          case Some(values) =>
            val tmp = values.toSeq.collect {
              case v if v.ts >= from && v.ts < to => v.value
            }
            tmp
          case None => Iterable.empty
        }
      }
    }

    override def store(key: String, value: A): Future[Option[A]] = Future {
      synchronized {
        val newValues = index.get(key) match {
          case Some(values) =>
            index += (key -> (values + DataPoint(timestamp(value), value)))
          case None =>
            index += (key -> SortedSet(DataPoint(timestamp(value), value))(ordering))
        }
        Some(value)
      }
    }

    override def delete(key: String): Future[Boolean] = Future {
      synchronized {
        index -= key
        true
      }
    }
  }

}

