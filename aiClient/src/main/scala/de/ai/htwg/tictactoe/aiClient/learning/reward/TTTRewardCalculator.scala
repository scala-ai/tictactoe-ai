package de.ai.htwg.tictactoe.aiClient.learning.reward

import de.ai.htwg.tictactoe.aiClient.learning.action.TTTAction
import de.ai.htwg.tictactoe.aiClient.learning.state.TTTEpochResult
import de.ai.htwg.tictactoe.aiClient.learning.state.TTTState

case class TTTRewardCalculator() extends RewardCalculator[TTTAction, TTTState, TTTEpochResult] {
  override def getLongTermReward(runResult: TTTEpochResult): Double =
    if (runResult.win) {
      100
    } else {
      -100
    }

  override def getImmediateReward(action: TTTAction, state: TTTState): Double = -0.000001
}
