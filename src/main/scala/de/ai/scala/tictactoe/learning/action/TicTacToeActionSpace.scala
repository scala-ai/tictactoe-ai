package de.ai.scala.tictactoe.learning.action

import de.ai.scala.tictactoe.learning.state.TicTacToeState
import de.ai.scala.tictactoe.model.EmptyField

case class TicTacToeActionSpace() extends ActionSpace[TicTacToeState, TicTacToeAction] {
  override def getPossibleActions(state: TicTacToeState): Set[TicTacToeAction] = {
    val allFields = state.playground.mapToCoordinate
    allFields.collect({
      case (coordinate, EmptyField()) => coordinate
    }).map(TicTacToeAction)
  }
}
