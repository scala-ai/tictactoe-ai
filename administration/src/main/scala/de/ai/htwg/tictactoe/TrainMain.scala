package de.ai.htwg.tictactoe

import scala.util.Random

import de.ai.htwg.tictactoe.aiClient.AiLearning.LearningProcessorConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.TTTRewardCalculator
import de.ai.htwg.tictactoe.aiClient.learning.core.QLearningConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.net.NeuralNetConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.EpsGreedyConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.ExplorationStepConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.PolicyConfiguration
import de.ai.htwg.tictactoe.clientConnection.fxUI.UiMain
import de.ai.htwg.tictactoe.clientConnection.model.strategy.TTTWinStrategy3xBuilder
import de.ai.htwg.tictactoe.clientConnection.util.SingleThreadPlatform
import grizzled.slf4j.Logging
import org.nd4j.linalg.activations.Activation

object TrainMain extends App with Logging {
  // this direct logger call will prevent calls to not initialized loggers in a multi threaded environment (actor system)
  trace("game start")
  //  val strategy = TTTWinStrategy4xBuilder
  val strategy = TTTWinStrategy3xBuilder
  val seed = -292423813451567322L //new Random().nextLong
  val testSeed = 127368234235L
  // unique training id for a whole training execution run
  val trainingId = Random.alphanumeric.take(6).mkString
  val random = new Random(seed)
  val properties = LearningProcessorConfiguration(
    strategy.dimensions,
    buildEpsGreedyConfiguration(random),
    QLearningConfiguration(
      alpha = 0.03,
      gamma = 0.3
    ),
    NeuralNetConfiguration(
      hiddenLayers = 4,
      hiddenNodes = 64,
      inputNodes = strategy.dimensions * strategy.dimensions * 2,
      activationFunction = Activation.RELU
    ),
    TTTRewardCalculator.RewardConfiguration(
      won = 100, drawDefense = 90, drawOffense = 90, lost = -500, immediateStartingPlayer = 1, immediate = 1
    )
  )

  val clientMain = UiMain(strategy.dimensions)
  val platform = SingleThreadPlatform()
  platform.execute {
    val trainer = new Trainer(trainingId, seed, testRandom, testSeed, strategy, properties, clientMain, platform)
    trainer.startTraining(60000)
  }


  private def buildEpsGreedyConfiguration(random: Random): PolicyConfiguration = EpsGreedyConfiguration(
    minEpsilon = 0.5f,
    nbEpochVisits = 50000,
    random = random
  )

  private def buildExplorationStepConfiguration(random: Random): PolicyConfiguration = ExplorationStepConfiguration(
    minEpsilon = 0.2f,
    nbStepVisits = 1000,
    random = random
  )
}
