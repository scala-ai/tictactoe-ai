package de.ai.scala.aiClient.learning.reward

import de.ai.scala.aiClient.learning.action.Action
import de.ai.scala.aiClient.learning.state.EpochResult
import de.ai.scala.aiClient.learning.state.State

trait RewardCalculator[A <: Action, S <: State, R <: EpochResult] {

  def getLongTermReward(runResult: R): Double

  def getImmediateReward(action: A, state: S): Double

}