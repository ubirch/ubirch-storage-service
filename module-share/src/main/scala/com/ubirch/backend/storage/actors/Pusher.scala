package com.ubirch.backend.storage.actors

import akka.actor.{Actor, ActorLogging}
import akka.event.LoggingReceive
import com.ubirch.backend.config.Config
import com.ubirch.backend.util.{BrokerUtil, JsonUtil}
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.json4s.JValue

/**
  * Created by derMicha on 30/07/16.
  */
class Pusher extends Actor with ActorLogging {

  val topic = Config.outboxTopic
  val qos = 2
  val clientId = "pusher-" + java.util.UUID.randomUUID().toString

  val brokerClient = BrokerUtil.connectBroker(topic = topic, qos = qos, clientId = clientId)

  def receive = LoggingReceive {
    case value: JValue =>
      val jsonStr = JsonUtil.jvalue2String(value)
      log.debug(s"Topic: $topic - Message: $jsonStr")
      val message = new MqttMessage(jsonStr.getBytes)
      message.setQos(qos)
      brokerClient.publish(topic, message)
    case _ =>
      log.error(s"got unknown message")
  }

  @scala.throws[Exception](classOf[Exception])
  override def postStop(): Unit = brokerClient.disconnect()

}
