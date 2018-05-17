package de.ai.htwg.tictactoe

import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors

import scala.collection.mutable.ListBuffer

import de.ai.htwg.tictactoe.aiClient.AiLearning.LearningProcessorConfiguration
import grizzled.slf4j.Logging


class Watcher(val trainingId: String, val seed: Long, val properties: LearningProcessorConfiguration) extends Logging {

  private val results: ListBuffer[Watcher.EpochResult] = ListBuffer()
  private val executors = Executors.newFixedThreadPool(5)

  def addEpochResult(r: Watcher.EpochResult): Unit = {
    results += r
  }

  def printCSV(): Unit = {
    val list = results.toList
    executors.execute(() => {
      list match {
        case Nil => info("no epoch results recoded")
        case nonEmpty =>
          val csvString = buildCSV(nonEmpty)
          debug("csv:\n" + csvString)
          writeToFile(csvString)
      }
    })
  }

  private def buildCSV(results: List[Watcher.EpochResult]): String = {
    val totalEpochs = results.head.epoch
    s"properties:; $seed; ${properties.dimensions}; ${properties.neuralNetProperties}; ${properties.policyProperties}; ${properties.qLearningProperties}\n" +
      "epoch; wins; defDraws; offDraw; losses; win percentage\n" +
      results.map { r =>
        val totalGames = r.won + r.defDraw + r.offDraw + r.lost
        f"${totalEpochs - r.epoch}; ${r.won}; ${r.defDraw}; ${r.offDraw}; ${r.lost}; ${(r.won + r.defDraw).toDouble / totalGames}%2.2f"
      }.mkString("\n")
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
  case class EpochResult(epoch: Int, won: Int, lost: Int, offDraw: Int, defDraw: Int)
}
