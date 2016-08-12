package com.ubirch.backend.storage.server.route

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.ubirch.backend.storage.server.model.Welcome
import com.ubirch.backend.util.MyJsonProtocol
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

/**
  * author: dermicha
  * since: 2016-07-29
  */

object WelcomeRoute extends MyJsonProtocol {

  val route: Route = {
    complete {
      Welcome(message = "Welcome to the ubirchStorageServer")
    }
  }
}
