package de.ai.htwg.tictactoe.aiClient.learning

import scala.util.Random

import de.ai.htwg.tictactoe.aiClient.learning.core.QLearning
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.EpsGreedy
import de.ai.htwg.tictactoe.aiClient.learning.core.transition.TransitionHistoryImpl

case class TTTLearningProcessor(
    learning: QLearning[TTTState, TTTAction, TTTEpochResult]
) {

  def getDecision(state: TTTState): (TTTAction, TTTLearningProcessor) = {
    val (newLearning, action) = learning.getDecision(state)
    (action, copy(learning = newLearning))
  }

  def trainResult(result: TTTEpochResult): TTTLearningProcessor = copy(learning.trainHistory(result))
}

object TTTLearningProcessor {
  def apply(): TTTLearningProcessor = new TTTLearningProcessor(
    QLearning[TTTState, TTTAction, TTTEpochResult](
      policy = EpsGreedy[TTTState, TTTAction](
        random = Random,
        minEpsilon = 0.01f,
        epsilonNbEpoch = 100
      ),
      rewardCalculator = TTTRewardCalculator(),
      neuralNet = TTTNeuralNet(),
      transitionHistory = TransitionHistoryImpl[TTTAction, TTTState](),
      transitionFactory = TTTTransition,
      actionSpace = TTTActionSpace()
    )
  )
}
