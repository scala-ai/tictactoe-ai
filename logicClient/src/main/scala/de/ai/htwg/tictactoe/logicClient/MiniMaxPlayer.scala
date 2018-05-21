package de.ai.htwg.tictactoe.logicClient

import de.ai.htwg.tictactoe.clientConnection.gameController.GameControllerPlayer
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.model.strategy.TTTWinStrategyBuilder
import grizzled.slf4j.Logging

class MiniMaxPlayer(
    override val currentPlayer: Player,
    strategy: TTTWinStrategyBuilder,
) extends GameControllerPlayer with Logging {
  private type MinMax = List[Int] => Int
  private def getMinMax(oldField: GameField, pos: GridPosition, player: Player, current: MinMax, other: MinMax): Int = {
    val field = oldField.setPos(pos)

    if (checkFinished(field, pos, player)) {
      if (player == currentPlayer) 1 else -1
    } else {
      val ret = field.getAllEmptyPos.map { posMove =>
        getMinMax(field, posMove, Player.other(player), other, current)
      }
      current(ret)
    }
  }

  private def checkFinished(field: GameField, pos: GridPosition, player: Player): Boolean = {
    strategy.allWinStrategyCheckerPerPos(pos).exists { strat =>
      strat.check(p => field.getPos(p).contains(player))
    }
  }

  override def getMove(field: GameField): GridPosition = {
    def emptyToZero(minMax: MinMax): MinMax = list => if (list.isEmpty) 0 else minMax(list)
    field.getAllEmptyPos.map { pos =>
      pos -> getMinMax(field, pos, currentPlayer, emptyToZero(_.min), emptyToZero(_.max))
    }.maxBy(_._2)._1
  }
}
