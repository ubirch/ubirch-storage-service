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

import scala.concurrent.Future

/**
  * Timeseries storage component for basic functionality of storing and retrieving timeseries data.
  * A concrete implementation of the underlying storage service is necessary.
  *
  * @author Matthias L. Jugel
  */
trait TimesSeriesStorageComponent[T] {
  protected val storage: Storage[T]

  protected trait Storage[A] {
    def fetch(key: String): Future[Option[A]]

    def fetch(key: String, count: Int): Future[Iterable[A]]

    def fetch(key: String, from: Long, to: Long): Future[Iterable[A]]

    def store(key: String, value: A): Future[Option[A]]

    def delete(key: String): Future[Boolean]
  }

}