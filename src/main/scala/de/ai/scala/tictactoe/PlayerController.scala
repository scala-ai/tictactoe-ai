package de.ai.scala.tictactoe

import de.ai.scala.tictactoe.action.Action
import de.ai.scala.tictactoe.state.State

trait PlayerController[S >: State, A >: Action] {
  def getAction(state: S): A
}
