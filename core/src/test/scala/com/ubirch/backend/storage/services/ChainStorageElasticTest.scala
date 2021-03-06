package com.ubirch.backend.storage.services

import com.roundeights.hasher.Implicits._
import com.typesafe.scalalogging.LazyLogging
import com.ubirch.backend.chain.model.{BlockInfo, FullBlock, GenesisBlock, HashedData}
import com.ubirch.backend.storage.StorageCleanUp
import com.ubirch.util.date.DateUtil
import com.ubirch.util.uuid.UUIDUtil
import org.scalatest.{BeforeAndAfterAll, FeatureSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by derMicha on 05/08/16.
  */
class ChainStorageElasticTest extends FeatureSpec
  with Matchers
  with BeforeAndAfterAll
  with LazyLogging
  with StorageCleanUp {

  /* TODO refactor tests !!!
   * Currently they depend on the order of execution including changes made by previous test.
   * Instead each test may create data it needs. Database clean up can happen either before running a test or afterwards
   * for as long as the test is responsible of creating the consistent state it needs.
   */

  // if cleanUp == true all tests delete their stuff
  val cleanUp = false

  val hashValue = HashedData(UUIDUtil.uuidStr.sha256.hex)

  val hashValues = List(UUIDUtil.uuidStr.sha256.hex, UUIDUtil.uuidStr.sha256.hex, UUIDUtil.uuidStr.sha256.hex)

  val genesisBlockHash = UUIDUtil.uuidStr.sha256.hex

  val blockHash1: HashedData = HashedData(hash = UUIDUtil.uuidStr.sha256.hex)

  val blockInfo1 = BlockInfo(
    hash = blockHash1.hash,
    previousBlockHash = genesisBlockHash,
    number = 1L
  )

  val blockHash2: HashedData = HashedData(hash = UUIDUtil.uuidStr.sha256.hex)

  val blockInfo2 = BlockInfo(
    hash = blockHash2.hash,
    previousBlockHash = blockInfo1.hash,
    number = 2L
  )

  val fullBlockHashes1 = Seq(UUIDUtil.uuidStr.sha256.hex, UUIDUtil.uuidStr.sha256.hex, UUIDUtil.uuidStr.sha256.hex)

  val fullBlockInfo1 = FullBlock(
    hash = blockHash1.hash,
    previousBlockHash = genesisBlockHash,
    created = DateUtil.nowUTC,
    number = blockInfo1.number,
    version = "1.0",
    hashes = Some(fullBlockHashes1)
  )

  val fullBlockHashes2 = Seq(UUIDUtil.uuidStr.sha256.hex, UUIDUtil.uuidStr.sha256.hex, UUIDUtil.uuidStr.sha256.hex)

  val fullBlockInfo2 = FullBlock(
    hash = blockHash2.hash,
    previousBlockHash = blockInfo1.hash,
    created = DateUtil.nowUTC,
    number = blockInfo2.number,
    version = "1.0",
    hashes = Some(fullBlockHashes2)
  )

  feature("ChainStoreES") {

    // TODO move to ChainStorageNeo4JTest
//    scenario("store a hash") {
//      val res = Await.result(ChainStorageElastic.storeHash(hashValue), 10 seconds)
//      res shouldBe 'isDefined
//      res.get.hash shouldBe hashValue.hash
//    }

    scenario("fetch a hash") {
      val fetchedHash = Await.result(ChainStorageElastic.getHash(hashValue.hash), 10 seconds)
      fetchedHash shouldBe 'isDefined
      fetchedHash.get.hash shouldBe hashValue.hash
    }

    scenario("delete hashes") {

      hashValues.foreach { hash =>
        val hv = Await.result(ChainStorageElastic.getHash(hash), 10 seconds)
        hv shouldBe 'isDefined
        hv.get.hash shouldBe hash
      }

      ChainStorageElastic.deleteHashes(hashValues.toSet)

      Thread.sleep(500)

      hashValues.foreach { hash =>
        val hv = Await.result(ChainStorageElastic.getHash(hash), 10 seconds)
        hv shouldBe 'isEmpty
      }
    }

    scenario("fetch unminded hashes") {
      Thread.sleep(500)
      val unminedHashes = Await.result(ChainStorageElastic.unminedHashes(), 10 seconds)
      unminedHashes.hashes shouldBe 'nonEmpty
      unminedHashes.hashes.head shouldBe hashValue.hash
    }

    scenario("delete a hash") {
      val res = Await.result(ChainStorageElastic.deleteHash(hashValue.hash), 10 seconds)
      res shouldBe true

      val fetchedHash = Await.result(ChainStorageElastic.getHash(hashValue.hash), 10 seconds)
      fetchedHash shouldBe 'isEmpty
    }

    scenario("store a BlockInfo") {

      val res = Await.result(ChainStorageElastic.upsertBlock(block = blockInfo1), 10 seconds)
      res shouldBe 'isDefined
      res.get.hash shouldBe blockInfo1.hash
    }

    scenario("get a BlockInfo") {

      val res = Await.result(ChainStorageElastic.getBlockInfo(blockHash = blockHash1), 10 seconds)
      res shouldBe 'isDefined
      res.get.hash shouldBe blockHash1.hash
    }

    scenario("store a 2nd BlockInfo") {

      val res = Await.result(ChainStorageElastic.upsertBlock(block = blockInfo2), 10 seconds)
      res shouldBe 'isDefined
      res.get.hash shouldBe blockInfo2.hash
    }

    scenario("get a 2nd BlockInfo") {

      val res = Await.result(ChainStorageElastic.getBlockInfo(blockHash = blockHash2), 10 seconds)
      res shouldBe 'isDefined
      res.get.hash shouldBe blockHash2.hash
    }

    ignore("get BlockInfo by hash") {

      //      val res = Await.result(ChainStorageElastic.getBlockByEventHash(), 10 seconds)
      //      res shouldBe 'isDefined
      //      res.get.hash shouldBe blockHash.hash

    }

    scenario("get a FullBlock") {

      val res = Await.result(ChainStorageElastic.getFullBlock(blockHash = blockHash1.hash), 10 seconds)
      res shouldBe 'isDefined
      res.get.hash shouldBe blockHash1.hash

    }
    scenario("load most recent BlockInfo") {

      Thread.sleep(1000)

      val res = Await.result(ChainStorageElastic.mostRecentBlock(), 10 seconds)
      res shouldBe 'isDefined
      res.get.hash shouldBe blockHash2.hash
    }

    scenario("save a GenesisBlock") {
      val genesisBlock = GenesisBlock(
        hash = genesisBlockHash
      )

      val res = Await.result(ChainStorageElastic.saveGenesisBlock(genesis = genesisBlock), 10 seconds)
      res shouldBe 'isDefined
      res.get.hash shouldBe genesisBlock.hash
    }

    scenario("load a GenesisBlock") {
      val res = Await.result(ChainStorageElastic.getGenesisBlock, 10 seconds)
      res shouldBe 'isDefined
      res.get.hash shouldBe genesisBlockHash
    }

    scenario("get next BlockInfo (block does not exist though)") {

      val res = Await.result(ChainStorageElastic.getNextBlockInfo(blockHash = blockHash2), 10 seconds)
      res shouldBe None
    }

    scenario("get next BlockInfo (block exists)") {

      val res = Await.result(ChainStorageElastic.getNextBlockInfo(blockHash = HashedData(genesisBlockHash)), 20 seconds)
      res shouldBe 'isDefined
      res.get.hash shouldEqual blockHash1.hash
      res.get.previousBlockHash shouldEqual genesisBlockHash
    }

  }

  override protected def beforeAll(): Unit = {
    logger.info("start clean up before")
    resetStorage()

    Thread.sleep(500)

    hashValues.foreach { hv =>
      val hash = HashedData(hv)
      // TODO update test tooling
//      Await.result(ChainStorageElastic.storeHash(hash), 10 seconds)
    }

  }

  override protected def afterAll(): Unit = {
    if (cleanUp) {
      logger.info("start clean up after")
      resetStorage()
    }
  }
}
