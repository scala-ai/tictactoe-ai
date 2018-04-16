package de.ai.htwg.tictactoe.aiClient.learning.action

import de.ai.htwg.tictactoe.aiClient.learning.state.TicTacToeState
import de.ai.htwg.tictactoe.aiClient.model.Field

case class TicTacToeActionSpace() extends ActionSpace[TicTacToeState, TicTacToeAction] {
  override def getPossibleActions(state: TicTacToeState): List[TicTacToeAction] = {
    val allFields = state.playground.mapToCoordinate
    allFields.collect({
      case (coordinate, Field.Empty) => coordinate
    }).map(TicTacToeAction(_, state.playground.dimensions))
  }
}
