package de.ai.scala.aiClient.learning.transition

import de.ai.scala.aiClient.learning.action.TicTacToeAction
import de.ai.scala.aiClient.learning.state.TicTacToeState

case class TicTacToeTransitionHistory(
    transitions: List[Transition[TicTacToeAction, TicTacToeState]]
) extends TransitionHistory[TicTacToeAction, TicTacToeState] {
  override def addTransition(transition: Transition[TicTacToeAction, TicTacToeState]): TicTacToeTransitionHistory =
    copy(transitions = transition :: transitions)

  override def reverseTransitions(): List[Transition[TicTacToeAction, TicTacToeState]] = transitions.reverse
}
