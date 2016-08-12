package com.ubirch.backend.storage.util

import com.ubirch.backend.storage.model.ToHash
import com.ubirch.backend.util.JsonUtil._
import org.json4s._

/**
  * Created by derMicha on 30/07/16.
  */

object JsonUtil {

  implicit val formats = DefaultFormats.lossless ++ org.json4s.ext.JodaTimeSerializers.all

  def string2ToHash(value: String): Option[ToHash] = {
    string2JValue(value) match {
      case Some(jval) =>
        jval.extractOpt[ToHash]
      case None =>
        None
    }
  }
}
