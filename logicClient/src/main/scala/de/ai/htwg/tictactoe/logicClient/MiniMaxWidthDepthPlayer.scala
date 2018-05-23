package de.ai.htwg.tictactoe.logicClient

import scala.collection.mutable.ListBuffer

import de.ai.htwg.tictactoe.clientConnection.gameController.GameControllerMultiPlayer
import de.ai.htwg.tictactoe.clientConnection.gameController.GameControllerPlayer
import de.ai.htwg.tictactoe.clientConnection.gameController.RatedGameControllerPlayer
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.model.strategy.TTTWinStrategyBuilder
import grizzled.slf4j.Logging

class MiniMaxWidthDepthPlayer[Cont <: GameControllerMultiPlayer with RatedGameControllerPlayer](
    override val currentPlayer: Player,
    strategy: TTTWinStrategyBuilder,
    cont: Cont,
    val maxDepth: Int,
) extends GameControllerPlayer with Logging {
  private type MinMax = List[Double] => Double

  private def checkFinished(field: GameField, pos: GridPosition, player: Player): Boolean = {
    strategy.allWinStrategyCheckerPerPos(pos).exists { strat =>
      strat.check(p => field.getPos(p).contains(player))
    }
  }

  private def getMaxWidth(options: Int): Int = options match {
    case x if x <= 8 => 8
    case _ => 5
  }


  def reduceListToMaxSize(maxWidth: Int, field: GameField): List[GridPosition] = {
    if (field.noPosMoves <= maxWidth) {
      field.getAllEmptyPos
    } else {
      cont.getXMoves(maxWidth, field)
    }
  }


  private def checkFinishedWithRating(field: GameField, pos: GridPosition, lastPlayer: Player): Option[Double] = {
    if (checkFinished(field, pos, lastPlayer)) {
      Some(if (lastPlayer == currentPlayer) 200.0 else -200.0)
    } else if (field.noPosMoves == 0) {
      Some(0.0)
    } else {
      None
    }
  }

  private def getMinMaxForMove(oldField: GameField, move: GridPosition, player: Player, maxWidth: Int, current: MinMax, other: MinMax): Double = {
    val field = oldField.setPos(move)
    checkFinishedWithRating(field, move, player) match {
      case Some(rating) => rating
      case None => getMinMax(field, Player.other(player), maxWidth, current, other, maxDepth - 1)
    }
  }

  private def getMinMax(field: GameField, player: Player, maxWidth: Int, current: MinMax, other: MinMax, depth: Int): Double = {
    val finished: ListBuffer[Double] = ListBuffer()
    var posMoves: List[GridPosition] = Nil
    var posFields: List[GameField] = Nil
    reduceListToMaxSize(maxWidth, field).foreach { move =>
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
      posFields.map { nextField => getMinMax(nextField, nextPlayer, maxWidth, other, current, depth - 1) }
    } else {
      cont.getScores(field, posMoves).map(_.score)
    }

    current(finished.prependToList(rated))
  }


  override def getMove(field: GameField): GridPosition = {
    val maxWidth = getMaxWidth(field.noPosMoves)
    def emptyToZero(minMax: MinMax): MinMax = list => if (list.isEmpty) 0.0 else minMax(list)
    if (maxWidth == 1) {
      return cont.getMove(field)
    }

    reduceListToMaxSize(maxWidth, field).map { pos =>
      pos -> getMinMaxForMove(field, pos, currentPlayer, maxWidth, emptyToZero(_.min), emptyToZero(_.max))
    }.maxBy(_._2)._1
  }

}
