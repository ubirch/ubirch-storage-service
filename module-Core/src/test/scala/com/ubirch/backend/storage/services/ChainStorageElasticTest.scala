package com.ubirch.backend.storage.services

import com.typesafe.scalalogging.LazyLogging
import com.ubirch.backend.util.UUIDUtil
import com.roundeights.hasher.Implicits._
import com.ubirch.backend.chain.model.{BlockInfo, GenesisBlock, Hash}
import org.joda.time.DateTime
import org.scalatest.{FeatureSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by derMicha on 05/08/16.
  */
class ChainStorageElasticTest extends FeatureSpec
  with Matchers
  with LazyLogging {

  // if cleanUp == true all tests delete their stuff
  val cleanUp = false

  val hashValue = UUIDUtil.uuidStr.sha256.hex

  val blockHash: Hash = Hash(hash = UUIDUtil.uuidStr.sha256.hex)

  val genesisBlockHash = UUIDUtil.uuidStr.sha256.hex

  feature("ChainStoreES") {

    scenario("store a hash") {
      val res = Await.result(ChainStorageElastic.storeHash(hashValue), 10 seconds)
      res.isDefined shouldBe true
      res.get shouldBe hashValue
    }

    scenario("fetch a hash") {
      val fetchedHash = Await.result(ChainStorageElastic.getHash(hashValue), 10 seconds)
      fetchedHash.isDefined shouldBe true
      fetchedHash.get shouldBe hashValue
    }

    scenario("delete a hash") {
      val res = Await.result(ChainStorageElastic.deleteHash(hashValue), 10 seconds)
      res shouldBe true

      val fetchedHash = Await.result(ChainStorageElastic.getHash(hashValue), 10 seconds)
      fetchedHash.isDefined shouldBe false
    }

    scenario("store a BlockInfo") {
      val blockInfo = BlockInfo(
        hash = blockHash.hash,
        previousBlockHash = UUIDUtil.uuidStr.sha256.hex
      )

      val res = Await.result(ChainStorageElastic.upsertBlock(block = blockInfo), 10 seconds)
      res.isDefined shouldBe true
      res.get.hash shouldBe blockInfo.hash
    }

    scenario("load a BlockInfo") {

      val res = Await.result(ChainStorageElastic.getBlockInfo(blockHash = blockHash), 10 seconds)
      res.isDefined shouldBe true
      res.get.hash shouldBe blockHash.hash

      if (cleanUp)
        BlockStore.delete(blockHash.hash)
    }

    scenario("store a GenesisBlock") {
      val genesisBlock = GenesisBlock(
        hash = genesisBlockHash
      )

      val res = Await.result(ChainStorageElastic.saveGenesisBlock(genesis = genesisBlock), 10 seconds)
      res.isDefined shouldBe true
      res.get.hash shouldBe genesisBlock.hash
    }

    scenario("load a GenesisBlock") {
      val res = Await.result(ChainStorageElastic.getGenesisBlock, 10 seconds)
      res.isDefined shouldBe true
      res.get.hash shouldBe genesisBlockHash

      if (cleanUp)
        GenesisBlockStore.delete(genesisBlockHash)
    }
  }
}
