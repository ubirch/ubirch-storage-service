package com.ubirch.backend.storage.config

/**
  * author: cvandrei
  * since: 2016-07-26
  */
object ServerConst {

  final val INTERFACE = "ubirchStorageService.interface"
  final val PORT = "ubirchStorageService.port"

  final val HASH_ALGORITHM = "ubirchStorageService.hash.algorithm"

  final val HASH_SHA256 = "sha256"

  final val HASH_SHA512 = "sha512"

  final val HASH_MD5 = "md5"

  final val ESURL = "ubirchStorageService.elasticsearch.url"

  final val ESCHAINHASHINDEX = "ubirchStorageService.elasticsearch.hashchainstore.index"

  final val ESCHAINHASHTYPE = "ubirchStorageService.elasticsearch.hashchainstore.type"

  final val ESCHAINBLOCKINDEX = "ubirchStorageService.elasticsearch.blockchainstore.index"

  final val ESCHAINBLOCKTYPE = "ubirchStorageService.elasticsearch.blockchainstore.type"

  final val ESCHAINGENESISBLOCKINDEX = "ubirchStorageService.elasticsearch.genesisblockchainstore.index"

  final val ESCHAINGENESISBLOCKTYPE = "ubirchStorageService.elasticsearch.genesisblockchainstore.type"

  final val ES_ALL_INDEXES: Set[String] = Set(ESCHAINBLOCKINDEX, ESCHAINGENESISBLOCKINDEX, ESCHAINHASHINDEX)

}
