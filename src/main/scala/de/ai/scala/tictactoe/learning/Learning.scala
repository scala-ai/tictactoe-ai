package de.ai.scala.tictactoe.learning

import de.ai.scala.tictactoe.learning.action.Action
import de.ai.scala.tictactoe.learning.state.State

trait Learning[S >: State, A >: Action] {
  def init()

  def incrementEpoch()

  def incrementStep()

  def resetSteps()

  def addStepVisit(step: Long)

  def getDecision(state: S): A

  def trainHistory(reward: Double)

  def getEpochs: Int
}
