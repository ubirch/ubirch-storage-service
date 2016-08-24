package com.ubirch.backend.chain.model

import org.joda.time.DateTime

/**
  * author: cvandrei
  * since: 2016-07-28
  */
case class HashRequest(
                 data: String,
                 created: DateTime = DateTime.now()
               )

case class HashedData(
                 hash: String,
                 created: DateTime = DateTime.now()
               )
