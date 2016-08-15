package com.ubirch

//import com.sandinh.paho.akka.{MqttPubSub, PSConfig, Publish}
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.eclipse.paho.client.mqttv3.{MqttClient, MqttConnectOptions, MqttException, MqttMessage}

object QuickTester extends App {

  //  implicit val as = ActorSystem("MqttPubSubSpec")
  //
  //  implicit val timeout = Timeout(5 seconds) // needed for `?` below
  //
  //  //  lazy val pubsub = TestFSMRef(new MqttPubSub(PSConfig(
  //  lazy val pubsub = as.actorOf(Props(classOf[MqttPubSub], PSConfig(
  //    brokerUrl = "tcp://localhost:1883",
  //    stashTimeToLive = 1.minute,
  //    stashCapacity = 8000, //stash messages will be drop first haft elems when reach this size
  //    reconnectDelayMin = 10.millis, //for fine tuning re-connection logic
  //    reconnectDelayMax = 30.seconds)))
  //
  //  val topic = "wumms/keks"
  //  for (i <- 1 to 100) {
  //    val payload = s"payload $i".getBytes("utf-8")
  //    try {
  //      pubsub ! new Publish(topic, payload, 2)
  //    }
  //    catch {
  //      case t: Throwable =>
  //        println(s"index: $i")
  //        println(t.getMessage)
  //    }
  //  }
  val topic = "wumms/keks"
  val qos = 2
  val broker = "tcp://localhost:1883"
  val clientId = "JavaSample"
  val persistence = new MemoryPersistence()

  try {
    val sampleClient = new MqttClient(broker, clientId, persistence)
    val connOpts = new MqttConnectOptions()
    connOpts.setCleanSession(true)
    println("Connecting to broker: " + broker)
    sampleClient.connect(connOpts)
    println("Connected")
    for (i <- 1 to 1000) {

      val content = s"Message from MqttPublishSample $i"
      println("Publishing message: " + content)
      val message = new MqttMessage(content.getBytes())
      message.setQos(qos)
      sampleClient.publish(topic, message)
      println("Message published")
    }

    sampleClient.disconnect()
    println("Disconnected")
  } catch {
    case me: MqttException =>
      println("reason " + me.getReasonCode)
      println("msg " + me.getMessage)
      println("loc " + me.getLocalizedMessage)
      println("cause " + me.getCause)
      println("excep " + me)
      me.printStackTrace();
  }

}
