package com.ubirch.backend.storage.model

import org.joda.time.DateTime

/**
  * Created by derMicha on 01/08/16.
  */
case class HashValue(hash: String, algorithm: String, created: DateTime = new DateTime())