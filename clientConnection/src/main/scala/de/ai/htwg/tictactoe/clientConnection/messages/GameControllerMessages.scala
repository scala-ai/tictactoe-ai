package de.ai.htwg.tictactoe.clientConnection.messages

import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.clientConnection.model.GameField

object GameControllerMessages {
  case object RegisterCross
  case object RegisterCircle
  case object Unregister

  case class SetPos(pos: GridPosition)
  case class PosAlreadySet(pos: GridPosition)
  case class NotYourTurn(pos: GridPosition)
  case class GameFinished(result: GameResult, gf: GameField)
  case class PositionSet(gf: GameField)


  sealed trait GameResult
  case object GameWon extends GameResult
  case object GameLost extends GameResult
  case object GameDraw extends GameResult
}
