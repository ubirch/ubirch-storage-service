package com.ubirch.backend.util

import com.ubirch.backend.config.Config

import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.eclipse.paho.client.mqttv3.{MqttClient, MqttConnectOptions}

/**
  * Created by derMicha on 30/07/16.
  */
object BrokerUtil {

  def connectBroker(topic: String, qos: Int, clientId: String): MqttClient = {
    val broker = Config.mqttBroker
    val persistence = new MemoryPersistence()
    val mqttClient = new MqttClient(broker, clientId, persistence)
    val connOpts = new MqttConnectOptions()
    connOpts.setCleanSession(true)
    mqttClient.connect(connOpts)
    mqttClient
  }
}
