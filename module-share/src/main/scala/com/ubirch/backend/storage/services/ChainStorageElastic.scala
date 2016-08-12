package com.ubirch.backend.storage.services

import java.net.URI

import com.typesafe.scalalogging.LazyLogging
import com.ubirch.backend.chain.json._
import com.ubirch.backend.storage.services.elasticsearch.KeyValueStorage
import com.ubirch.backend.storage.services.elasticsearch.components.ElasticSearchKeyValueStorage
import com.ubirch.backend.storage.config.ServerConfig

import com.ubirch.backend.util.JsonUtil

import org.json4s.{DefaultFormats, JValue}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by derMicha on 05/08/16.
  */
object ChainStorageElastic extends ChainStorage with LazyLogging {

  implicit val formats = DefaultFormats.lossless ++ org.json4s.ext.JodaTimeSerializers.all

  /**
    * Adds a hash to the list of unmined hashes.
    *
    * @param hash the hash to store
    */
  override def storeHash(hash: String): Future[Option[String]] = {
    JsonUtil.any2jvalue(Hash(hash = hash)) match {
      case Some(hashJval) =>
        HashStore.store(hash, hashJval).map { r =>
          Some(hash)
        }
      case None =>
        logger.error(s"got invalid hash value: $hash")
        Future(None)
    }
  }

  def getHash(hash: String): Future[Option[String]] = {
    HashStore.fetch(hash).map {
      case Some(hJval) =>
        hJval.extractOpt[Hash] match {
          case Some(hObj) =>
            Some(hObj.hash)
          case None =>
            None
        }
      case _ => None
    }
  }

  override def deleteHash(hash: String): Future[Boolean] = {
    HashStore.delete(hash)
  }

  override def deleteHashes(hashes: Set[String]): Future[Boolean] = {
    hashes.foreach(deleteHash(_))
    //@TODO fix it
    Future(true)
  }

  /**
    * Gives us a list of hashes that haven't been mined yet.
    *
    * @return list of unmined hashes
    */
  override def unminedHashes(): UnminedHashes = ???

  /**
    * Gives us the block that the input hash is included in.
    *
    * @param eventHash hash based on which we look for the related block
    * @return block matching the input hash
    */
  override def getBlockByEventHash(eventHash: Hash): Future[Option[BlockInfo]] = ???

  /**
    * Gives us basic information about a block (without all it's hashes).
    *
    * @param blockHash hash of the requested block
    * @return block matching the input hash
    */
  override def getBlockInfo(blockHash: Hash): Future[Option[BlockInfo]] = {
    BlockStore.fetch(blockHash.hash).map {
      case Some(bJval) =>
        bJval.extractOpt[BlockInfo]
      case _ => None
    }
  }

  /**
    * Gives us a block including all it's hashes.
    *
    * @param blockHash hash of the requested block
    * @return block matching the input hash
    */
  override def getFullBlock(blockHash: String): Future[Option[FullBlock]] = {
    BlockStore.fetch(blockHash).map {
      case Some(bJval) =>
        bJval.extractOpt[FullBlock]
      case _ => None
    }
  }

  override def mostRecentBlock(): BlockInfo = ???

  override def saveGenesisBlock(genesis: GenesisBlock): Future[Option[GenesisBlock]] = {
    JsonUtil.any2jvalue(genesis) match {
      case Some(genesisJval) =>
        GenesisBlockStore.store("1", genesisJval).map {
          case Some(bi) =>
            //@TODO add deeper result check
            Some(genesis)
          case None =>
            None
        }
      case None =>
        logger.error(s"got invalid GenesisBlock: $genesis")
        Future(None)
    }
  }

  /**
    * Saves or updates a block.
    *
    * @param block block info to store
    */
  override def upsertBlock(block: BlockInfo): Future[Option[BlockInfo]] =
    JsonUtil.any2jvalue(block) match {
      case Some(jval) =>
        BlockStore.store(block.hash, jval).map {
          case Some(bi) =>
            //@TODO add deeper result check
            Some(block)
          case None =>
            None
        }
      case None =>
        logger.error(s"could not store BlockInfo: $block")
        Future(None)
    }

  /**
    * @return the genesis block; None if none exists
    */
  override def getGenesisBlock: Future[Option[GenesisBlock]] =
    GenesisBlockStore.fetch("1") map {
      case Some(bJval) =>
        bJval.extractOpt[GenesisBlock]
      case None =>
        None
    }

}

object HashStore extends KeyValueStorage[JValue] with ElasticSearchKeyValueStorage with LazyLogging {

  override val baseUrl = ServerConfig.esUrl

  override val index = ServerConfig.esChainHashIndex

  override val datatype: String = ServerConfig.esChainHashType

  logger.debug(s"current uri: $uri")
}

object BlockStore extends KeyValueStorage[JValue] with ElasticSearchKeyValueStorage with LazyLogging {

  override val baseUrl = ServerConfig.esUrl

  override val index = ServerConfig.esChainBlockIndex

  override val datatype: String = ServerConfig.esChainBlockType

  logger.debug(s"current uri: $uri")
}

object GenesisBlockStore extends KeyValueStorage[JValue] with ElasticSearchKeyValueStorage with LazyLogging {

  override val baseUrl = ServerConfig.esUrl

  override val index = ServerConfig.esChainGenesisBlockIndex

  override val datatype: String = ServerConfig.esChainGenesisBlockType

  logger.debug(s"current uri: $uri")
}
