package de.ai.scala.tictactoe.policy

import de.ai.scala.tictactoe.action.Action
import de.ai.scala.tictactoe.state.State

trait Policy[S >: State, A >: Action] {
  def nextAction(state: S): A

  def incrementStep()

  def resetSteps()
}
