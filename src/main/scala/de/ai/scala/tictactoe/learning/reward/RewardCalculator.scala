package de.ai.scala.tictactoe.learning.reward

import de.ai.scala.tictactoe.learning.action.Action
import de.ai.scala.tictactoe.learning.state.EpochResult
import de.ai.scala.tictactoe.learning.state.State

trait RewardCalculator[A <: Action, S <: State, R <: EpochResult] {

  def getLongTermReward(runResult: R): Double

  def getImmediateReward(action: A, state: S): Double

}