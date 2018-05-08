package de.ai.htwg.tictactoe.aiClient.learning

import de.ai.htwg.tictactoe.aiClient.learning.core.reward.RewardCalculator

case class TTTRewardCalculator() extends RewardCalculator[TTTAction, TTTState, TTTEpochResult] {
  override def getLongTermReward(runResult: TTTEpochResult): Double = runResult match {
    case r if r.win => 500
    case r if r.undecided => 26
    case _ => -100
  }

  override def getImmediateReward(action: TTTAction, state: TTTState): Double = -1
}
