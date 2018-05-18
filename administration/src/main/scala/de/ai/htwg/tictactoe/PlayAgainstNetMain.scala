package de.ai.htwg.tictactoe

import java.util.concurrent.Executors

import scala.util.Random

import de.ai.htwg.tictactoe.aiClient.AiLearning
import de.ai.htwg.tictactoe.aiClient.learning.TTTLearningProcessor
import de.ai.htwg.tictactoe.aiClient.learning.core.QLearningConfiguration
import de.ai.htwg.tictactoe.clientConnection.fxUI.UiMain
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.model.strategy.TTTWinStrategy3xBuilder
import de.ai.htwg.tictactoe.clientConnection.util.SingleThreadPlatform
import de.ai.htwg.tictactoe.gameLogic.controller.GameFieldController
import de.ai.htwg.tictactoe.playerClient.UiPlayer
import grizzled.slf4j.Logging

object PlayAgainstNetMain extends App with Logging {
  private val strategy = TTTWinStrategy3xBuilder
  private val clientMain = UiMain(strategy.dimensions)
  private val platform = SingleThreadPlatform()
  private val gameName = "game1"

  private val aiTrainer = new AiLearning(TTTLearningProcessor.apply(
    policyProperties = Trainer.buildEpsGreedyConfiguration(new Random(5L)),
    qLearningProperties = QLearningConfiguration(),
    neuralNetFileName = "cOvMZd.2018-05-17-20-47-49-149.network.zip",
    executors = Executors.newFixedThreadPool(5)
  ), "testTraining")


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
            case Some(Player.Cross) => s"Human-Player wins"
            case None => "No winner in this game"
            case _ /* other player */ => s"AI-Player wins"
          }
        }
        platform.execute {
          playGame()
        }
      }
    }

    clientMain.getNewStage(gameName).foreach { gameUi =>
      val playerUi = new UiPlayer(Player.Circle, gameUi, gameController.getGrid(), platform, handleGameFinish)
      gameController.subscribe(playerUi)
      aiTrainer.registerGame(gameController, training = false, handleGameFinish)
    }(platform.executionContext)
  }
}