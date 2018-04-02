package de.ai.scala.tictactoe.learning.action

import de.ai.scala.tictactoe.learning.state.State

trait ActionSpace[S <: State, A <: Action] {
  def getPossibleActions(state: S): Set[A]
}
