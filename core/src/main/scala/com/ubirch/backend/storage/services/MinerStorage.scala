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
    * Adds a hash to the list of unmined hashes.
    *
    * @param hash the hash to store
    */
  def storeHash(hash: HashedData): Future[Option[HashedData]]

  /**
    * Insert an unmined block.
    *
    * @return BlockInfo of the inserted block
    */
  def insertUnminedBlock(): Future[BlockInfo]

  /**
    * There's always exactly one unmined block which through mining becomes the newest block in the chain.
    *
    * @return the unmined block
    */
  def unminedBlock(): Future[BlockInfo]

}
