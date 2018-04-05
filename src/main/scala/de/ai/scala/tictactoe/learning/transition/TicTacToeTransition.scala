package de.ai.scala.tictactoe.learning.transition

import de.ai.scala.tictactoe.learning.action.TicTacToeAction
import de.ai.scala.tictactoe.learning.state.TicTacToeState

case class TicTacToeTransition(
    observation: TicTacToeState,
    action: TicTacToeAction,
    newObservation: TicTacToeState
) extends Transition[TicTacToeAction, TicTacToeState]
