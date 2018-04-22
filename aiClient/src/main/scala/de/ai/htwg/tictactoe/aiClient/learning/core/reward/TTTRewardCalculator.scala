package de.ai.htwg.tictactoe.aiClient.learning.core.reward

import de.ai.htwg.tictactoe.aiClient.learning.core.action.TTTAction
import de.ai.htwg.tictactoe.aiClient.learning.core.state.TTTEpochResult
import de.ai.htwg.tictactoe.aiClient.learning.core.state.TTTState

case class TTTRewardCalculator() extends RewardCalculator[TTTAction, TTTState, TTTEpochResult] {
  override def getLongTermReward(runResult: TTTEpochResult): Double =
    if (runResult.win) {
      100
    } else {
      -100
    }

  override def getImmediateReward(action: TTTAction, state: TTTState): Double = -0.000001
}
