package com.ubirch.backend.storage.server.model

/**
  * author: cvandrei
  * since: 2016-07-27
  */

//TODO version should be sbt version
case class Welcome(version: String = "1.0", status: String = "OK", message: String)
