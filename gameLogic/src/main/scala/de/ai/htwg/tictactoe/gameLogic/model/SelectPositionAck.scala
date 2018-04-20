package de.ai.htwg.tictactoe.gameLogic.model

import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.gameLogic.model.SelectPositionAck.SelectPositionReturnCode

case class SelectPositionAck(p: Player, pos: GridPosition, state: GameField, returnCode: SelectPositionReturnCode)

object SelectPositionAck {
  sealed trait SelectPositionReturnCode
  object SelectPositionReturnCode {
    case object PositionSet extends SelectPositionReturnCode
    case object GameWon extends SelectPositionReturnCode
    case object GameAlreadyFinished extends SelectPositionReturnCode
    case object PositionAlreadySelected extends SelectPositionReturnCode
    case object NotThisPlayersTurn extends SelectPositionReturnCode
  }
}
