package de.ai.htwg.tictactoe.aiClient.learning.transition

import de.ai.htwg.tictactoe.aiClient.learning.action.Action
import de.ai.htwg.tictactoe.aiClient.learning.state.State

case class TransitionHistoryImpl[A <: Action, S <: State](
    transitions: List[Transition[A, S]]
) extends TransitionHistory[A, S] {

  override def addTransition(transition: Transition[A, S]): TransitionHistoryImpl[A, S] =
    copy(transitions = transition :: transitions)

  // transitions are already reverse, cause of adding to head
  override def reverseTransitions(): List[Transition[A, S]] = transitions

  override def truncate(): TransitionHistoryImpl[A, S] = copy(transitions = List())
}

object TransitionHistoryImpl {
  def apply[A <: Action, S <: State](): TransitionHistory[A, S] = new TransitionHistoryImpl[A, S](List())
}
