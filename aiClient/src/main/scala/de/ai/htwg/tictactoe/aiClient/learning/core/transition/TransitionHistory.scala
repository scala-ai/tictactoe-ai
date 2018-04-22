package de.ai.htwg.tictactoe.aiClient.learning.core.transition

import de.ai.htwg.tictactoe.aiClient.learning.core.action.Action
import de.ai.htwg.tictactoe.aiClient.learning.core.state.State

trait TransitionHistory[A <: Action, S <: State] {
  def addTransition(transition: Transition[A, S]): TransitionHistory[A, S]

  def reverseTransitions(): List[Transition[A, S]]

  def truncate(): TransitionHistory[A, S]
}
