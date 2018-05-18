package de.ai.htwg.tictactoe

import scala.concurrent.ExecutionContext

import akka.actor.ActorSystem
import de.ai.htwg.tictactoe.clientConnection.fxUI.UiMain
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.model.strategy.TTTWinStrategy3xBuilder
import de.ai.htwg.tictactoe.clientConnection.util.SingleThreadPlatform
import de.ai.htwg.tictactoe.gameLogic.controller.GameFieldController
import de.ai.htwg.tictactoe.playerClient.UiPlayer
import grizzled.slf4j.Logging

object PlayerMain extends App with Logging {
  trace("start 2 player game")
  val system = ActorSystem()

  val platform: SingleThreadPlatform = SingleThreadPlatform()
  val strategy = TTTWinStrategy3xBuilder
  val gameName = "game1"
  val clientMain = UiMain(strategy.dimensions)

  platform.execute {
    playGame()
  }

  def playGame(): Unit = {
    val gameController = GameFieldController(strategy, Player.Cross)
    var finishedPlayers = 0
    def handleGameFinish(winner: Option[Player]): Unit = {
      finishedPlayers += 1
      if (finishedPlayers >= 2) {
        info {
          winner match {
            case Some(Player.Cross) => s"First-Player wins"
            case None => "No winner in this game"
            case _ /* other player */ => s"Second-Player wins"
          }
        }
      }
    }

    implicit val executionContext: ExecutionContext = platform.executionContext
    for {
      pUi1 <- clientMain.getNewStage(gameName + "player1")
      pUi2 <- clientMain.getNewStage(gameName + "player2")
    } {
      val player1 = new UiPlayer(Player.Cross, pUi1, gameController.getGrid(), platform, handleGameFinish)
      val player2 = new UiPlayer(Player.Circle, pUi2, gameController.getGrid(), platform, handleGameFinish)
      gameController.subscribe(player1)
      gameController.subscribe(player2)
    }
  }

}
