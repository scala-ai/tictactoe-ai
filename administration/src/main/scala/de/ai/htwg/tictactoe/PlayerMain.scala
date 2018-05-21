package de.ai.htwg.tictactoe

import scala.concurrent.ExecutionContext

import akka.actor.ActorSystem
import de.ai.htwg.tictactoe.clientConnection.fxUI.UiMain
import de.ai.htwg.tictactoe.clientConnection.gameController.CallBackSubscriber
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.model.strategy.TTTWinStrategy3xBuilder
import de.ai.htwg.tictactoe.clientConnection.util.SingleThreadPlatform
import de.ai.htwg.tictactoe.gameLogic.controller.GameControllerImpl
import de.ai.htwg.tictactoe.playerClient.UiView
import de.ai.htwg.tictactoe.playerClient.UiPlayerController
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
    val gameController = GameControllerImpl(strategy, Player.Cross)
    def handleGameFinish(winner: Option[Player]): Unit = {
      info {
        winner match {
          case Some(Player.Cross) => s"First-Player wins"
          case None => "No winner in this game"
          case _ /* other player */ => s"Second-Player wins"
        }
      }
    }


    implicit val executionContext: ExecutionContext = platform.executionContext
    for {
      gameUi <- clientMain.getNewStage(gameName)
    } {
      val player1 = new UiPlayerController(gameUi, Player.Cross)
      val player2 = new UiPlayerController(gameUi, Player.Circle)
      val uiView = new UiView(gameUi, gameController.getGrid())
      gameController.subscribe(uiView)
      gameController.subscribe(CallBackSubscriber(handleGameFinish _))
      gameController.startGame(player1, player2)
    }
  }

}
