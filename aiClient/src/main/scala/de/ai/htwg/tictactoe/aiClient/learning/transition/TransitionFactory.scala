package de.ai.htwg.tictactoe.aiClient.learning.transition

import de.ai.htwg.tictactoe.aiClient.learning.action.Action
import de.ai.htwg.tictactoe.aiClient.learning.state.State

trait TransitionFactory[A <: Action, S <: State] {
  def apply(state: S, action: A, reward: Double): Transition[A, S]
}
