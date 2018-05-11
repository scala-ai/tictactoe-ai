package de.ai.htwg.tictactoe

import scala.collection.mutable.ListBuffer

import akka.actor.Actor
import akka.actor.Props
import de.ai.htwg.tictactoe.WatcherActor.EpochResult
import grizzled.slf4j.Logging


object WatcherActor {
  def props() = Props(new WatcherActor())

  case class EpochResult(epoch: Int, won: Int, lost: Int, draw: Int)
  case class PrintCSV(epochsPerLine: Int)
}
class WatcherActor extends Actor with Logging {

  private val results: ListBuffer[EpochResult] = ListBuffer()

  override def receive: Receive = {
    case r: EpochResult => results += r
    case WatcherActor.PrintCSV(epl) =>
      info("csv:\n" + buildCSV(epl)) // TODO write actual CSV
  }

  def buildCSV(epochsPerLine: Int): String = {
    "epoch; wins; draws; losses; win percentage\n" +
      results.map(r =>
        f"${r.epoch}; ${r.won}; ${r.draw}; ${r.lost}; ${(r.won + r.draw).toFloat / (r.won + r.lost + r.draw)}%2.2f%%"
      ).mkString("\n")
  }
}
