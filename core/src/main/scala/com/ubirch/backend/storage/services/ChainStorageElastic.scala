package com.ubirch.backend.storage.services

import com.typesafe.scalalogging.LazyLogging
import com.ubirch.backend.chain.model._
import com.ubirch.backend.storage.config.ServerConfig
import com.ubirch.backend.storage.services.elasticsearch.KeyValueStorage
import com.ubirch.backend.storage.services.elasticsearch.components.ElasticSearchKeyValueStorage
import com.ubirch.util.json.Json4sUtil
import org.json4s.{DefaultFormats, JValue}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by derMicha on 05/08/16.
  */
object ChainStorageElastic extends ExplorerStorage with LazyLogging {

  implicit val formats = DefaultFormats.lossless ++ org.json4s.ext.JodaTimeSerializers.all

  /**
    * Saves or updates a block.
    *
    * @param block block info to store
    */
  override def upsertFullBlock(block: FullBlock): Future[Option[FullBlock]] =
    Json4sUtil.any2jvalue(block) match {
      case Some(jval) =>
        BlockStore.store(block.hash, jval).map {
          case Some(bi) =>
            //@TODO add deeper result check
            Some(block)
          case None =>
            None
        }
      case None =>
        logger.error(s"could not store FullBlock: $block")
        Future(None)
    }

  /**
    * Gives us the block that the input hash is included in.
    *
    * @param eventHash hash based on which we look for the related block
    * @return block matching the input hash
    */
  override def getBlockByEventHash(eventHash: HashedData): Future[Option[BlockInfo]] = {
    BlockStore.fetchAll(filter = Some(s"hashes:$eventHash")).map {
      case Some(jvals: List[JValue]) =>
        jvals.nonEmpty match {
          case true => jvals.head.extractOpt[BlockInfo]
          case false => None
        }
      case None =>
        None
    }
  }

  /**
    * Gives us basic information about a block (without all it's hashes).
    *
    * @param blockHash hash of the requested block
    * @return block matching the input hash
    */
  override def getBlockInfo(blockHash: HashedData): Future[Option[BlockInfo]] = {
    // TODO do not ignore genesis block in search
    BlockStore.fetch(blockHash.hash).map {
      case Some(bJval) =>
        bJval.extractOpt[BlockInfo]
      case _ => None
    }
  }

  /**
    * Gives us basic information about a block (without all it's hashes) based on the blockHash of it's predecessor.
    *
    * @param blockHash blockHash predecessor block
    * @return block whose predecessor has the specified blockHash
    */
  override def getNextBlockInfo(blockHash: HashedData): Future[Option[BlockInfo]] = {

    // TODO do not ignore genesis block in search
    val filter = Some(s"previousBlockHash:${blockHash.hash}")
    BlockStore.fetchAll(filter = filter) map {

      case Some(jvals: List[JValue]) =>
        jvals.nonEmpty match {
          case true => jvals.head.extractOpt[BlockInfo]
          case false => None
        }

      case None => None

    }

  }

  /**
    * Gives us a block including all it's hashes.
    *
    * @param blockHash hash of the requested block
    * @return block matching the input hash
    */
  override def getFullBlock(blockHash: String): Future[Option[FullBlock]] = {
    // TODO do not ignore genesis block in search
    BlockStore.fetch(blockHash).map {
      case Some(bJval) =>
        bJval.extractOpt[FullBlock]
      case _ => None
    }
  }

}

private object HashStore extends KeyValueStorage[JValue] with ElasticSearchKeyValueStorage with LazyLogging {

  override val baseUrl = ServerConfig.esUrl

  override val index = ServerConfig.esChainHashIndex

  override val datatype: String = ServerConfig.esChainHashType

  logger.debug(s"current uri: $uri")
}

private object BlockStore extends KeyValueStorage[JValue] with ElasticSearchKeyValueStorage with LazyLogging {

  override val baseUrl = ServerConfig.esUrl

  override val index = ServerConfig.esChainBlockIndex

  override val datatype: String = ServerConfig.esChainBlockType

  logger.debug(s"current uri: $uri")
}

private object GenesisBlockStore extends KeyValueStorage[JValue] with ElasticSearchKeyValueStorage with LazyLogging {

  override val baseUrl = ServerConfig.esUrl

  override val index = ServerConfig.esChainGenesisBlockIndex

  override val datatype: String = ServerConfig.esChainGenesisBlockType

  logger.debug(s"current uri: $uri")
}
