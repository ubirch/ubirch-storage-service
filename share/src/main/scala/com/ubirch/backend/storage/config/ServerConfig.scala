package com.ubirch.backend.storage.config

import com.typesafe.config.ConfigFactory

/**
  * author: cvandrei
  * since: 2016-07-27
  */
object ServerConfig {

  private lazy val conf = ConfigFactory.load

  val interface: String = conf.getString(ServerConst.INTERFACE)

  val port: Int = conf.getInt(ServerConst.PORT)

  val hashAlgorithm: String = conf.getString(ServerConst.HASH_ALGORITHM).toLowerCase

  val hasherActor = "hasher-actor"
  val hasherServiceActor = "hasher-service-actor"
  val pusherActor = "pusher-actor"

  val esUrl = conf.getString(ServerConst.ESURL)

  val esChainHashIndex = conf.getString(ServerConst.ESCHAINHASHINDEX)
  val esChainHashType = conf.getString(ServerConst.ESCHAINHASHTYPE)

  val esChainBlockIndex = conf.getString(ServerConst.ESCHAINBLOCKINDEX)
  val esChainBlockType = conf.getString(ServerConst.ESCHAINBLOCKTYPE)

  val esChainGenesisBlockIndex = conf.getString(ServerConst.ESCHAINGENESISBLOCKINDEX)
  val esChainGenesisBlockType = conf.getString(ServerConst.ESCHAINGENESISBLOCKTYPE)
}
