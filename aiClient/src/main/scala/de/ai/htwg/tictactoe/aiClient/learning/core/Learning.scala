package de.ai.htwg.tictactoe.aiClient.learning.core

import de.ai.htwg.tictactoe.aiClient.learning.core.action.Action
import de.ai.htwg.tictactoe.aiClient.learning.core.state.EpochResult
import de.ai.htwg.tictactoe.aiClient.learning.core.state.State

trait Learning[S <: State, A <: Action, R <: EpochResult] {
  def getDecision(state: S): (Learning[S, A, R], A)

  def trainHistory(reward: R): Learning[S, A, R]
}
