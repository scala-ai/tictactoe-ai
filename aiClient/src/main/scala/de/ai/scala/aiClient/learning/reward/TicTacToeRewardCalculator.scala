package de.ai.scala.aiClient.learning.reward

import de.ai.scala.aiClient.learning.action.TicTacToeAction
import de.ai.scala.aiClient.learning.state.TicTacToeEpochResult
import de.ai.scala.aiClient.learning.state.TicTacToeState

class TicTacToeRewardCalculator extends RewardCalculator[TicTacToeAction, TicTacToeState, TicTacToeEpochResult] {
  override def getLongTermReward(runResult: TicTacToeEpochResult): Double =
    if (runResult.win) {
      100
    } else {
      -100
    }

  override def getImmediateReward(action: TicTacToeAction, state: TicTacToeState): Double = -0.000001
}
