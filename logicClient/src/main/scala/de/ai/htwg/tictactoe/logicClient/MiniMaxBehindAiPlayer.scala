package de.ai.htwg.tictactoe.logicClient

import de.ai.htwg.tictactoe.clientConnection.gameController.GameControllerMultiPlayer
import de.ai.htwg.tictactoe.clientConnection.gameController.GameControllerPlayer
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.model.strategy.TTTWinStrategyBuilder
import grizzled.slf4j.Logging

class MiniMaxBehindAiPlayer(
    override val currentPlayer: Player,
    strategy: TTTWinStrategyBuilder,
    cont: GameControllerMultiPlayer,
) extends GameControllerPlayer with Logging {
  private type MinMax = List[Int] => Int
  private def getMinMax(maxWidth: Int, oldField: GameField, pos: GridPosition, player: Player, current: MinMax, other: MinMax): Int = {
    val field = oldField.setPos(pos)

    if (checkFinished(field, pos, player)) {
      if (player == currentPlayer) 1 else -1
    } else {
      val ret = reduceListToMaxSize(maxWidth, field).map { posMove =>
        getMinMax(maxWidth, field, posMove, Player.other(player), other, current)
      }
      current(ret)
    }
  }

  private def checkFinished(field: GameField, pos: GridPosition, player: Player): Boolean = {
    strategy.allWinStrategyCheckerPerPos(pos).exists { strat =>
      strat.check(p => field.getPos(p).contains(player))
    }
  }

  private def getMaxWidth(options: Int): Int = options match {
    case x if x <= 8 => 8
    case 9 => 5
    case 10 => 4
    case 11 => 3
    case 12 => 3
    case _ => 1
  }

  override def getMove(field: GameField): GridPosition = {
    val start = System.currentTimeMillis()
    val move = getMoveInternally(field)
    info(s"calculating move took: ${System.currentTimeMillis() - start} ms")
    move
  }


  private def getMoveInternally(field: GameField): GridPosition = {
    def emptyToZero(minMax: MinMax): MinMax = list => if (list.isEmpty) 0 else minMax(list)
    val maxWidth = getMaxWidth(field.noPosMoves)
    if (maxWidth == 1) {
      cont.getMove(field)
    } else {
      reduceListToMaxSize(maxWidth, field).map { pos =>
        pos -> getMinMax(maxWidth, field, pos, currentPlayer, emptyToZero(_.min), emptyToZero(_.max))
      }.maxBy(_._2)._1
    }
  }


  def reduceListToMaxSize(maxSize: Int, field: GameField): List[GridPosition] = {
    if (field.noPosMoves <= maxSize) {
      field.getAllEmptyPos
    } else {
      cont.getXMoves(maxSize, field)
    }
  }
}
