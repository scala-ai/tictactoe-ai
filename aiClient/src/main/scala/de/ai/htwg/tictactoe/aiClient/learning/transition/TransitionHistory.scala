package de.ai.htwg.tictactoe.aiClient.learning.transition

import de.ai.htwg.tictactoe.aiClient.learning.action.Action
import de.ai.htwg.tictactoe.aiClient.learning.state.State

trait TransitionHistory[A <: Action, S <: State] {
  def addTransition(transition: Transition[A, S]): TransitionHistory[A, S]

  def reverseTransitions(): List[Transition[A, S]]
}
