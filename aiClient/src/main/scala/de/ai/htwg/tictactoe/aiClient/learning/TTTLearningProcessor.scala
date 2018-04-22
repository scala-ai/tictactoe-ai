package de.ai.htwg.tictactoe.aiClient.learning

import scala.util.Random

import de.ai.htwg.tictactoe.aiClient.learning.action.TTTAction
import de.ai.htwg.tictactoe.aiClient.learning.action.TTTActionSpace
import de.ai.htwg.tictactoe.aiClient.learning.net.TTTNeuralNet
import de.ai.htwg.tictactoe.aiClient.learning.policy.EpsGreedy
import de.ai.htwg.tictactoe.aiClient.learning.reward.TTTRewardCalculator
import de.ai.htwg.tictactoe.aiClient.learning.state.TTTEpochResult
import de.ai.htwg.tictactoe.aiClient.learning.state.TTTState
import de.ai.htwg.tictactoe.aiClient.learning.transition.TTTTransition
import de.ai.htwg.tictactoe.aiClient.learning.transition.TransitionHistoryImpl

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
