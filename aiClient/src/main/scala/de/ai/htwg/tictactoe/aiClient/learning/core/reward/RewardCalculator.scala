package de.ai.htwg.tictactoe.aiClient.learning.core.reward

import de.ai.htwg.tictactoe.aiClient.learning.core.action.Action
import de.ai.htwg.tictactoe.aiClient.learning.core.state.EpochResult
import de.ai.htwg.tictactoe.aiClient.learning.core.state.State

trait RewardCalculator[A <: Action, S <: State, R <: EpochResult] {

  def getLongTermReward(runResult: R): Double

  def getImmediateReward(action: A, state: S): Double

}