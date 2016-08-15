package com.ubirch.backend.util

import org.json4s._
import org.json4s.ext.{JavaTypesSerializers, JodaTimeSerializers}

/**
  * author: cvandrei
  * since: 2016-07-27
  */
trait MyJsonProtocol {

  implicit val serialization = jackson.Serialization // or native.Serialization

  implicit def json4sJacksonFormats: Formats = DefaultFormats ++ JavaTypesSerializers.all ++ JodaTimeSerializers.all

}
