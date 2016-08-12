package com.ubirch.backend.config

import com.typesafe.config.ConfigFactory

/**
  * author: cvandrei
  * since: 2016-07-27
  */
object Config {

  private lazy val conf = ConfigFactory.load

  val mqttBroker = "tcp://localhost:1883"
  val outboxTopic = "ubirch/generic/outbox"
  val inboxTopic = "ubirch/generic/inbox"
}
