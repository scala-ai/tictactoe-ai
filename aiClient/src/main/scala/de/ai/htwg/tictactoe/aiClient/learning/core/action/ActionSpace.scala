package de.ai.htwg.tictactoe.aiClient.learning.core.action

import de.ai.htwg.tictactoe.aiClient.learning.core.state.State

trait ActionSpace[S <: State, A <: Action] {
  def getPossibleActions(state: S): List[A]
}
