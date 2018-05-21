package de.ai.htwg.tictactoe.clientConnection.gameController

import scala.collection.mutable

import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.clientConnection.model.Player
import grizzled.slf4j.Logging

object GameController {
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

trait GameController extends mutable.Publisher[GameController.Updates] with Logging {
  final override type Pub = GameController
  final override type Sub = mutable.Subscriber[GameController.Updates, Pub]

  def setPos(posX: Int, posY: Int, player: Player): GameController.Result =
    setPos(GridPosition(posX, posY), player)

  def setPos(pos: GridPosition, player: Player): GameController.Result

  def getGrid(): GameField

  def startingPlayer: Player

  def startGame(): Unit
}
