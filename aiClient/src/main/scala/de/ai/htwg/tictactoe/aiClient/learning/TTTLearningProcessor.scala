package de.ai.htwg.tictactoe.aiClient.learning

import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import scala.io.Source

import de.ai.htwg.tictactoe.aiClient.learning.core.QLearning
import de.ai.htwg.tictactoe.aiClient.learning.core.QLearningConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.EpsGreedy
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.EpsGreedyConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.ExplorationStep
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.ExplorationStepConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.PolicyConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.transition.TransitionHistoryImpl
import grizzled.slf4j.Logging

class TTTLearningProcessor(
    learning: QLearning[TTTState, TTTAction, TTTEpochResult]
) extends Logging {

  def getTrainingDecision(state: TTTState): (TTTAction, TTTLearningProcessor) = {
    trace(s"Ask for training decision")
    val (newLearning, action) = learning.getTrainingDecision(state)
    (action, new TTTLearningProcessor(learning = newLearning))
  }

  def getBestDecision(state: TTTState): (TTTAction, TTTLearningProcessor) = {
    trace(s"Ask for best decision")
    val (newLearning, action) = learning.getBestDecision(state)
    (action, new TTTLearningProcessor(learning = newLearning))
  }

  def trainResult(result: TTTEpochResult): TTTLearningProcessor = new TTTLearningProcessor(learning.trainHistory(result))

  def persist(trainingId: String): Unit = {
    val now = LocalDateTime.now.format(DateTimeFormatter.ofPattern("YYYY-MM-dd-HH-mm-ss-SSS"))
    val fileName = s"nets/$trainingId.$now.network.zip"
    debug(s"Save current neural network to $fileName")
    val file = Paths.get(fileName)
    Files.createDirectories(file.getParent)
    learning.neuralNet.serialize(fileName)
  }

  def load(): TTTLearningProcessor = {
    val fileName = s"nets/2018-05-08-16-47-13-499.network"
    debug(s"Load current neural network from $fileName")
    val file = Source.fromFile(fileName, "UTF-8")
      .getLines()
      .mkString("#")
    val deserializeNet = TTTQTable.deserialize(file)
    new TTTLearningProcessor(learning = learning.copy(neuralNet = deserializeNet))
  }
}

object TTTLearningProcessor {
  def apply(
      dimensions: Int,
      policyProperties: PolicyConfiguration,
      qLearningProperties: QLearningConfiguration,
  ): TTTLearningProcessor = new TTTLearningProcessor(
    QLearning[TTTState, TTTAction, TTTEpochResult](
      policy = policyProperties match {
        case c: EpsGreedyConfiguration => EpsGreedy[TTTState, TTTAction](c)
        case c: ExplorationStepConfiguration => ExplorationStep[TTTState, TTTAction](c)
      },
      rewardCalculator = TTTRewardCalculator(),
      neuralNet = TTTNeuralNet(dimensions),
      transitionHistory = TransitionHistoryImpl[TTTAction, TTTState](),
      transitionFactory = TTTTransition,
      actionSpace = TTTActionSpace(),
      qLearningProperties = qLearningProperties
    )
  )
}
