package de.ai.htwg.tictactoe.aiClient.learning.action

import de.ai.htwg.tictactoe.aiClient.learning.state.TicTacToeState

case class TicTacToeActionSpace() extends ActionSpace[TicTacToeState, TicTacToeAction] {
  override def getPossibleActions(state: TicTacToeState): List[TicTacToeAction] = {
    val allEmptyFields = state.field.getAllEmptyPos()
    allEmptyFields.map(TicTacToeAction(_, state.field.dimensions))
  }
}
