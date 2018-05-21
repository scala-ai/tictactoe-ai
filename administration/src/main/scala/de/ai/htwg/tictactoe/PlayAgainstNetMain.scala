package de.ai.htwg.tictactoe

import java.util.concurrent.Executors

import scala.util.Random

import de.ai.htwg.tictactoe.aiClient.AiLearning
import de.ai.htwg.tictactoe.aiClient.learning.TTTLearningProcessor
import de.ai.htwg.tictactoe.aiClient.learning.TTTRewardCalculator
import de.ai.htwg.tictactoe.aiClient.learning.core.QLearningConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.EpsGreedyConfiguration
import de.ai.htwg.tictactoe.clientConnection.fxUI.UiMain
import de.ai.htwg.tictactoe.clientConnection.gameController.CallBackSubscriber
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.model.strategy.TTTWinStrategy3xBuilder
import de.ai.htwg.tictactoe.clientConnection.util.SingleThreadPlatform
import de.ai.htwg.tictactoe.gameLogic.controller.GameControllerImpl
import de.ai.htwg.tictactoe.playerClient.UiPlayer
import de.ai.htwg.tictactoe.playerClient.UiPlayerController
import grizzled.slf4j.Logging

object PlayAgainstNetMain extends App with Logging {
  private val strategy = TTTWinStrategy3xBuilder
  private val clientMain = UiMain(strategy.dimensions)
  private val platform = SingleThreadPlatform()
  private val gameName = "game"
  private val random = new Random(5L)

  private val aiTrainer = new AiLearning(TTTLearningProcessor.apply(
    policyProperties = EpsGreedyConfiguration(
      minEpsilon = 0.5f,
      nbEpochVisits = 50000,
      random = new Random(5L)
    ),
    qLearningProperties = QLearningConfiguration(),
    neuralNetFileName = "Vs8Syg.2018-05-19-18-57-44-372.network.zip",
    executors = Executors.newFixedThreadPool(5),
    rewardProperties = TTTRewardCalculator.defaultConfig()
  ), "testTraining")


  platform.execute {
    playGame(0)
  }

  def playGame(gameNumber: Int): Unit = {
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
      val uiView = new UiPlayer(gameUi, gameController.getGrid())
      gameController.subscribe(uiView)
      val aiPlayer = aiTrainer.getNewAiPlayer(gameController, training = false)
      gameController.subscribe(CallBackSubscriber(handleGameFinish _))
      gameController.startGame(aiPlayer, uiPlayer)
    }(platform.executionContext)
  }
}