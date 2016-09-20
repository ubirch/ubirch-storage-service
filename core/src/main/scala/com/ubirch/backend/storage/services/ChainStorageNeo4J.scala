package com.ubirch.backend.storage.services

import com.typesafe.scalalogging.LazyLogging
import com.ubirch.backend.chain.model.{HashedData, BlockInfo}

import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2016-09-14
  */
object ChainStorageNeo4J extends MinerStorage with LazyLogging {

  /**
    * Add a hash and assign it to the [[unminedBlock()]].
    *
    * @param hash the hash to store
    * @return the stored hash; None if something went wrong
    */
  override def storeHash(hash: HashedData): Future[Option[HashedData]] = ??? // TODO implement

  /**
    * Assign hashes to another block.
    *
    * @param hashes hashes to reassign
    * @param newBlockNumber number block to assign hashes to
    * @return sequence of reassigned hashes
    */

  override def reassignHashes(hashes: Seq[HashedData], newBlockNumber: Long): Seq[HashedData] = ??? // TODO implement

  /**
    * Load all hashes for a given block number.
    *
    * @param blockNumber load hashes for this block number
    * @return empty if no hashes exist; not empty otherwise
    */
  override def getHashes(blockNumber: Long): Future[Seq[HashedData]] = ??? // TODO implement

  /**
    * Insert a block. The code containing the business logic may ensure that there's exactly one at any given time.
    *
    * @return info of the inserted block; None if something went wrong
    */
  override def insertBlock(block: BlockInfo): Future[Option[BlockInfo]] = ??? // TODO implement

  /**
    * Load a [[BlockInfo]] based on the block's number.
    *
    * @param blockNumber number of block to load
    * @return None if block does not exist; Some otherwise
    */
  override def getBlock(blockNumber: Long): Future[Option[BlockInfo]] = ??? // TODO implement

  /**
    * Update an existing block.
    *
    * @return info of the updated blokck; None if something went wrong
    */
  override def updateBlock(block: BlockInfo): Future[BlockInfo] = ??? // TODO implement

  /**
    * Gives us the unmined block (has no block hash yet). At any given time exactly one unmined block may exist.
    *
    * @return the unmined block
    */
  override def unminedBlock(): Future[BlockInfo] = ??? // TODO implement

}
