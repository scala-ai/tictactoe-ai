package de.ai.htwg.tictactoe.clientConnection.gameController

import scala.collection.mutable

import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.clientConnection.model.Player
import grizzled.slf4j.Logging

object GameFieldController {
  sealed trait Result {
    val field: GameField
  }
  sealed trait Updates extends Result
  object Result {
    case class GameUpdated(field: GameField) extends Updates
    case class GameFinished(field: GameField, winner: Option[Player]) extends Updates
    case class NotThisPlayersTurn(field: GameField, wrongPlayer: Player) extends Result
    case class PositionAlreadySelected(field: GameField, pos: GridPosition) extends Result
  }
}

trait GameFieldController extends mutable.Publisher[GameFieldController.Updates] with Logging {
  final override type Pub = GameFieldController
  final override type Sub = mutable.Subscriber[GameFieldController.Updates, Pub]

  override def subscribe(sub: Sub, filter: Filter): Unit

  def setPos(posX: Int, posY: Int, player: Player): GameFieldController.Result =
    setPos(GridPosition(posX, posY), player)

  def setPos(pos: GridPosition, player: Player): GameFieldController.Result

  def getGrid(): GameField

  def startingPlayer: Player
}
