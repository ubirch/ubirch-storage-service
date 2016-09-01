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
object ChainStorageElastic extends ChainStorage with LazyLogging {

  implicit val formats = DefaultFormats.lossless ++ org.json4s.ext.JodaTimeSerializers.all

  /**
    * Adds a hash to the list of unmined hashes.
    *
    * @param hash the hash to store
    */
  override def storeHash(hash: HashedData): Future[Option[HashedData]] = {
    Json4sUtil.any2jvalue(hash) match {
      case Some(hashJval) =>
        HashStore.store(hash.hash, hashJval).map { r =>
          Some(hash)
        }
      case None =>
        logger.error(s"got invalid hash value: $hash")
        Future(None)
    }
  }

  def getHash(hash: String): Future[Option[HashedData]] = {
    HashStore.fetch(hash).map {
      case Some(hJval) =>
        hJval.extractOpt[com.ubirch.backend.chain.model.HashedData] match {
          case Some(hObj) =>
            Some(hObj)
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
    hashes.foreach(deleteHash)
    //@TODO fix it, return false if one the results are false
    Future(true)
  }

  /**
    * Gives us a list of hashes that haven't been mined yet.
    *
    * @return list of unmined hashes
    */
  override def unminedHashes(): Future[UnminedHashes] = {
    Thread.sleep(500)

    val limit = ServerConfig.esUnminedHashesLimit
    HashStore.fetchAll(limit = limit).map {
      case Some(jvals) =>
        UnminedHashes(jvals.map { jval =>
          jval.extractOpt[HashedData] match {
            case Some(hash) =>
              Some(hash.hash)
            case None =>
              None
          }
        }.filter(_.isDefined).map(_.get))
      case None =>
        UnminedHashes(Seq.empty)
    }
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
  override def getBlockInfoByPreviousBlockHash(blockHash: HashedData): Future[Option[BlockInfo]] = {

    // TODO do not ignore genesis block in search
    BlockStore.fetchAll(filter = Some(s"previousBlockHash:${blockHash.hash}")) map {

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

  override def mostRecentBlock(): Future[Option[BlockInfo]] = {
    // TODO do not ignore genesis block in search
    BlockStore.fetchAll(ordered = Some("created"), order = "desc").map {
      case Some(jvals: List[JValue]) =>
        jvals.nonEmpty match {
          case true => jvals.head.extractOpt[BlockInfo]
          case false => None
        }
      case None =>
        None
    }
  }

  override def saveGenesisBlock(genesis: GenesisBlock): Future[Option[GenesisBlock]] = {
    Json4sUtil.any2jvalue(genesis) match {
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
        logger.error(s"could not store BlockInfo: $block")
        Future(None)
    }

  /**
    * Saves or updates a block.
    *
    * @param fullBlock block info to store
    */
  override def upsertFullBlock(fullBlock: FullBlock): Future[Option[FullBlock]] =
    Json4sUtil.any2jvalue(fullBlock) match {
      case Some(jval) =>
        BlockStore.store(fullBlock.hash, jval).map {
          case Some(bi) =>
            //@TODO add deeper result check
            Some(fullBlock)
          case None =>
            None
        }
      case None =>
        logger.error(s"could not store BlockInfo: $fullBlock")
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
