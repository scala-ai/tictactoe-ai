package de.ai.htwg.tictactoe.gameLogic.controller

import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.concurrent.duration.Duration

import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.model.strategy.TTTWinStrategy
import de.ai.htwg.tictactoe.clientConnection.model.strategy.TTTWinStrategyBuilder
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

class GameFieldController(
    val strategyBuilder: TTTWinStrategyBuilder,
    val startingPlayer: Player,
) extends mutable.Publisher[GameFieldController.Updates] with Logging {
  private val (thread, threadFactory) = {
    val p = concurrent.Promise[Runnable]()
    val t = new Thread(() => {
      Await.result(p.future, Duration(100, "ms")).run()
    })

    val f: ThreadFactory = r => {
      p.success(r)
      t
    }
    (t, f)
  }

  private val executionContext = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor(threadFactory))
  debug("starting Game")
  final override type Pub = GameFieldController
  final override type Sub = mutable.Subscriber[GameFieldController.Updates, Pub]
  private val res = GameFieldController.Result
  val dimensions: Int = strategyBuilder.dimensions
  val strategy: Map[GridPosition, List[TTTWinStrategy]] = strategyBuilder.allWinStrategyCheckerPerPos

  @volatile private var gameField = GameField(startingPlayer, dimensions)

  private def isNotCurrentThread: Boolean = Thread.currentThread() != thread
  private def execute(func: => Unit): Unit = executionContext.execute(() => func)

  override def subscribe(sub: Sub, filter: Filter): Unit = {
    if (isNotCurrentThread) {
      execute(subscribe(sub, filter))
      return
    }

    val msg = GameFieldController.Result.GameUpdated(gameField)
    super.subscribe(sub, filter)
    if (filter(msg)) sub.notify(this, GameFieldController.Result.GameUpdated(gameField))
  }

  def setPosThreadSave(pos: GridPosition, player: Player): Future[GameFieldController.Result] = {
    val p = Promise[GameFieldController.Result]()
    execute {
      p.success(setPos(pos, player))
    }
    p.future
  }

  def setPos(posX: Int, posY: Int, player: Player): GameFieldController.Result = setPos(GridPosition(posX, posY), player)
  def setPos(pos: GridPosition, player: Player): GameFieldController.Result = {
    if (isNotCurrentThread) throw new IllegalStateException("wrong thread")

    val result = setPosInGrid(gameField, pos, player)
    gameField = result.field
    trace(s"player: $player trying to set pos: $pos: $result")
    result match {
      case update: GameFieldController.Updates => publish(update)
      case _ => warn(s"player: $player failed $result")
    }
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

  def getGrid(): GameField = this.synchronized(gameField)
}
