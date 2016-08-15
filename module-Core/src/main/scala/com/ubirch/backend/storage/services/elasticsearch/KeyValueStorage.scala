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

package com.ubirch.backend.storage.services.elasticsearch

import com.ubirch.backend.storage.services.elasticsearch.components.KeyValueStorageComponent

/**
  * Timeseries storage that can have different implementations.
  *
  * {{{object MyStorage extends Storage with MapStorageComponent}}}
  *
  * @author Matthias L. Jugel
  */
class KeyValueStorage[T] {
  this: KeyValueStorageComponent[T] =>

  /**
    * Fetch the latest data point.
    *
    * @param key the id under which the data is stored
    * @return a single data point
    */
  def fetch(key: String) = storage.fetch(key)

  /**
    * fetches all docs of current index
    *
    * @param limit limits the reuslt size, 0 means no limit
    * @return
    */
  def fetchAll(limit: Int = 0, ordered: Option[String] = None, order: String = "asc", filter: Option[String] = None) = storage.fetchAll(limit = limit, ordered = ordered, order = order, filter = filter)

  /**
    * Store a data point. It is up to the underlying implementation to extract a timestamp
    * from the data point or use the local timestamp to store the data.
    *
    * @param key   the id unser which the data should be stored
    * @param value the new data point
    * @return the new data point
    */
  def store(key: String, value: T) = storage.store(key, value)

  /**
    * Delete complete data for a given key.
    *
    * @param key the key under which the data is stored
    * @return whether the deletion was successful (also returns true if no data exists)
    */
  def delete(key: String) = storage.delete(key)
}
