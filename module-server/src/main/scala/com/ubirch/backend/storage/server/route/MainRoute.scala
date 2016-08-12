package com.ubirch.backend.storage.server.route

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

/**
  * author: dermicha
  * since: 2016-07-29
  */
class MainRoute {

  //  val hash = new HashRoute {}

  //  val chainExplorer = new ChainExplorerRoute {}

  val myRoute: Route = {

    pathPrefix("api") {
      HashRoute.route
    } ~
      pathSingleSlash {
        WelcomeRoute.route
      }
  }

}
