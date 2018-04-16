package de.ai.scala.tictactoe.learning.action

import de.ai.scala.tictactoe.learning.state.TicTacToeState
import de.ai.scala.tictactoe.model.Field

case class TicTacToeActionSpace() extends ActionSpace[TicTacToeState, TicTacToeAction] {
  override def getPossibleActions(state: TicTacToeState): List[TicTacToeAction] = {
    val allFields = state.playground.mapToCoordinate
    allFields.collect({
      case (coordinate, Field.Empty) => coordinate
    }).map(TicTacToeAction)
  }
}
