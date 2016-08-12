package com.ubirch.backend.storage.server.route

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import com.ubirch.backend.storage.actors._
import com.ubirch.backend.storage.config.{ServerConst, ServerConfig}
import com.ubirch.backend.storage.model.{Error, HashValue, ToHash}
import com.ubirch.backend.util.MyJsonProtocol
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * author: dermicha
  * since: 2016-07-29
  */

object HashRoute extends MyJsonProtocol with LazyLogging {

  implicit val system = ActorSystem()
  implicit val timeout: Timeout = 2 seconds

  //  val hasherActor = system.actorSelection(s"/user/${Config.hasherActor}")
  val hasherActor = system.actorOf(Props[Hasher], name = ServerConfig.hasherActor)
  logger.info(hasherActor.path.toString)

  val route: Route = {

    path("hash" / Segment / Segment) { (alg, input) =>
      get {
        onComplete {
          alg.toLowerCase match {
            case ServerConst.HASH_MD5 =>
              val th = ToHash(input, ServerConst.HASH_MD5)
              logger.debug(s"toHash: $th")
              hasherActor ? th
            case ServerConst.HASH_SHA256 =>
              val th = ToHash(input, ServerConst.HASH_SHA256)
              logger.debug(s"toHash: $th")
              hasherActor ? th
            case _ =>
              val th = ToHash(input, ServerConst.HASH_SHA512)
              logger.debug(s"toHash: $th")
              hasherActor ? th
          }
        } {
          case Success(resp) =>
            resp match {
              case hash: HashValue => complete(hash)
              case _ => complete(Error(1, "tot!"))
            }
          case Failure(e) => complete(Error(2, "tot!"))
        }
      }
    }
  }
}
