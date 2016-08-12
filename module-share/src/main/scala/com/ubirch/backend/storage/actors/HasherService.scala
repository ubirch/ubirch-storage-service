package com.ubirch.backend.storage.actors

import akka.actor.{Actor, ActorLogging, Props}
import akka.event.LoggingReceive
import akka.pattern.ask
import akka.util.Timeout
import com.ubirch.backend.storage.config.ServerConfig
import com.ubirch.backend.storage.model.HashValue
import com.ubirch.backend.util.JsonUtil

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * Created by derMicha on 31/07/16.
  */
class HasherService extends Actor with ActorLogging {

  import context.dispatcher

  implicit val timeout: Timeout = 2 seconds

  val hasherActor = context.actorOf(Props[Hasher], name = ServerConfig.hasherActor)
  val pusherActor = context.actorOf(Props[Pusher], name = ServerConfig.pusherActor)

  def receive = LoggingReceive {
    case hv: AnyRef =>
      (hasherActor ? hv) onComplete {
        case Success(resp) =>
          resp match {
            case hash: HashValue =>
              JsonUtil.any2jvalue(hash) match {
                case Some(jval) =>
                  pusherActor ! jval
                case None =>
                  log.error("input message could not be marshalled to json")
              }

            case _ =>
              log.error("input message type, have to be HashValue")
          }
        case Failure(e) =>
          log.error("input message could be hashed")
      }
    case _ =>
      log.error("unknown message")
  }

}
