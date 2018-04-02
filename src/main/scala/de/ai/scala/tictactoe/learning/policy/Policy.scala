package de.ai.scala.tictactoe.learning.policy

import de.ai.scala.tictactoe.learning.action.Action
import de.ai.scala.tictactoe.learning.state.State

trait Policy[S >: State, A >: Action] {
  def nextAction(state: S): A

  def incrementStep()

  def resetSteps()
}
