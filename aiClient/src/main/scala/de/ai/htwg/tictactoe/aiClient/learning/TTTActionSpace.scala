package de.ai.htwg.tictactoe.aiClient.learning

import de.ai.htwg.tictactoe.aiClient.learning.core.action.ActionSpace

case class TTTActionSpace() extends ActionSpace[TTTState, TTTAction] {
  override def getPossibleActions(state: TTTState): List[TTTAction] = {
    val allEmptyFields = state.field.getAllEmptyPos
    allEmptyFields.map(TTTAction(_, state.field.dimensions))
  }
}
