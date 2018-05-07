package de.ai.htwg.tictactoe

import scala.collection.mutable.ListBuffer

import akka.actor.Actor
import akka.actor.Props
import de.ai.htwg.tictactoe.aiClient.AiActor
import de.ai.htwg.tictactoe.clientConnection.messages.GameControllerMessages
import grizzled.slf4j.Logging


object WatcherActor {
  def props() = Props(new WatcherActor())

  case class PrintCSV(epochsPerLine: Int)
}
class WatcherActor extends Actor with Logging {

  private var epoch = 0
  private val results: ListBuffer[GameControllerMessages.GameResult] = ListBuffer[GameControllerMessages.GameResult]()

  override def receive: Receive = {
    case AiActor.TrainingEpochResult(result) =>
      epoch += 1
      results += result
    //      info(s"$epoch - (+) $wonGames (0) $drawGames (-) $lostGames = ${(wonGames + drawGames).toFloat / (wonGames + lostGames + drawGames)}")

    case WatcherActor.PrintCSV(epl) =>
      info("csv:\n" + buildCSV(epl)) // TODO write actual CSV

  }

  def buildCSV(epochsPerLine: Int): String = {
    val lines: ListBuffer[String] = ListBuffer[String]()
    lines += "epoch; wins; draws; losses; win percentage"
    var wonGames = 0
    var lostGames = 0
    var drawGames = 0
    var epoch = 0
    results.foreach { gr =>
      epoch += 1
      gr match {
        case GameControllerMessages.GameWon => wonGames += 1
        case GameControllerMessages.GameDraw => drawGames += 1
        case GameControllerMessages.GameLost => lostGames += 1
      }
      if (epoch % epochsPerLine == 0) {
        lines += f"$epoch; $wonGames; $drawGames; $lostGames; ${(wonGames + drawGames).toDouble / epoch * 100}%2.2f%%"
      }
    }
    lines.mkString("\n")
  }
}
