package de.ai.htwg.tictactoe.aiClient.learning.reward

import de.ai.htwg.tictactoe.aiClient.learning.action.Action
import de.ai.htwg.tictactoe.aiClient.learning.state.EpochResult
import de.ai.htwg.tictactoe.aiClient.learning.state.State

trait RewardCalculator[A <: Action, S <: State, R <: EpochResult] {

  def getLongTermReward(runResult: R): Double

  def getImmediateReward(action: A, state: S): Double

}