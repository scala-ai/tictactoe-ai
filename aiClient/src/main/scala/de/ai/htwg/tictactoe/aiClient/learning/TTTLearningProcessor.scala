package de.ai.htwg.tictactoe.aiClient.learning

import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ExecutorService

import de.ai.htwg.tictactoe.aiClient.learning.TTTRewardCalculator.RewardConfiguration
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
    val executors: ExecutorService,
    val ratingConfig: RewardConfiguration,
) extends Logging {

  private def updateLearning(learning: QLearning[TTTState, TTTAction]): TTTLearningProcessor = {
    new TTTLearningProcessor(learning, executors, ratingConfig)
  }

  def getTrainingDecision(state: TTTState): (TTTAction, TTTLearningProcessor) = {
    trace(s"Ask for training decision")
    val (newLearning, action) = learning.getTrainingDecision(state)
    (action, updateLearning(learning = newLearning))
  }

  def getBestDecision(state: TTTState): (TTTAction, TTTLearningProcessor) = {
    trace(s"Ask for best decision")
    val (newLearning, action) = learning.getBestDecision(state)
    (action, updateLearning(learning = newLearning))
  }

  def getXBestDecisions(x: Int, state: TTTState): (List[TTTAction], TTTLearningProcessor) = {
    trace(s"Ask for $x best decision")

    val (newLearning, actions) = learning.getXBestDecision(x, state)
    (actions, updateLearning(learning = newLearning))
  }

  def getRatedDecisions(state: TTTState, possibleActions: List[TTTAction]): List[(TTTAction, Double)] = {
    val ratings = List(ratingConfig.won, ratingConfig.drawDefense, ratingConfig.drawOffense, ratingConfig.lost)
    val immediates = List(ratingConfig.immediate, ratingConfig.immediateStartingPlayer)
    val maxTurns = state.field.dimensions * state.field.dimensions
    val max = 1.1 * (ratings.max + {
      val maxI = immediates.max
      if (maxI < 0) 0 else maxI * maxTurns
    })

    val min = 1.1 * (ratings.min + {
      val minI = immediates.min
      if (minI > 0) 0 else minI * maxTurns
    })

    def normalize(rating: Double): Double = {
      if (rating >= 0) {
        rating / max * 100
      } else {
        rating / min * -100
      }
    }

    learning.calcRatedActions(state, possibleActions).map {
      case (a, rating) => a -> normalize(rating)
    }
  }

  def trainResult(result: EpochResult): TTTLearningProcessor = updateLearning(learning.trainHistory(result))

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
      rewardProperties,
    ),
    executors,
    rewardProperties.asInstanceOf[TTTRewardCalculator.RewardConfiguration],
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
        rewardProperties,
      ),
      executors,
      rewardProperties.asInstanceOf[TTTRewardCalculator.RewardConfiguration],
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
