package de.ai.htwg.tictactoe

import de.ai.htwg.tictactoe.aiClient.AiLearning
import de.ai.htwg.tictactoe.aiClient.AiLearning.LearningProcessorConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.TTTRewardCalculator
import de.ai.htwg.tictactoe.aiClient.learning.core.QLearningConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.net.NeuralNetConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.EpsGreedyConfiguration
import de.ai.htwg.tictactoe.clientConnection.fxUI.UiMain
import de.ai.htwg.tictactoe.clientConnection.gameController.CallBackSubscriber
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.model.strategy.TTTWinStrategy3xBuilder
import de.ai.htwg.tictactoe.clientConnection.util.SingleThreadPlatform
import de.ai.htwg.tictactoe.gameLogic.controller.GameControllerImpl
import de.ai.htwg.tictactoe.playerClient.UiPlayer
import grizzled.slf4j.Logging

object AiMain extends App with Logging {
  trace("start game against Ai")
  val platform: SingleThreadPlatform = SingleThreadPlatform()
  val strategy = TTTWinStrategy3xBuilder
  val gameName = "game1"
  val clientMain = UiMain(strategy.dimensions)
  val properties = LearningProcessorConfiguration(strategy.dimensions, EpsGreedyConfiguration(), QLearningConfiguration(), NeuralNetConfiguration(), TTTRewardCalculator.defaultConfig())
  private val aiTrainer = AiLearning(properties, gameName)

  platform.execute {
    playGame()
  }

  def playGame(): Unit = {
    val gameController = GameControllerImpl(strategy, Player.Cross)

    def handleGameFinish(winner: Option[Player]): Unit = {
      info {
        winner match {
          case Some(Player.Circle) => s"Human-Player wins"
          case None => "No winner in this game"
          case _ /* other player */ => s"AI-Player wins"
        }
      }
    }

    clientMain.getNewStage(gameName).foreach { gameUi =>
      val playerUi = new UiPlayer(Player.Circle, gameUi, gameController.getGrid(), platform)
      gameController.subscribe(playerUi)
      aiTrainer.registerGame(gameController, training = false)
      gameController.subscribe(CallBackSubscriber(handleGameFinish _))
      gameController.startGame()
    }(platform.executionContext)
  }


}
