package com.ubirch.backend.storage

import java.net.URL

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import com.ubirch.backend.storage.config.{ServerConfig, ServerConst}
import uk.co.bigbeeconsultants.http.HttpClient

/**
  * author: cvandrei
  * since: 2016-08-19
  */
trait StorageCleanUp extends LazyLogging {

  private lazy val conf = ConfigFactory.load

  /**
    * Delete everything in the indexes specified in [ServerConst.ES_ALL_INDEXES].
    */
  final def resetStorage(): Unit = {

    logger.info("start storage clean up")
    val httpClient = new HttpClient
    ServerConst.ES_ALL_INDEXES foreach { idx =>
      httpClient.delete(new URL(s"${ServerConfig.esUrl}/${conf.getString(idx)}"))
    }

  }

}
