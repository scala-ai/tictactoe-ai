package de.ai.htwg.tictactoe.aiClient.learning.reward

import de.ai.htwg.tictactoe.aiClient.learning.action.TicTacToeAction
import de.ai.htwg.tictactoe.aiClient.learning.state.TicTacToeEpochResult
import de.ai.htwg.tictactoe.aiClient.learning.state.TicTacToeState

case class TicTacToeRewardCalculator() extends RewardCalculator[TicTacToeAction, TicTacToeState, TicTacToeEpochResult] {
  override def getLongTermReward(runResult: TicTacToeEpochResult): Double =
    if (runResult.win) {
      100
    } else {
      -100
    }

  override def getImmediateReward(action: TicTacToeAction, state: TicTacToeState): Double = -0.000001
}
