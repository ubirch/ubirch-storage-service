package com.ubirch.backend.storage.services

import com.typesafe.scalalogging.LazyLogging
import com.ubirch.backend.chain.model.{BlockInfo, HashedData}

import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2016-09-14
  */
trait MinerStorage extends LazyLogging {

  /**
    * Add a hash and assign it to the [[unminedBlock()]].
    *
    * @param hash the hash to store
    */
  def storeHash(hash: HashedData): Future[Option[HashedData]]

  def reassignHashes(hashes: Seq[HashedData], newBlockNumber: Long): Seq[HashedData]

  /**
    * Load all hashes for a given block number.
    *
    * @param blockNumber load hashes for this block number
    * @return empty sequence if no hashes exist; otherwise sequence is not empty
    */
  def getHashes(blockNumber: Long): Future[Seq[HashedData]]

  /**
    * Insert an unmined block. The code containing the business logic may ensure that there's exactly one at any given
    * time.
    *
    * @return info of the inserted block
    */
  def insertUnminedBlock(block: BlockInfo): Future[BlockInfo]

  /**
    * Update an existing block.
    *
    * @return
    */
  def updateBlock(block: BlockInfo): Future[BlockInfo]

  /**
    * Gives us the unmined block (has no block hash yet). At any given time exactly one unmined block exists.
    *
    * @return the unmined block
    */
  def unminedBlock(): Future[BlockInfo]

}
