package de.ai.htwg.tictactoe.aiClient.learning

import de.ai.htwg.tictactoe.aiClient.learning.TTTRewardCalculator.Configuration
import de.ai.htwg.tictactoe.aiClient.learning.core.reward.RewardCalculator
import de.ai.htwg.tictactoe.aiClient.learning.core.reward.RewardCalculatorConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.state.EpochResult

case class TTTRewardCalculator(config: Configuration) extends RewardCalculator[TTTAction, TTTState] {
  override def getLongTermReward(runResult: EpochResult): Double = runResult match {
    case EpochResult.Won => config.won
    case EpochResult.DrawDefense => config.drawDefense
    case EpochResult.DrawOffense => config.drawOffense
    case EpochResult.Lost => config.lost
  }

  override def getImmediateReward(action: TTTAction, state: TTTState): Double = {
    if (state.isStartingPlayer) {
      config.immediateStartingPlayer
    } else {
      config.immediate
    }
  }
}

object TTTRewardCalculator {
  def defaultConfig(): TTTRewardCalculator.Configuration = Configuration(
    5, 2, 0, -10, 0.1, 0.2
  )

  case class Configuration(
      won: Double,
      drawDefense: Double,
      drawOffense: Double,
      lost: Double,
      immediateStartingPlayer: Double,
      immediate: Double
  ) extends RewardCalculatorConfiguration
}