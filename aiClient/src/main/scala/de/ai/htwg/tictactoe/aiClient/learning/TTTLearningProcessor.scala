package de.ai.htwg.tictactoe.aiClient.learning

import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ExecutorService

import scala.io.Source

import de.ai.htwg.tictactoe.aiClient.learning.core.QLearning
import de.ai.htwg.tictactoe.aiClient.learning.core.QLearningConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.net.Dl4JNeuralNet
import de.ai.htwg.tictactoe.aiClient.learning.core.net.NeuralNetConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.EpsGreedy
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.EpsGreedyConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.ExplorationStep
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.ExplorationStepConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.PolicyConfiguration
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
    val (newLearning, action) = learning.getBestDecision(state)
    (action, new TTTLearningProcessor(learning = newLearning, executors))
  }

  def trainResult(result: EpochResult): TTTLearningProcessor = new TTTLearningProcessor(learning.trainHistory(result), executors)

  def persist(trainingId: String): Unit = {
    val serializedNet = learning.neuralNet.serialize()
    executors.execute(() => {
      val now = LocalDateTime.now.format(DateTimeFormatter.ofPattern("YYYY-MM-dd-HH-mm-ss-SSS"))
      val fileName = s"nets/$trainingId.$now.network"
      debug(s"Save current neural network to $fileName")
      val file = Paths.get(fileName)
      Files.createDirectories(file.getParent)
      Files.write(file, serializedNet.getBytes("UTF-8"))
    })
  }

  def load(fileName: String): TTTLearningProcessor = {
    val path = s"nets/$fileName"
    debug(s"Load current neural network from $path")
    val file = Source.fromFile(path, "UTF-8")
      .getLines()
      .mkString("#")
    val deserializeNet = TTTQTable.deserialize(file)
    new TTTLearningProcessor(learning = learning.copy(neuralNet = deserializeNet), executors)
  }
}

object TTTLearningProcessor {
  def apply(
      dimensions: Int,
      policyProperties: PolicyConfiguration,
      qLearningProperties: QLearningConfiguration,
      neuralNetConfiguration: NeuralNetConfiguration,
      executors: ExecutorService
  ): TTTLearningProcessor = new TTTLearningProcessor(
    QLearning[TTTState, TTTAction](
      policy = policyProperties match {
        case c: EpsGreedyConfiguration => EpsGreedy[TTTState, TTTAction](c)
        case c: ExplorationStepConfiguration => ExplorationStep[TTTState, TTTAction](c)
      },
      rewardCalculator = TTTRewardCalculator(),
      neuralNet = Dl4JNeuralNet(neuralNetConfiguration),
      transitionHistory = TransitionHistoryImpl[TTTAction, TTTState](),
      transitionFactory = TTTTransition,
      actionSpace = TTTActionSpace(),
      qLearningProperties = qLearningProperties
    ),
    executors
  )
}
