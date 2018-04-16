package de.ai.htwg.tictactoe.aiClient.learning.action

import de.ai.htwg.tictactoe.aiClient.learning.state.State

trait ActionSpace[S <: State, A <: Action] {
  def getPossibleActions(state: S): List[A]
}
