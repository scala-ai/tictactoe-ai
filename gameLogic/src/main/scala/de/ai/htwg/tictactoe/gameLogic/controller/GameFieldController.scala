package de.ai.htwg.tictactoe.gameLogic.controller

import scala.collection.mutable

import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.clientConnection.model.Player

object GameFieldController {
  sealed trait Result {
    val field: GameField
  }
  object Result {
    case class GameUpdated(field: GameField) extends Result
    case class GameFinished(field: GameField, winner: Option[Player]) extends Result
    case class NotThisPlayersTurn(field: GameField, wrongPlayer: Player) extends Result
    case class PositionAlreadySelected(field: GameField, pos: GridPosition) extends Result
  }
}

class GameFieldController(
    val strategyBuilder: TTTWinStrategyBuilder
) extends mutable.Publisher[GameFieldController.Result] {
  override type Pub = this.type
  private val res = GameFieldController.Result
  val dimensions = 4
  val strategy: Map[GridPosition, List[TTTWinStrategy]] = strategyBuilder.allWinStrategyCheckerPerPos

  private var gameField = GameField(Player.Cross, dimensions)

  def setPos(pos: GridPosition, player: Player): GameFieldController.Result = {
    val result = setPosInGrid(gameField, pos, player)
    gameField = result.field
    super.publish(result)
    result
  }

  private def setPosInGrid(gameField: GameField, pos: GridPosition, player: Player): GameFieldController.Result = {
    gameField.gameState match {
      case GameField.Finished(winner) => res.GameFinished(gameField, winner) // error already finished
      case GameField.Running(p) if p != player => res.NotThisPlayersTurn(gameField, p) // error wrong player
      case GameField.Running(_) if gameField.gameField.contains(pos) => res.PositionAlreadySelected(gameField, pos) // error already set
      case GameField.Running(current) =>
        val updatedGameField = gameField.setPos(pos)
        checkGameFinished(updatedGameField, current, pos) match {
          case None => res.GameUpdated(updatedGameField) // no winner
          case Some(optWinner) => res.GameFinished(updatedGameField.finishGame(optWinner), optWinner)
        }
    }
  }

  // only the last person playing can possible win.
  private def checkGameFinished(gf: GameField, lastPlayer: Player, lastPos: GridPosition): Option[Option[Player]] = {
    def checkPos(pos: GridPosition) = gf.gameField.get(pos).contains(lastPlayer)
    if (strategy.getOrElse(lastPos, Nil).exists(_.check(checkPos))) {
      // last Player is winner
      Some(Some(lastPlayer))
    } else if (gf.isCompletelyFilled()) {
      // draw
      Some(None)
    } else {
      // still running
      None
    }
  }

  def getGrid(): GameField = gameField

}
