package de.ai.htwg.tictactoe.aiClient.learning

import de.ai.htwg.tictactoe.aiClient.learning.core.reward.RewardCalculator
import de.ai.htwg.tictactoe.aiClient.learning.core.state.EpochResult

case class TTTRewardCalculator() extends RewardCalculator[TTTAction, TTTState] {
  override def getLongTermReward(runResult: EpochResult): Double = runResult match {
    case EpochResult.Won => 5
    case EpochResult.DrawDefense => 2
    case EpochResult.DrawOffense => 0
    case EpochResult.Lost => -10
  }

  override def getImmediateReward(action: TTTAction, state: TTTState): Double = {
    if (state.isStartingPlayer) {
      0.1
    } else {
      0.2
    }
  }
}
