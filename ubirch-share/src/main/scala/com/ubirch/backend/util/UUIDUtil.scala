package com.ubirch.backend.util

/**
  * Created by derMicha on 01/08/16.
  */
object UUIDUtil {

  def uuid = java.util.UUID.randomUUID()

  def uuidStr = uuid.toString

}
