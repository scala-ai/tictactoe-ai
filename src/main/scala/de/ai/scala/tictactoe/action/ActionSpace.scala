package de.ai.scala.tictactoe.action

import de.ai.scala.tictactoe.state.State

trait ActionSpace[S >: State, A >: Action] {
  def getPossibleActions(state: S): List[A]
}
