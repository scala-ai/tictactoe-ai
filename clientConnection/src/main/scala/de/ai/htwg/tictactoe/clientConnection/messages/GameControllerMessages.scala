package de.ai.htwg.tictactoe.clientConnection.messages

import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.model.GameField

object GameControllerMessages {
  case object RegisterCross
  case object RegisterCircle
  case object Unregister

  case class SetPos(pos: GridPosition)
  case class PosAlreadySet(pos: GridPosition)
  case class NotYourTurn(pos: GridPosition)
  case class GameWon(winner: Player, gf: GameField)
  case class PositionSet(gf: GameField)
}
