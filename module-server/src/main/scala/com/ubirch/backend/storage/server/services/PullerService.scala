package com.ubirch.backend.storage.server.services

import akka.actor.{ActorSystem, Props}
import com.typesafe.scalalogging.LazyLogging
import com.ubirch.backend.storage.actors.HasherService
import com.ubirch.backend.config.Config
import com.ubirch.backend.storage.config.ServerConfig
import com.ubirch.backend.storage.util.JsonUtil
import org.eclipse.paho.client.mqttv3._

object PullerService extends MqttCallback with LazyLogging {
  private val BROKER_URL: String = Config.mqttBroker
  private val TOPIC: String = Config.inboxTopic
  private val clientId: String = "puller-" + java.util.UUID.randomUUID.toString

  private var myClient: MqttClient = null
  private var connOpt: MqttConnectOptions = null

  implicit val system = ActorSystem()
  val hasherServiceActor = system.actorOf(Props[HasherService], name = ServerConfig.hasherServiceActor)

  def connectionLost(t: Throwable) {
    logger.debug("Connection lost!")
  }

  @throws[Exception]
  def messageArrived(topic: String, message: MqttMessage) {
    logger.debug(s"Topic: $topic Message: ${message.toString}")
    try {
      val payload = message.toString
      val jval = JsonUtil.string2ToHash(payload) match {
        case Some(th) =>
          hasherServiceActor ! th
        case None =>
          logger.error("got invalide message")
      }

    }
    catch {
      case e: Throwable =>
        logger.error("wrong input, have to be ToHash", e)
    }

  }

  def deliveryComplete(token: IMqttDeliveryToken) {
    logger.debug("-------------------------------------------------")
    logger.debug("| deliveryComplete")
    logger.debug("| Token:" + token)
    logger.debug("-------------------------------------------------")
  }

  def runClient() {
    connOpt = new MqttConnectOptions
    connOpt.setCleanSession(true)
    connOpt.setKeepAliveInterval(30)
    try {
      myClient = new MqttClient(BROKER_URL, clientId)
      myClient.setCallback(this)
      myClient.connect(connOpt)
    }
    catch {
      case e: MqttException =>
        logger.error("could not connect to mqtt server", e)
    }
    logger.debug("Connected to " + BROKER_URL)
    val myTopic: String = TOPIC
    val topic: MqttTopic = myClient.getTopic(myTopic)
    try {
      val subQoS: Int = 0
      myClient.subscribe(myTopic, subQoS)
    }
    catch {
      case e: Exception =>
        logger.error(s"could not subscribe to topic $myTopic", e)
    }
  }

  def stopClient() {
    try {
      myClient.disconnect()
    }
    catch {
      case e: Exception =>
        logger.error(s"error while disconnecting from mqtt server", e)
    }
  }
}