package de.ai.htwg.tictactoe

import scala.util.Random

import de.ai.htwg.tictactoe.clientConnection.fxUI.UiMain
import de.ai.htwg.tictactoe.clientConnection.gameController.CallBackSubscriber
import de.ai.htwg.tictactoe.clientConnection.gameController.GameController
import de.ai.htwg.tictactoe.clientConnection.gameController.GameControllerPlayer
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.model.strategy.TTTWinStrategy3xBuilder
import de.ai.htwg.tictactoe.clientConnection.util.SingleThreadPlatform
import de.ai.htwg.tictactoe.gameLogic.controller.GameControllerImpl
import de.ai.htwg.tictactoe.playerClient.UiPlayerController
import de.ai.htwg.tictactoe.playerClient.UiView
import grizzled.slf4j.Logging

trait PlayAgainstUi extends App with Logging {
  val strategy = TTTWinStrategy3xBuilder
  val random = new Random(5L)
  val opponentPlayer = Player.Cross


  private val clientMain = UiMain(strategy.dimensions)
  private val platform = SingleThreadPlatform()
  private val gameName = "game"

  def buildOpponent(gameController: GameController): GameControllerPlayer

  protected def start(): Unit = {
    platform.execute {
      playGame(0)
    }

  }

  private def playGame(gameNumber: Int): Unit = {
    val startPlayer = if (random.nextBoolean) Player.Cross else Player.Circle
    val gameController = GameControllerImpl(strategy, startPlayer)

    def handleGameFinish(winner: Option[Player]): Unit = {
      info {
        winner match {
          case Some(Player.Circle) => s"Human-Player wins"
          case None => "No winner in this game"
          case _ /* other player */ => s"AI-Player wins"
        }
      }
      platform.execute {
        playGame(gameNumber + 1)
      }
    }

    clientMain.getNewStage(gameName + gameNumber).foreach { gameUi =>
      val uiPlayer = new UiPlayerController(gameUi, Player.Circle)
      val uiView = new UiView(gameUi, gameController.getGrid())
      gameController.subscribe(uiView)
      val opponent = buildOpponent(gameController)
      gameController.subscribe(CallBackSubscriber(handleGameFinish _))
      gameController.startGame(opponent, uiPlayer)
    }(platform.executionContext)
  }
}