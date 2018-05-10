package de.ai.htwg.tictactoe.aiClient.model

import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition

object GameFieldToVectorConverter {

  def convertToVector(gameField: GameField): Vector[Double] = {
    val currentPlayer = gameField.gameState.asRunning match {
      case None => throw new IllegalStateException("Trying to train an already finished game.")
      case Some(running) => running.current
    }

    val values = for {
      x <- 0 until gameField.dimensions
      y <- 0 until gameField.dimensions
    } yield {
      gameField.getPos(GridPosition(x, y))
    }
    values.toVector.map(specificDoubleValueOfPlayer(currentPlayer))
  }

  private def specificDoubleValueOfPlayer(currentPlayer: Player)(positionSet: Option[Player]): Double = positionSet match {
    case None => 0.0
    case Some(`currentPlayer`) => 1.0
    case _ /* opponent */ => -1.0
  }

}
