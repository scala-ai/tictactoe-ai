package de.ai.htwg.tictactoe.aiClient.learning

import de.ai.htwg.tictactoe.aiClient.learning.action.Action
import de.ai.htwg.tictactoe.aiClient.learning.state.State

trait Learning[S <: State, A <: Action] {
  def getDecision(state: S): (Learning[S, A], A)

  def trainHistory(reward: Double): Learning[S, A]
}
