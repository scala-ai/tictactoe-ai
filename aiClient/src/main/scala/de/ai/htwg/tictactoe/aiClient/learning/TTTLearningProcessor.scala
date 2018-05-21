package de.ai.htwg.tictactoe.aiClient.learning

import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ExecutorService

import de.ai.htwg.tictactoe.aiClient.learning.core.QLearning
import de.ai.htwg.tictactoe.aiClient.learning.core.QLearningConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.net.Dl4JNeuralNet
import de.ai.htwg.tictactoe.aiClient.learning.core.net.NeuralNet
import de.ai.htwg.tictactoe.aiClient.learning.core.net.NeuralNetConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.EpsGreedy
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.EpsGreedyConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.ExplorationStep
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.ExplorationStepConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.PolicyConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.reward.RewardCalculatorConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.state.EpochResult
import de.ai.htwg.tictactoe.aiClient.learning.core.transition.TransitionHistoryImpl
import grizzled.slf4j.Logging

class TTTLearningProcessor(
    val learning: QLearning[TTTState, TTTAction],
    val executors: ExecutorService
) extends Logging {

  def getTrainingDecision(state: TTTState): (TTTAction, TTTLearningProcessor) = {
    trace(s"Ask for training decision")
    val (newLearning, action) = learning.getTrainingDecision(state)
    (action, new TTTLearningProcessor(learning = newLearning, executors))
  }

  def getBestDecision(state: TTTState): (TTTAction, TTTLearningProcessor) = {
    trace(s"Ask for best decision")
    // val (newLearning, action) = learning.getBestDecision(state)
    val (newLearning, action) = learning.getMinimaxDecision(state,
      (state, action) => TTTState(
        state.field.setPos(action.coordinate.x, action.coordinate.y),
        !state.isStartingPlayer
      )
    )
    (action, new TTTLearningProcessor(learning = newLearning, executors))
  }

  def trainResult(result: EpochResult): TTTLearningProcessor = new TTTLearningProcessor(learning.trainHistory(result), executors)

  def persist(trainingId: String): Unit = {
    executors.execute(() => {
      val now = LocalDateTime.now.format(DateTimeFormatter.ofPattern("YYYY-MM-dd-HH-mm-ss-SSS"))
      val fileName = s"${TTTLearningProcessor.networkFilesLocation}$trainingId.$now.network.zip"
      debug(s"Save current neural network to $fileName")
      val file = Paths.get(fileName)
      Files.createDirectories(file.getParent)
      learning.neuralNet.serialize(fileName)
    })
  }
}

object TTTLearningProcessor {
  val networkFilesLocation = "nets/"

  def apply(
      policyProperties: PolicyConfiguration,
      qLearningProperties: QLearningConfiguration,
      neuralNetConfiguration: NeuralNetConfiguration,
      executors: ExecutorService,
      rewardProperties: RewardCalculatorConfiguration
  ): TTTLearningProcessor = new TTTLearningProcessor(
    createQLearning(
      policyProperties,
      qLearningProperties,
      Dl4JNeuralNet(neuralNetConfiguration),
      rewardProperties
    ),
    executors
  )

  def apply(
      policyProperties: PolicyConfiguration,
      qLearningProperties: QLearningConfiguration,
      neuralNetFileName: String,
      executors: ExecutorService,
      rewardProperties: RewardCalculatorConfiguration
  ): TTTLearningProcessor = {
    val path = s"$networkFilesLocation$neuralNetFileName"
    val network = Dl4JNeuralNet.deserialize(path)
    new TTTLearningProcessor(
      createQLearning(
        policyProperties,
        qLearningProperties,
        network,
        rewardProperties
      ),
      executors
    )
  }

  private def createQLearning(
      policyProperties: PolicyConfiguration,
      qLearningProperties: QLearningConfiguration,
      neuralNet: NeuralNet,
      rewardProperties: RewardCalculatorConfiguration
  ) = {
    QLearning[TTTState, TTTAction](
      policy = policyProperties match {
        case c: EpsGreedyConfiguration => EpsGreedy[TTTState, TTTAction](c)
        case c: ExplorationStepConfiguration => ExplorationStep[TTTState, TTTAction](c)
      },
      rewardCalculator = rewardProperties match {
        case c: TTTRewardCalculator.RewardConfiguration => TTTRewardCalculator(c)
      },
      neuralNet = neuralNet,
      transitionHistory = TransitionHistoryImpl[TTTAction, TTTState](),
      transitionFactory = TTTTransition,
      actionSpace = TTTActionSpace(),
      qLearningProperties = qLearningProperties
    )
  }
}
