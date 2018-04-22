package de.ai.htwg.tictactoe.aiClient.learning

import scala.util.Random

import de.ai.htwg.tictactoe.aiClient.learning.action.TicTacToeAction
import de.ai.htwg.tictactoe.aiClient.learning.action.TicTacToeActionSpace
import de.ai.htwg.tictactoe.aiClient.learning.net.TicTacToeNeuralNet
import de.ai.htwg.tictactoe.aiClient.learning.policy.EpsGreedy
import de.ai.htwg.tictactoe.aiClient.learning.reward.TicTacToeRewardCalculator
import de.ai.htwg.tictactoe.aiClient.learning.state.TicTacToeEpochResult
import de.ai.htwg.tictactoe.aiClient.learning.state.TicTacToeState
import de.ai.htwg.tictactoe.aiClient.learning.transition.TicTacToeTransition
import de.ai.htwg.tictactoe.aiClient.learning.transition.TransitionHistoryImpl

case class TTTLearningProcessor() {
  private var learning = QLearning[TicTacToeState, TicTacToeAction, TicTacToeEpochResult](
    policy = EpsGreedy[TicTacToeState, TicTacToeAction](
      random = Random,
      minEpsilon = 0.01f,
      epsilonNbEpoch = 100
    ),
    rewardCalculator = TicTacToeRewardCalculator(),
    neuralNet = TicTacToeNeuralNet(),
    transitionHistory = TransitionHistoryImpl[TicTacToeAction, TicTacToeState](),
    transitionFactory = TicTacToeTransition,
    actionSpace = TicTacToeActionSpace()
  )

  def getDecision(state: TicTacToeState): TicTacToeAction = {
    val (newLearning, action) = learning.getDecision(state)
    learning = newLearning
    action
  }

  def trainResult(win: Boolean): Unit = {
    learning = learning.trainHistory(TicTacToeEpochResult(win))
  }
}
