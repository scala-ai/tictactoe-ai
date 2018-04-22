package de.ai.htwg.tictactoe.aiClient.learning

import de.ai.htwg.tictactoe.aiClient.learning.action.Action
import de.ai.htwg.tictactoe.aiClient.learning.state.EpochResult
import de.ai.htwg.tictactoe.aiClient.learning.state.State

trait Learning[S <: State, A <: Action, R <: EpochResult] {
  def getDecision(state: S): (Learning[S, A, R], A)

  def trainHistory(reward: R): Learning[S, A, R]
}
