package com.ubirch.backend.storage.services

import com.typesafe.scalalogging.LazyLogging
import com.ubirch.backend.chain.model._

import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2016-08-01
  */
trait ExplorerStorage extends LazyLogging {

  /**
    *
    * @param fullBlock a block with event hashes
    * @return
    */
  def upsertFullBlock(fullBlock: FullBlock): Future[Option[FullBlock]]

  /**
    * Gives us the block that the input hash is included in.
    *
    * @param eventHash hash based on which we look for the related block
    * @return block matching the input hash
    */
  def getBlockByEventHash(eventHash: HashedData): Future[Option[BlockInfo]]

  /**
    * Gives us basic information about a block (without all it's hashes).
    *
    * @param blockHash hash of the requested block
    * @return block matching the input hash
    */
  def getBlockInfo(blockHash: HashedData): Future[Option[BlockInfo]]

  /**
    * Gives us a block including all it's hashes.
    *
    * @param hash hash of the requested block
    * @return block matching the input hash
    */
  def getFullBlock(hash: String): Future[Option[FullBlock]]

  /**
    * Gives us basic information about a block (without all it's hashes) based on the blockHash of it's predecessor.
    *
    * @param blockHash blockHash predecessor block
    * @return block whose predecessor has the specified blockHash
    */
  def getNextBlockInfo(blockHash: HashedData): Future[Option[BlockInfo]]

}
