package de.ai.htwg.tictactoe.aiClient.learning

import de.ai.htwg.tictactoe.aiClient.learning.core.reward.RewardCalculator
import de.ai.htwg.tictactoe.aiClient.learning.core.state.EpochResult

case class TTTRewardCalculator() extends RewardCalculator[TTTAction, TTTState] {
  override def getLongTermReward(runResult: EpochResult): Double = runResult match {
    case EpochResult.Won => 100
    case EpochResult.DrawDefense => 90
    case EpochResult.DrawOffense => 90
    case EpochResult.Lost => -500
  }

  override def getImmediateReward(action: TTTAction, state: TTTState): Double = 1
}
