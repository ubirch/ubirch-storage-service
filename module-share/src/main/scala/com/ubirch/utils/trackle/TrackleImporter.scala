package com.ubirch.utils.trackle

import java.util.UUID

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.JsonAST._
import org.json4s.native.Serialization._

import scala.io._
import java.io._

case class TrackleRawValue(
                            id: UUID,
                            sensorId: Int,
                            packetCount: Int,
                            timestampMin: Int,
                            t1: Double,
                            t2: Double,
                            t3: Double,
                            //                            tavg: Double,
                            day: String,
                            time: String,
                            timestamp: DateTime,
                            date: DateTime
                          ) {

  override def toString: String = s"id: $id: trackleId: $sensorId / t1: $t1 / t2: $t2 / t3: $t3 / date: $date"
}

/**
  * Created by derMicha on 07/06/16.
  */
object TrackleImporter extends App {

  //  implicit object TrackleRawValueIndexable extends Indexable[TrackleRawValue] {
  //    implicit val formats = DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all
  //
  //    override def json(t: TrackleRawValue): String = write(t)
  //
  //  }

  //  //  val uri = ElasticsearchClientUri("elasticsearch://localhost:9300")
  //  val uri = ElasticsearchClientUri("elasticsearch://search-ubirchtrackledata-4ubdfnk44h35mi2lbpfuztp3ui.us-east-1.es.amazonaws.com:80")
  //  //  val client = ElasticClient.remote("search-ubirchtrackledata-4ubdfnk44h35mi2lbpfuztp3ui.us-east-1.es.amazonaws.com", 80)
  //  //  val client = ElasticClient.transport(uri)
  //
  //  val client = ElasticClient.remote("search-ubirchtrackledata-4ubdfnk44h35mi2lbpfuztp3ui.us-east-1.es.amazonaws.com", 80)

  //  val host = "search-ubirchtrackledata-4ubdfnk44h35mi2lbpfuztp3ui.us-east-1.es.amazonaws.com"
  //  val port = 80
  //  val restClient = new RestlasticSearchClient(new StaticEndpoint(new Endpoint(host, port)))

  val tsFmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
  val dayFmt = DateTimeFormat.forPattern("yyyy-MM-dd")

  //  client.execute {
  //    deleteIndex("trackleresults")
  //  }

  val basePath = "/Users/derMicha/tmp/trackle/rawdataFiles/testLogData/"

  val filenames =
    List(
      //      "20160507/trackle_log_20160507.csv",
      //      "20160518/trackle_log_20160518.csv",
      //      "20160518/trackle_log__.csv",
      //      "20160530/trackle_log_20160530.csv",
      //      "20160531-1/trackle_log.csv",
      //      "20160531-inBody/20160531-inBody_trackle_log.csv",
      //      "20160531-inBody/trackle_log.csv",
      //      "20160601/20160601-inBody_trackle_log.csv",
      "20160602/20160602_trackle_log.csv",
      "20160603/20160603_trackle_log.csv",
      "20160604/20160604_trackle_log.csv",
      "20160605/20160605_trackle_log.csv",
      "20160606/20160606_trackle_log.csv",
      "20160607/20160607_trackle_log.csv",
      "20160608/20160608_trackle_log.csv",
      "20160609/20160609_trackle_log.csv",
      "20160610/20160610_trackle_log.csv",
      "20160611/20160611a_trackle_log.csv",
      "20160611/20160611b_trackle_log.csv",
      "20160612/20160612_trackle_log.csv",
      "20160613/20160613_trackle_log.csv",
      "20160614/20160614_trackle_log.csv",
      "20160615/20160615_trackle_log.csv",
      "20160616/20160616_trackle_log.csv",
      "20160617/20160617_trackle_log.csv",
      "20160619/20160619_trackle_log.csv",
      "20160629/20160629_trackle_log.csv",
      "20160630/20160630_trackle_log.csv",
      "20160701/20160701_trackle_log.csv",
      "20160702/20160702_trackle_log.csv",
      "20160704/20160704_trackle_log.csv",
      "20160705/20160705_trackle_log.csv",
      "20160706/20160706_trackle_log.csv",
      "20160707/20160707_trackle_log.csv",
      "20160710/20160710_trackle_log.csv",
      "20160711/20160711_trackle_log.csv",
      "20160712/20160712_trackle_log.csv",
      "20160713/20160713_trackle_log.csv",
      "20160721/20160721_trackle_log.csv",
      "20160722/20160722_trackle_log.csv",
      "20160724/20160724_trackle_log.csv",
      "20160726/20170726_trackle_log.csv",
      "20160727/20160727_trackle_log.csv",
      "20160729/20160729_trackle_log.csv",
      "20160731/20160731_trackle_log.csv",
      "20160801/20160801_trackle_log.csv",
      "20160802/20160802_trackle_log.csv"
    )

  val outfilenamen = s"${basePath}trackle_log.csv"
  val outfile = new File(outfilenamen)
  if (outfile.isFile)
    if (outfile.delete())
      println("deleted")

  printToFile(outfile) { p =>

    val allValues = filenames.map { filename =>
      for {
        line <- Source.fromFile(s"$basePath$filename").getLines().toList
      } yield line
    }.foldLeft(List[String]())(_ ++ _).map { line =>
      val values = line.split(';')

      val currentTimestamp = DateTime.parse(s"${values(6)} ${values(7)}", tsFmt)
      val fixedDay = currentTimestamp.getHourOfDay match {
        case h if h <= 12 =>
          currentTimestamp.minusDays(1)
        case _ =>
          currentTimestamp
      }

      TrackleRawValue(
        id = UUID.randomUUID(),
        sensorId = values(0).toInt,
        packetCount = values(1).toInt,
        timestampMin = values(2).toInt,
        t1 = values(3).toDouble,
        t2 = values(4).toDouble,
        t3 = values(5).toDouble,
        //        tavg = (values(3).toDouble + values(4).toDouble + values(5).toDouble) / 3,
        day = fixedDay.toString(dayFmt),
        time = values(7),
        timestamp = currentTimestamp,
        date = fixedDay
      )
      //    }.filter(p => p.tavg > 36.0 && p.tavg < 38.0).filter(p => p.timestamp.getHourOfDay > 23 || p.timestamp.getHourOfDay < 7)
    }
    allValues.foreach { v =>
      writeData(p, v)
    }

    //    val days = allValues.map(_.day).toSet[String]
    //    val reducedValues = days.foreach { d =>
    //      val minT = allValues.filter(v => d.equals(v.day)).reduce { (a, b) =>
    //        if (a.tavg < b.tavg)
    //          a
    //        else
    //          b
    //      }
    //      client.execute {
    //        index into "trackleresults" / "trackleresult" id minT.id source minT
    //      }
    //      writeData(p, d)
    //    }
  }

  def writeData(p: PrintWriter, trackleRawValue: TrackleRawValue): Unit = {
    val avg = (trackleRawValue.t1 + trackleRawValue.t3) / 2.0

    val vals = List(
      trackleRawValue.timestampMin,
      trackleRawValue.day,
      trackleRawValue.time,
      avg
    ).mkString("\"", "\",\"", "\"")

    val line = s"$vals,0,,,,0,0,"
    p.println(line)
  }

  def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
    val p = new java.io.PrintWriter(f)
    try {
      op(p)
    } finally {
      p.close()
    }
  }

  //  def writeToEs(device: Device, trackleRawValue: TrackleRawValue) = ???
}