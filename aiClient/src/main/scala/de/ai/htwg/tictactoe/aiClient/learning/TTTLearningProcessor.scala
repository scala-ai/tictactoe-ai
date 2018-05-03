package de.ai.htwg.tictactoe.aiClient.learning

import de.ai.htwg.tictactoe.aiClient.learning.core.QLearning
import de.ai.htwg.tictactoe.aiClient.learning.core.QLearningConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.EpsGreedy
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.EpsGreedyConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.ExplorationStep
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.ExplorationStepConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.PolicyConfiguration
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
  def apply(
      policyProperties: PolicyConfiguration,
      qLearningProperties: QLearningConfiguration
  ): TTTLearningProcessor = new TTTLearningProcessor(
    QLearning[TTTState, TTTAction, TTTEpochResult](
      policy = policyProperties match {
        case c: EpsGreedyConfiguration => EpsGreedy[TTTState, TTTAction](c)
        case c: ExplorationStepConfiguration => ExplorationStep[TTTState, TTTAction](c)
      },
      rewardCalculator = TTTRewardCalculator(),
      neuralNet = TTTNeuralNet(),
      transitionHistory = TransitionHistoryImpl[TTTAction, TTTState](),
      transitionFactory = TTTTransition,
      actionSpace = TTTActionSpace(),
      qLearningProperties = qLearningProperties
    )
  )
}
