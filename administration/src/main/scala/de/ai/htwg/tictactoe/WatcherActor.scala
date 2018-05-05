package de.ai.htwg.tictactoe

import akka.actor.Actor
import akka.actor.Props
import de.ai.htwg.tictactoe.aiClient.AiActor
import de.ai.htwg.tictactoe.aiClient.learning.TTTEpochResult
import grizzled.slf4j.Logging


object WatcherActor {
  def props() = Props(new WatcherActor())
}
class WatcherActor extends Actor with Logging {

  private var wonGames = 0
  private var lostGames = 0
  private var drawGames = 0
  private var epoch = 0


  override def receive: Receive = {
    case AiActor.TrainingEpochResult(result) =>
      epoch = epoch + 1
      result match {
        case TTTEpochResult(true, false) => wonGames = wonGames + 1
        case TTTEpochResult(false, false) => lostGames = lostGames + 1
        case TTTEpochResult(false, true) => drawGames = drawGames + 1
        case _ => error("Unknown epoch result!")
      }
      //addData.put((epoch, wonGames))
      info(s"$epoch - (+) $wonGames (0) $drawGames (-) $lostGames = ${(wonGames + drawGames).toFloat / (wonGames + lostGames + drawGames)}")
  }

}
