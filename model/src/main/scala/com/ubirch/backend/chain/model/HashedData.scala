package com.ubirch.backend.chain.model

import com.ubirch.util.date.DateUtil
import org.joda.time.DateTime

/**
  * author: cvandrei
  * since: 2016-07-28
  */
case class HashRequest(
                 data: String,
                 created: DateTime = DateUtil.nowUTC
               )

case class HashedData(
                 hash: String,
                 created: DateTime = DateUtil.nowUTC
               )
