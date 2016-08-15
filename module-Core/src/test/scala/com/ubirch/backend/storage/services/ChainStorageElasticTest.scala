package com.ubirch.backend.storage.services

import java.net.URL

import com.typesafe.scalalogging.LazyLogging
import com.ubirch.backend.util.UUIDUtil
import com.roundeights.hasher.Implicits._
import com.ubirch.backend.chain.model.{BlockInfo, GenesisBlock, Hash}
import org.scalatest.{BeforeAndAfterAll, FeatureSpec, Matchers}

import uk.co.bigbeeconsultants.http.HttpClient

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by derMicha on 05/08/16.
  */
class ChainStorageElasticTest extends FeatureSpec
  with Matchers
  with BeforeAndAfterAll
  with LazyLogging {

  // if cleanUp == true all tests delete their stuff
  val cleanUp = false
  val httpClient = new HttpClient

  val hashValue = UUIDUtil.uuidStr.sha256.hex

  val hashValues = List(UUIDUtil.uuidStr.sha256.hex, UUIDUtil.uuidStr.sha256.hex, UUIDUtil.uuidStr.sha256.hex)

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

    scenario("delete hashes") {

      hashValues.foreach { hash =>
        val hv = Await.result(ChainStorageElastic.getHash(hash), 10 seconds)
        hv.isDefined shouldBe true
        hv.get shouldBe hash
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
      unminedHashes.hashes.head shouldBe hashValue
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

    scenario("get a BlockInfo") {

      val res = Await.result(ChainStorageElastic.getBlockInfo(blockHash = blockHash), 10 seconds)
      res.isDefined shouldBe true
      res.get.hash shouldBe blockHash.hash
    }

    ignore("get BlockInfo by hash") {

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
  }

  override protected def beforeAll(): Unit = {
    logger.info("start clean up before")
    httpClient.delete(new URL(s"${HashStore.baseUrl}/${HashStore.index}"))
    httpClient.delete(new URL(s"${BlockStore.baseUrl}/${BlockStore.index}"))
    httpClient.delete(new URL(s"${GenesisBlockStore.baseUrl}/${GenesisBlockStore.index}"))

    Thread.sleep(500)

    hashValues.foreach { hv =>
      Await.result(ChainStorageElastic.storeHash(hv), 10 seconds)
    }

  }

  override protected def afterAll(): Unit = {
    if (cleanUp) {
      logger.info("start clean up after")
      httpClient.delete(new URL(s"${HashStore.baseUrl}/${HashStore.index}"))
      httpClient.delete(new URL(s"${BlockStore.baseUrl}/${BlockStore.index}"))
      httpClient.delete(new URL(s"${GenesisBlockStore.baseUrl}/${GenesisBlockStore.index}"))
    }
  }
}
