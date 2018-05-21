package de.ai.htwg.tictactoe.gameLogic.controller

import de.ai.htwg.tictactoe.clientConnection.gameController.GameController
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.model.strategy.TTTWinStrategy
import de.ai.htwg.tictactoe.clientConnection.model.strategy.TTTWinStrategyBuilder
import grizzled.slf4j.Logging

object GameControllerImpl {
  def apply(strategyBuilder: TTTWinStrategyBuilder, startingPlayer: Player): GameController =
    new GameControllerImpl(strategyBuilder, startingPlayer)
}

class GameControllerImpl(
    val strategyBuilder: TTTWinStrategyBuilder,
    override val startingPlayer: Player,
) extends GameController with Logging {
  private val thread = Thread.currentThread()
  debug("starting Game")
  private val res = GameController.Result
  val dimensions: Int = strategyBuilder.dimensions
  val strategy: Map[GridPosition, List[TTTWinStrategy]] = strategyBuilder.allWinStrategyCheckerPerPos

  @volatile private var gameField = GameField(startingPlayer, strategyBuilder)

  private def isNotCurrentThread: Boolean = Thread.currentThread() != thread

  override def setPos(posX: Int, posY: Int, player: Player): GameController.Result = setPos(GridPosition(posX, posY), player)

  override def setPos(pos: GridPosition, player: Player): GameController.Result = {
    if (isNotCurrentThread) throw new IllegalStateException("wrong thread")

    checkPosition(pos)
    val result = setPosInGrid(gameField, pos, player)
    gameField = result.field
    debug(s"new game field: \n${gameField.print()}")
    trace(s"player: $player trying to set pos: $pos: $result")
    result match {
      case update: GameController.Updates => publish(update)
      case _ => warn(s"player: $player failed $result")
    }
    result
  }

  private def checkPosition(pos: GridPosition): Unit = {
    def checkCoord(c: Int): Boolean = c >= 0 && c < dimensions
    if (!checkCoord(pos.x) && checkCoord(pos.y)) throw new IllegalArgumentException(s"Coordinates needs to be between 0 and $dimensions in $pos")
  }

  private def setPosInGrid(gameField: GameField, pos: GridPosition, player: Player): GameController.Result = {
    gameField.gameState match {
      case GameField.Finished(winner) => res.GameFinished(gameField, winner) // error already finished
      case GameField.Running(p) if p != player => res.NotThisPlayersTurn(gameField, p) // error wrong player
      case GameField.Running(_) if gameField.gameField.contains(pos) => res.PositionAlreadySelected(gameField, pos) // error already set
      case GameField.Running(current) =>
        val updatedGameField = gameField.setPos(pos)
        checkGameFinished(updatedGameField, current, pos) match {
          case None => res.GameUpdated(updatedGameField) // game Still running
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

  override def getGrid(): GameField = this.synchronized(gameField)

  override def startGame(): Unit = {
    publish(GameController.Result.GameUpdated(gameField))
  }
}
