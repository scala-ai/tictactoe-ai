package de.ai.htwg.tictactoe.aiClient.learning.core

import scala.util.Random

import de.ai.htwg.tictactoe.aiClient.learning.core.action.TTTAction
import de.ai.htwg.tictactoe.aiClient.learning.core.action.TTTActionSpace
import de.ai.htwg.tictactoe.aiClient.learning.core.net.TTTNeuralNet
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.EpsGreedy
import de.ai.htwg.tictactoe.aiClient.learning.core.reward.TTTRewardCalculator
import de.ai.htwg.tictactoe.aiClient.learning.core.state.TTTEpochResult
import de.ai.htwg.tictactoe.aiClient.learning.core.state.TTTState
import de.ai.htwg.tictactoe.aiClient.learning.core.transition.TTTTransition
import de.ai.htwg.tictactoe.aiClient.learning.core.transition.TransitionHistoryImpl

case class TTTLearningProcessor() {
  private var learning = QLearning[TTTState, TTTAction, TTTEpochResult](
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

  def getDecision(state: TTTState): TTTAction = {
    val (newLearning, action) = learning.getDecision(state)
    learning = newLearning
    action
  }

  def trainResult(win: Boolean): Unit = {
    learning = learning.trainHistory(TTTEpochResult(win))
  }
}
