package de.ai.htwg.tictactoe.aiClient.learning.core.transition

import de.ai.htwg.tictactoe.aiClient.learning.core.action.Action
import de.ai.htwg.tictactoe.aiClient.learning.core.state.State

trait TransitionFactory[A <: Action, S <: State] {
  def apply(state: S, action: A, reward: Double): Transition[A, S]
}
