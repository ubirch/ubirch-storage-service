package com.ubirch.backend.chain.model

import org.joda.time.DateTime

/**
  * author: cvandrei
  * since: 2016-07-28
  */
case class Data(
                 data: String,
                 created: DateTime = DateTime.now()
               )

case class Hash(
                 hash: String,
                 created: DateTime = DateTime.now()
               )
