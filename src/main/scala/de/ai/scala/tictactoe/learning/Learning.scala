package de.ai.scala.tictactoe.learning

import de.ai.scala.tictactoe.action.Action
import de.ai.scala.tictactoe.learning.reward.Reward
import de.ai.scala.tictactoe.state.State

trait Learning[S >: State, A >: Action] {
  def init()

  def incrementEpoch()

  def incrementStep()

  def resetSteps()

  def addStepVisit(step: Long)

  def getDecision(state: S): A

  def trainHistory(reward: Reward)

  def getEpochs: Int
}
