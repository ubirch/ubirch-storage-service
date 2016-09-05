/**
  *
  */
package com.ubirch.backend.chain.model

import com.ubirch.util.date.DateUtil
import org.joda.time.DateTime

/**
  * author: cvandrei
  * since: 2016-07-28
  */

/**
  * @param hash      hash value
  * @param blockHash hash of the block it is part of (None if not mined yet)
  */
case class HashInfo(hash: String,
                    blockHash: Option[String] = None
                   )

sealed trait BaseBlock {
  /** hash of the block **/
  def hash: String

  /** when the block was created **/
  def created: DateTime = DateUtil.nowUTC

  /** blocks are explicitly numbered **/
  def number: Long = 0L

  /** in which version of ubirchChainService was this block created **/
  def version: String = "1.0"
}

sealed trait PreviousBlockReference {
  /** hash of the previous block **/
  def previousBlockHash: String
}

sealed trait EventHashes {
  /** list of hashes included in the block (only set if you requested the full block) **/
  def hashes: Option[Seq[String]] // only set if you requested a full block
}

sealed trait AnchoredBlock {
  def anchors: Seq[Anchor]
}

/**
  *
  * @param hash    hash value of the GenesisBlock
  * @param created creation date
  * @param version version of the cas class
  */
case class GenesisBlock(
                         override val hash: String,
                         override val created: DateTime = DateUtil.nowUTC,
                         override val version: String = "1.0"
                       ) extends BaseBlock

/**
  * @param hash              hash of the block
  * @param previousBlockHash hash of the previous block
  * @param anchors           optional list of anchors to other chains
  */
case class BlockInfo(
                      override val hash: String,
                      override val previousBlockHash: String,
                      override val number: Long,
                      override val anchors: Seq[Anchor] = Seq.empty,
                      override val created: DateTime = DateUtil.nowUTC,
                      override val version: String = "1.0"
                    )
  extends BaseBlock with PreviousBlockReference with AnchoredBlock {
}

/**
  * @param hash              hash of the block
  * @param previousBlockHash hash of the previous block
  * @param anchors           optional list of anchors to other chains
  * @param hashes            list of hashes included in the block (only set if you requested the full block)
  */
case class FullBlock(
                      override val hash: String,
                      override val created: DateTime,
                      override val version: String,
                      override val previousBlockHash: String,
                      override val number: Long,
                      override val hashes: Option[Seq[String]],
                      override val anchors: Seq[Anchor] = Seq.empty
                    ) extends BaseBlock with PreviousBlockReference with EventHashes with AnchoredBlock

/**
  * @param hashes list of unmined hashes
  */
case class UnminedHashes(hashes: Seq[String] = Seq.empty)

/**
  * @param anchorTo which blockchain we anchor into
  * @param hash     hash of the anchor transaction
  * @param created  creation time
  * @param version  version of ubirchChainService that created the anchor
  */
case class Anchor(anchorTo: String,
                  hash: String,
                  created: DateTime = DateUtil.nowUTC,
                  version: String = "1.0"
                 )
