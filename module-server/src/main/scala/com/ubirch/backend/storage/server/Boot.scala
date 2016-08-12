package com.ubirch.backend.storage.server

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import com.ubirch.backend.storage.config.ServerConfig
import com.ubirch.backend.storage.server.route.MainRoute
import com.ubirch.backend.storage.server.services.PullerService

import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2016-07-26
  */
object Boot extends App with LazyLogging {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  logger.info("ubirchStorageService started")
  PullerService.runClient()

  val bindingFuture = start()

  Runtime.getRuntime.addShutdownHook(new Thread() {
    override def run() = {
      bindingFuture
        .flatMap(_.unbind())
        .onComplete(_ => system.terminate())
    }
  })

  def start(): Future[ServerBinding] = {

    val interface = ServerConfig.interface
    val port = ServerConfig.port
    implicit val timeout = Timeout(5, TimeUnit.SECONDS)

    Http().bindAndHandle((new MainRoute).myRoute, interface, port)

  }

}
