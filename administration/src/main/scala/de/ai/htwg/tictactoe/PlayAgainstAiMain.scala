package de.ai.htwg.tictactoe

import java.util.concurrent.Executors

import scala.util.Random

import de.ai.htwg.tictactoe.aiClient.AiLearning
import de.ai.htwg.tictactoe.aiClient.learning.TTTLearningProcessor
import de.ai.htwg.tictactoe.aiClient.learning.TTTRewardCalculator
import de.ai.htwg.tictactoe.aiClient.learning.core.QLearningConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.EpsGreedyConfiguration
import de.ai.htwg.tictactoe.clientConnection.gameController.GameController
import de.ai.htwg.tictactoe.clientConnection.gameController.GameControllerPlayer
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.logicClient.MiniMaxWidthPlayer
import de.ai.htwg.tictactoe.logicClient.MiniMaxDepthPlayer
import de.ai.htwg.tictactoe.logicClient.MiniMaxWidthDepthPlayer
import de.ai.htwg.tictactoe.logicClient.RandomPlayer

object AiTrainerBuilder {
  val network3 = "Vs8Syg.2018-05-19-18-57-44-372.network.zip"
  val network4 = "ufML8a.2018-05-21-10-59-08-912.network.zip"
  val networkName = network4

  def build(): AiLearning = {
    new AiLearning(TTTLearningProcessor.apply(
      policyProperties = EpsGreedyConfiguration(
        minEpsilon = 0.5f,
        nbEpochVisits = 50000,
        random = new Random(5L)
      ),
      qLearningProperties = QLearningConfiguration(),
      neuralNetFileName = networkName,
      executors = Executors.newFixedThreadPool(5),
      rewardProperties = TTTRewardCalculator.defaultConfig()
    ), "testTraining")
  }
}

object PlayAgainstNetMain extends PlayAgainstUi {

  private lazy val aiTrainer = AiTrainerBuilder.build()

  override def buildOpponent(gameController: GameController): GameControllerPlayer = {
    aiTrainer.getNewAiPlayer(gameController, training = false)
  }

  start()
}

object PlayAgainstMiniMax extends PlayAgainstUi {
  override def buildOpponent(gameController: GameController): GameControllerPlayer = {
    new MiniMaxWidthPlayer(Player.Cross, strategy, new RandomPlayer(Player.Cross, random))
  }

  start()
}

object PlayAgainstNetMiniMaxDepthMain extends PlayAgainstUi {

  private lazy val aiTrainer = AiTrainerBuilder.build()

  override def buildOpponent(gameController: GameController): GameControllerPlayer = {
    val ai = aiTrainer.getNewAiPlayer(gameController, training = false)
    new MiniMaxDepthPlayer(Player.Cross, strategy, ai, maxDepth = 3)
  }

  start()
}

object PlayAgainstNetMiniMaxWidthDepthMain extends PlayAgainstUi {

  private lazy val aiTrainer = AiTrainerBuilder.build()

  override def buildOpponent(gameController: GameController): GameControllerPlayer = {
    val ai = aiTrainer.getNewAiPlayer(gameController, training = false)
    new MiniMaxWidthDepthPlayer(Player.Cross, strategy, ai, maxDepth = 4)
  }

  start()
}