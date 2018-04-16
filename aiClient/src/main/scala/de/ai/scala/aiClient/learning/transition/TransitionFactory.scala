package de.ai.scala.aiClient.learning.transition

import de.ai.scala.aiClient.learning.action.Action
import de.ai.scala.aiClient.learning.state.State

trait TransitionFactory[A <: Action, S <: State] {
  def apply(state: S, action: A, reward: Double): Transition[A, S]
}
