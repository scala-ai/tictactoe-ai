package de.ai.htwg.tictactoe.aiClient.learning.transition

import de.ai.htwg.tictactoe.aiClient.learning.action.TicTacToeAction
import de.ai.htwg.tictactoe.aiClient.learning.state.TicTacToeState

case class TicTacToeTransitionHistory(
    transitions: List[Transition[TicTacToeAction, TicTacToeState]]
) extends TransitionHistory[TicTacToeAction, TicTacToeState] {

  override def addTransition(transition: Transition[TicTacToeAction, TicTacToeState]): TicTacToeTransitionHistory =
    copy(transitions = transition :: transitions)

  override def reverseTransitions(): List[Transition[TicTacToeAction, TicTacToeState]] =
    transitions.reverse

  override def truncate(): TicTacToeTransitionHistory =
    copy(transitions = List())
}
