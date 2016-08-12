package com.ubirch.backend.storage.actors

import akka.actor.{Actor, ActorLogging, Props}
import akka.event.LoggingReceive
import com.roundeights.hasher.Implicits._
import com.ubirch.backend.storage.config.{ServerConst, ServerConfig}
import com.ubirch.backend.storage.model.{HashValue, ToHash}
import com.ubirch.backend.util.JsonUtil

import scala.language.postfixOps

/**
  * Created by derMicha on 29/07/16.
  */
class Hasher extends Actor with ActorLogging {

  val pusherActor = context.actorOf(Props[Pusher], name = ServerConfig.pusherActor)

  def receive = LoggingReceive {
    case toHash: ToHash if toHash.algorithm.equals(ServerConst.HASH_MD5) =>
      val value: String = toHash.value
      val hv = HashValue(toHash.value.md5.hex, algorithm = ServerConst.HASH_MD5)
      sender ! hv
      self ! hv
    case toHash: ToHash if toHash.algorithm.equals(ServerConst.HASH_SHA256) =>
      val value: String = toHash.value
      val hv = HashValue(toHash.value.sha256.hex, algorithm = ServerConst.HASH_SHA256)
      sender ! hv
      self ! hv
    case toHash: ToHash =>
      val value: String = toHash.value
      val hv = HashValue(toHash.value.sha512.hex, algorithm = ServerConst.HASH_SHA512)
      sender ! hv
      self ! hv
    case hv: HashValue =>
      JsonUtil.any2jvalue(hv) match {
        case Some(jval) =>
        case None =>
          log.error("invalid hash value")
      }
    case _ =>
      log.error("unknown message")
  }
}