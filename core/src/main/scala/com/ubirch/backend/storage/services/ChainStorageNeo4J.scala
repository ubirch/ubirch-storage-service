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
    * Adds a hash to the list of unmined hashes.
    *
    * @param hash the hash to store
    */
  override def storeHash(hash: HashedData): Future[Option[HashedData]] = ??? // TODO implement

  /**
    * Insert an unmined block.
    *
    * @return BlockInfo of the inserted block
    */
  override def insertUnminedBlock(): Future[BlockInfo] = ??? // TODO implement

  /**
    * There's always exactly one unmined block which through mining becomes the newest block in the chain.
    *
    * @return the unmined block
    */
  override def unminedBlock(): Future[BlockInfo] = ??? // TODO implement

}
