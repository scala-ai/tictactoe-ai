package de.ai.htwg.tictactoe

import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import scala.collection.mutable.ListBuffer

import akka.actor.Actor
import akka.actor.Props
import de.ai.htwg.tictactoe.WatcherActor.EpochResult
import grizzled.slf4j.Logging


object WatcherActor {
  def props(trainingId: String) = Props(new WatcherActor(trainingId))

  case class EpochResult(epoch: Int, won: Int, lost: Int, draw: Int)
  case object PrintCSV
}
class WatcherActor(trainingId: String) extends Actor with Logging {

  private val results: ListBuffer[EpochResult] = ListBuffer()

  override def receive: Receive = {
    case r: EpochResult => results += r
    case WatcherActor.PrintCSV =>
      val csvString = buildCSV
      debug("csv:\n" + csvString)
      writeToFile(csvString)
  }

  def buildCSV: String = {
    val totalEpochs = results.head.epoch
    "epoch; wins; draws; losses; win percentage\n" +
      results.map(r =>
        f"${totalEpochs - r.epoch}; ${r.won}; ${r.draw}; ${r.lost}; ${(r.won + r.draw).toDouble / (r.won + r.lost + r.draw)}"
      ).mkString("\n")
  }


  def writeToFile(csvString: String): Unit = {
    val now = LocalDateTime.now.format(DateTimeFormatter.ofPattern("YYYY-MM-dd-HH-mm-ss-SSS"))
    val fileName = s"results/$trainingId.$now.training.csv"
    debug(s"Save training results to $fileName")
    val file = Paths.get(fileName)
    Files.createDirectories(file.getParent)
    Files.write(file, csvString.getBytes("UTF-8"))
  }
}
