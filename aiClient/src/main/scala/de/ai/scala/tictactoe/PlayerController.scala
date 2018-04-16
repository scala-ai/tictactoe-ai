package de.ai.scala.tictactoe

import de.ai.scala.tictactoe.learning.action.Action
import de.ai.scala.tictactoe.learning.state.State

trait PlayerController[S >: State, A >: Action] {
  def getAction(state: S): A
}
