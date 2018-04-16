package de.ai.scala.tictactoe.learning

import de.ai.scala.tictactoe.learning.action.Action
import de.ai.scala.tictactoe.learning.state.State

trait Learning[S <: State, A <: Action] {
  def getDecision(state: S): A

  def trainHistory(reward: Double)
}
