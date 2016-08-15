package com.ubirch.client.storage

import com.typesafe.scalalogging.LazyLogging
import com.ubirch.backend.chain.model._
import com.ubirch.backend.storage.services.{ChainStorage, ChainStorageElastic}

import scala.concurrent.Future

/**
  * Created by derMicha on 13/08/16.
  */
object ChainStorageServiceClient extends ChainStorage with LazyLogging {
  /**
    * Adds a hash to the list of unmined hashes.
    *
    * @param hash the hash to store
    */
  override def storeHash(hash: String): Future[Option[String]] = ChainStorageElastic.storeHash(hash)


  override def mostRecentBlock(): Future[Option[BlockInfo]] = ChainStorageElastic.mostRecentBlock

  /**
    * Gives us basic information about a block (without all it's hashes).
    *
    * @param blockHash hash of the requested block
    * @return block matching the input hash
    */
  override def getBlockInfo(blockHash: Hash): Future[Option[BlockInfo]] = ChainStorageElastic.getBlockInfo(blockHash)

  /**
    * deletes a set of hash from the list of unmined hashes.
    *
    * @param hashes set of hashes to delete
    */
  override def deleteHashes(hashes: Set[String]): Future[Boolean] = ChainStorageElastic.deleteHashes(hashes)

  override def saveGenesisBlock(genesis: GenesisBlock): Future[Option[GenesisBlock]] = ChainStorageElastic.saveGenesisBlock(genesis = genesis)

  /**
    * Gives us a block including all it's hashes.
    *
    * @param blockHash hash of the requested block
    * @return block matching the input hash
    */
  override def getFullBlock(blockHash: String): Future[Option[FullBlock]] = ChainStorageElastic.getFullBlock(blockHash = blockHash)

  /**
    * Saves or updates a block.
    *
    * @param block block info to store
    */
  override def upsertBlock(block: BlockInfo): Future[Option[BlockInfo]] = ChainStorageElastic.upsertBlock(block = block)

  /**
    * Gives us a list of hashes that haven't been mined yet.
    *
    * @return list of unmined hashes
    */
  override def unminedHashes(): Future[UnminedHashes] = ChainStorageElastic.unminedHashes

  /**
    * @return the genesis block; None if none exists
    */
  override def getGenesisBlock: Future[Option[GenesisBlock]] = ChainStorageElastic.getGenesisBlock

  /**
    * deletes a hash from the list of unmined hashes.
    *
    * @param hash the hash to delete
    */
  override def deleteHash(hash: String): Future[Boolean] = ChainStorageElastic.deleteHash(hash = hash)

  /**
    * Gives us the block that the input hash is included in.
    *
    * @param eventHash hash based on which we look for the related block
    * @return block matching the input hash
    */
  override def getBlockByEventHash(eventHash: Hash): Future[Option[BlockInfo]] = ChainStorageElastic.getBlockByEventHash(eventHash = eventHash)
}