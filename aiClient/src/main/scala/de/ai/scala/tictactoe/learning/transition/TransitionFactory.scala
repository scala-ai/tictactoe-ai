package de.ai.scala.tictactoe.learning.transition

import de.ai.scala.tictactoe.learning.action.Action
import de.ai.scala.tictactoe.learning.state.State

trait TransitionFactory[A <: Action, S <: State] {
  def apply(state: S, action: A, reward: Double): Transition[A, S]
}
