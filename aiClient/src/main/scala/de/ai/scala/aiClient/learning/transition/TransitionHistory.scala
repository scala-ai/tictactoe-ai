package de.ai.scala.aiClient.learning.transition

import de.ai.scala.aiClient.learning.action.Action
import de.ai.scala.aiClient.learning.state.State

trait TransitionHistory[A <: Action, S <: State] {
  def addTransition(transition: Transition[A, S]): TransitionHistory[A, S]

  def reverseTransitions(): List[Transition[A, S]]
}
