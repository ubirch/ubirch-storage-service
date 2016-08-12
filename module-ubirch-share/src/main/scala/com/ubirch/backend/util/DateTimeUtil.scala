package com.ubirch.backend.util

import org.joda.time.{DateTime, DateTimeZone}

/**
  * Created by derMicha on 01/08/16.
  */
object DateTimeUtil {

  def dateTimeUTC = DateTime.now(DateTimeZone.UTC)

}
