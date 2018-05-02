package de.ai.htwg.tictactoe.clientConnection.messages

import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.Player

object GameControllerMessages {

  // sending messages
  case object RegisterObserver
  case object RegisterCross
  case object RegisterCircle
  case object Unregister
  case class SetPos(pos: GridPosition)

  // all observer messages
  case class GameFinished(gf: GameField, winner: Option[Player])
  case class GameUpdated(gf: GameField)

  // player observer messages
  case class NotYourTurn(pos: GridPosition)
  case class PosAlreadySet(pos: GridPosition)
  case class YourTurn(gf: GameField)
  case class YourResult(gf: GameField, result: GameResult)


  sealed trait GameResult
  case object GameWon extends GameResult
  case object GameLost extends GameResult
  case object GameDraw extends GameResult
}
