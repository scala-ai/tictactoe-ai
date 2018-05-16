package de.ai.htwg.tictactoe

import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import scala.collection.mutable.ListBuffer

import grizzled.slf4j.Logging


class Watcher(trainingId: String) extends Logging {

  private val results: ListBuffer[Watcher.EpochResult] = ListBuffer()

  def addEpochResult(r: Watcher.EpochResult): Unit = {
    results += r
  }

  def printCSV(): Unit = {
    results.toList match {
      case Nil => info("no epoch results recoded")
      case nonEmpty =>
        val csvString = buildCSV(nonEmpty)
        debug("csv:\n" + csvString)
        writeToFile(csvString)
    }
  }

  private def buildCSV(results: List[Watcher.EpochResult]): String = {
    val totalEpochs = results.head.epoch
    "epoch; wins; draws; losses; win percentage\n" +
      results.map(r =>
        f"${totalEpochs - r.epoch}; ${r.won}; ${r.draw}; ${r.lost}; ${(r.won + r.draw).toDouble / (r.won + r.lost + r.draw)}%2.2f"
      ).mkString("\n")
  }

  private def writeToFile(csvString: String): Unit = {
    val now = LocalDateTime.now.format(DateTimeFormatter.ofPattern("YYYY-MM-dd-HH-mm-ss-SSS"))
    val fileName = s"results/$trainingId.$now.training.csv"
    debug(s"Save training results to $fileName")
    val file = Paths.get(fileName)
    Files.createDirectories(file.getParent)
    Files.write(file, csvString.getBytes("UTF-8"))
  }
}

object Watcher {
  case class EpochResult(epoch: Int, won: Int, lost: Int, draw: Int)
}
