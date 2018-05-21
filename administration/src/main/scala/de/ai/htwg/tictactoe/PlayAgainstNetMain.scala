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

object PlayAgainstNetMain extends PlayAgainstUi {
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


  override def buildOpponent(gameController: GameController): GameControllerPlayer = {
    aiTrainer.getNewAiPlayer(gameController, training = false)
  }

  start()
}