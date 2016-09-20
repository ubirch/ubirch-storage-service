package com.ubirch.client.storage

import com.typesafe.scalalogging.LazyLogging
import com.ubirch.backend.chain.model._
import com.ubirch.backend.storage.services.{ChainStorageElastic, ChainStorageNeo4J}

import scala.concurrent.Future

/**
  * Created by derMicha on 13/08/16.
  */
object ChainStorageServiceClient extends LazyLogging {

  /*
   * MinerStorage methods
   **********************************/

  /**
    * Remember a hash to have it mined into a future block.
    *
    * @param hash the hash to store
    * @return the stored hash; None if something went wrong
    */
  def minerStoreHash(hash: HashedData): Future[Option[HashedData]] = ChainStorageNeo4J.storeHash(hash)

  /**
    * Assign hashes to another block.
    *
    * @param hashes hashes to reassign
    * @param newBlockNumber number block to assign hashes to
    * @return sequence of reassigned hashes
    */

  def minerReassignHashes(hashes: Seq[HashedData], newBlockNumber: Long): Seq[HashedData] = ChainStorageNeo4J.reassignHashes(hashes, newBlockNumber)

  /**
    * Load all hashes for a given block number.
    *
    * @param blockNumber load hashes for this block number
    * @return empty if no hashes exist; not empty otherwise
    */
  def minerGetHashes(blockNumber: Long): Future[Seq[HashedData]] = ChainStorageNeo4J.getHashes(blockNumber)

  /**
    * Insert a block. The code containing the business logic may ensure that there's exactly one at any given time.
    *
    * @return info of the inserted block; None if something went wrong
    */
  def minerInsertBlock(block: BlockInfo): Future[Option[BlockInfo]] = ChainStorageNeo4J.insertBlock(block)

  /**
    * Load a [[BlockInfo]] based on the block's number.
    *
    * @param blockNumber number of block to load
    * @return None if block does not exist; Some otherwise
    */
  def minerGetBlock(blockNumber: Long): Future[Option[BlockInfo]] = ChainStorageNeo4J.getBlock(blockNumber)

  /**
    * Update an existing block.
    *
    * @return info of the updated blokck; None if something went wrong
    */
  def minerUpdateBlock(block: BlockInfo): Future[BlockInfo] = ChainStorageNeo4J.updateBlock(block)

  /**
    * Gives us the unmined block (has no block hash yet). At any given time exactly one unmined block may exist.
    *
    * @return the unmined block
    */
  def minerUnminedBlock(): Future[BlockInfo] = ChainStorageNeo4J.unminedBlock()

  /*
   * ExplorerStorage methods
   **********************************/

  /**
    * Saves or updates a block.
    *
    * @param block block info to store
    */
  def explorerUpsertFullBlock(block: FullBlock): Future[Option[FullBlock]] = ChainStorageElastic.upsertFullBlock(block)

  /**
    * Gives us the block that the input hash is included in.
    *
    * @param eventHash hash based on which we look for the related block
    * @return block matching the input hash
    */
  def explorerGetBlockByEventHash(eventHash: HashedData): Future[Option[BlockInfo]] = ChainStorageElastic.getBlockByEventHash(eventHash = eventHash)

  /**
    * Gives us basic information about a block (without all it's hashes).
    *
    * @param blockHash hash of the requested block
    * @return block matching the input hash
    */
  def explorerGetBlockInfo(blockHash: HashedData): Future[Option[BlockInfo]] = ChainStorageElastic.getBlockInfo(blockHash)

  /**
    * Gives us a block including all it's hashes.
    *
    * @param blockHash hash of the requested block
    * @return block matching the input hash
    */
  def explorerGetFullBlock(blockHash: String): Future[Option[FullBlock]] = ChainStorageElastic.getFullBlock(blockHash = blockHash)

  /**
    * Gives us basic information about a block (without all it's hashes) based on the blockHash of it's predecessor.
    *
    * @param blockHash blockHash predecessor block
    * @return block whose predecessor has the specified blockHash
    */
  def explorerGetNextBlockInfo(blockHash: HashedData): Future[Option[BlockInfo]] = ChainStorageElastic.getNextBlockInfo(blockHash)

}
