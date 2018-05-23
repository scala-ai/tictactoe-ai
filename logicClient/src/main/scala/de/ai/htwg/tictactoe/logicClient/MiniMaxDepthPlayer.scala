package de.ai.htwg.tictactoe.logicClient

import scala.collection.mutable.ListBuffer

import de.ai.htwg.tictactoe.clientConnection.gameController.GameControllerPlayer
import de.ai.htwg.tictactoe.clientConnection.gameController.RatedGameControllerPlayer
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.model.strategy.TTTWinStrategyBuilder
import grizzled.slf4j.Logging

class MiniMaxDepthPlayer(
    override val currentPlayer: Player,
    strategy: TTTWinStrategyBuilder,
    cont: RatedGameControllerPlayer,
    val maxDepth: Int,
) extends GameControllerPlayer with Logging {
  private type MinMax = List[Double] => Double

  private def checkFinishedWithRating(field: GameField, pos: GridPosition, lastPlayer: Player): Option[Double] = {
    if (checkFinished(field, pos, lastPlayer)) {
      Some(if (lastPlayer == currentPlayer) 200.0 else -200.0)
    } else if (field.noPosMoves == 0) {
      Some(0.0)
    } else {
      None
    }
  }

  private def getMinMaxForMove(oldField: GameField, move: GridPosition, player: Player, current: MinMax, other: MinMax): Double = {
    val field = oldField.setPos(move)
    checkFinishedWithRating(field, move, player) match {
      case Some(rating) => rating
      case None => getMinMax(field, Player.other(player), current, other, maxDepth - 1)
    }
  }

  private def getMinMax(field: GameField, player: Player, current: MinMax, other: MinMax, depth: Int): Double = {
    val finished: ListBuffer[Double] = ListBuffer()
    var posMoves: List[GridPosition] = Nil
    var posFields: List[GameField] = Nil
    field.getAllEmptyPos.foreach { move =>
      val nextField = field.setPos(move)
      checkFinishedWithRating(nextField, move, player) match {
        case Some(rating) => finished += rating
        case None =>
          posMoves = move :: posMoves
          posFields = nextField :: posFields
      }
    }
    val rated: List[Double] = if (depth > 0) {
      val nextPlayer = Player.other(player)
      posFields.map { nextField => getMinMax(nextField, nextPlayer, other, current, depth - 1) }
    } else {
      cont.getScores(field, posMoves).map(_.score)
    }

    current(finished.prependToList(rated))
  }

  private def checkFinished(field: GameField, pos: GridPosition, player: Player): Boolean = {
    strategy.allWinStrategyCheckerPerPos(pos).exists { strat =>
      strat.check(p => field.getPos(p).contains(player))
    }
  }

  override def getMove(field: GameField): GridPosition = {
    def emptyToZero(minMax: MinMax): MinMax = list => if (list.isEmpty) 0.0 else minMax(list)
    field.getAllEmptyPos.map { pos =>
      pos -> getMinMaxForMove(field, pos, currentPlayer, emptyToZero(_.min), emptyToZero(_.max))
    }.maxBy(_._2)._1
  }
}
