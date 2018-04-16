package de.ai.htwg.tictactoe.aiClient

import de.ai.htwg.tictactoe.aiClient.learning.action.Action
import de.ai.htwg.tictactoe.aiClient.learning.state.State

trait PlayerController[S >: State, A >: Action] {
  def getAction(state: S): A
}
