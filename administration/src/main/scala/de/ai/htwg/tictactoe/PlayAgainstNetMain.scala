package de.ai.htwg.tictactoe

import java.util.concurrent.Executors

import scala.util.Random

import de.ai.htwg.tictactoe.aiClient.AiLearning
import de.ai.htwg.tictactoe.aiClient.learning.TTTLearningProcessor
import de.ai.htwg.tictactoe.aiClient.learning.TTTRewardCalculator
import de.ai.htwg.tictactoe.aiClient.learning.core.QLearningConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.EpsGreedyConfiguration
import de.ai.htwg.tictactoe.clientConnection.fxUI.UiMain
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.model.strategy.TTTWinStrategy3xBuilder
import de.ai.htwg.tictactoe.clientConnection.util.SingleThreadPlatform
import de.ai.htwg.tictactoe.gameLogic.controller.GameFieldControllerImpl
import de.ai.htwg.tictactoe.playerClient.UiPlayer
import grizzled.slf4j.Logging

object PlayAgainstNetMain extends App with Logging {
  private val strategy = TTTWinStrategy3xBuilder
  private val clientMain = UiMain(strategy.dimensions)
  private val platform = SingleThreadPlatform()
  private val gameName = "game1"

  private val aiTrainer = new AiLearning(TTTLearningProcessor.apply(
    policyProperties = EpsGreedyConfiguration(
      minEpsilon = 0.5f,
      nbEpochVisits = 50000,
      random = new Random(5L)
    ),
    qLearningProperties = QLearningConfiguration(),
    neuralNetFileName = "1kwyyR.2018-05-19-11-29-29-913.network.zip",
    executors = Executors.newFixedThreadPool(5),
    rewardProperties = TTTRewardCalculator.defaultConfig()
  ), "testTraining")


  platform.execute {
    playGame()
  }

  def playGame(): Unit = {
    val startPlayer = if (Random.nextBoolean) Player.Cross else Player.Circle
    val gameController = GameFieldControllerImpl(strategy, startPlayer)
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