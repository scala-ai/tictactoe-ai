package de.ai.htwg.tictactoe.aiClient.learning.transition

import de.ai.htwg.tictactoe.aiClient.learning.action.TicTacToeAction
import de.ai.htwg.tictactoe.aiClient.learning.state.TicTacToeState

case class TicTacToeTransition(
    observation: TicTacToeState,
    action: TicTacToeAction,
    reward: Double,
) extends Transition[TicTacToeAction, TicTacToeState]

object TicTacToeTransition extends TransitionFactory[TicTacToeAction, TicTacToeState] {
  override def apply(state: TicTacToeState, action: TicTacToeAction, reward: Double): TicTacToeTransition =
    TicTacToeTransition(state, action, reward)
}