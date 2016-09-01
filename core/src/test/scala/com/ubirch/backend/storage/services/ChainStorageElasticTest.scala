package com.ubirch.backend.storage.services

import com.roundeights.hasher.Implicits._
import com.typesafe.scalalogging.LazyLogging
import com.ubirch.backend.chain.model.{BlockInfo, FullBlock, GenesisBlock, HashedData}
import com.ubirch.backend.storage.StorageCleanUp
import com.ubirch.util.uuid.UUIDUtil
import org.joda.time.DateTime
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

  val blockHash: HashedData = HashedData(hash = UUIDUtil.uuidStr.sha256.hex)

  val genesisBlockHash = UUIDUtil.uuidStr.sha256.hex

  val blockInfo = BlockInfo(
    hash = blockHash.hash,
    previousBlockHash = genesisBlockHash,
    number = 2342L
  )

  val fullBlockHashes = Seq(UUIDUtil.uuidStr.sha256.hex, UUIDUtil.uuidStr.sha256.hex, UUIDUtil.uuidStr.sha256.hex)

  val fullBlockInfo = FullBlock(
    hash = blockHash.hash,
    previousBlockHash = genesisBlockHash,
    created = DateTime.now,
    number = 2342L,
    version = "1.0",
    hashes = Some(fullBlockHashes)
  )

  feature("ChainStoreES") {

    scenario("store a hash") {
      val res = Await.result(ChainStorageElastic.storeHash(hashValue), 10 seconds)
      res.isDefined shouldBe true
      res.get.hash shouldBe hashValue.hash
    }

    scenario("fetch a hash") {
      val fetchedHash = Await.result(ChainStorageElastic.getHash(hashValue.hash), 10 seconds)
      fetchedHash.isDefined shouldBe true
      fetchedHash.get.hash shouldBe hashValue.hash
    }

    scenario("delete hashes") {

      hashValues.foreach { hash =>
        val hv = Await.result(ChainStorageElastic.getHash(hash), 10 seconds)
        hv.isDefined shouldBe true
        hv.get.hash shouldBe hash
      }

      ChainStorageElastic.deleteHashes(hashValues.toSet)

      Thread.sleep(500)

      hashValues.foreach { hash =>
        val hv = Await.result(ChainStorageElastic.getHash(hash), 10 seconds)
        hv.isDefined shouldBe false
      }
    }

    scenario("fetch unminded hashes") {
      Thread.sleep(500)
      val unminedHashes = Await.result(ChainStorageElastic.unminedHashes(), 10 seconds)
      unminedHashes.hashes.nonEmpty shouldBe true
      unminedHashes.hashes.head shouldBe hashValue.hash
    }

    scenario("delete a hash") {
      val res = Await.result(ChainStorageElastic.deleteHash(hashValue.hash), 10 seconds)
      res shouldBe true

      val fetchedHash = Await.result(ChainStorageElastic.getHash(hashValue.hash), 10 seconds)
      fetchedHash.isDefined shouldBe false
    }

    scenario("store a BlockInfo") {

      val res = Await.result(ChainStorageElastic.upsertBlock(block = blockInfo), 10 seconds)
      res.isDefined shouldBe true
      res.get.hash shouldBe blockInfo.hash
    }

    scenario("get a BlockInfo") {

      val res = Await.result(ChainStorageElastic.getBlockInfo(blockHash = blockHash), 10 seconds)
      res.isDefined shouldBe true
      res.get.hash shouldBe blockHash.hash
    }

    ignore("get BlockInfo by hash") {

      //      val res = Await.result(ChainStorageElastic.getBlockByEventHash(), 10 seconds)
      //      res.isDefined shouldBe true
      //      res.get.hash shouldBe blockHash.hash

    }

    scenario("get a FullBlock") {

      val res = Await.result(ChainStorageElastic.getFullBlock(blockHash = blockHash.hash), 10 seconds)
      res.isDefined shouldBe true
      res.get.hash shouldBe blockHash.hash

    }
    scenario("load most recent BlockInfo") {

      Thread.sleep(1000)

      val res = Await.result(ChainStorageElastic.mostRecentBlock(), 10 seconds)
      res.isDefined shouldBe true
      res.get.hash shouldBe blockHash.hash
    }

    scenario("save a GenesisBlock") {
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
    }

    scenario("get a BlockInfo based on previousBlockHash (block does not exist though)") {

      val res = Await.result(ChainStorageElastic.getBlockInfoByPreviousBlockHash(blockHash = blockHash), 10 seconds)
      res shouldBe None
    }

    scenario("get a BlockInfo based on previousBlockHash (block exists)") {

      // TODO fix test
      val res = Await.result(ChainStorageElastic.getBlockInfoByPreviousBlockHash(blockHash = HashedData(genesisBlockHash)), 20 seconds)
      res.isDefined shouldBe true
      res.get.hash shouldEqual blockHash.hash
      res.get.previousBlockHash shouldEqual genesisBlockHash
    }

  }

  override protected def beforeAll(): Unit = {
    logger.info("start clean up before")
    resetStorage()

    Thread.sleep(500)

    hashValues.foreach { hv =>
      val hash = HashedData(hv)
      Await.result(ChainStorageElastic.storeHash(hash), 10 seconds)
    }

  }

  override protected def afterAll(): Unit = {
    if (cleanUp) {
      logger.info("start clean up after")
      resetStorage()
    }
  }
}
