package de.ai.htwg.tictactoe.aiClient.model

import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition

object GameFieldToVectorConverter {

  def convertToVector(gameField: GameField): Vector[Double] = {
    val values = for {
      x <- 0 until gameField.dimensions
      y <- 0 until gameField.dimensions
    } yield {
      gameField.getPos(GridPosition(x, y))
    }
    values.toVector.map(specificDoubleValueOfPlayer)
  }

  private def specificDoubleValueOfPlayer(p: Option[Player]): Double = p match {
    case None => 0.0
    case Some(Player.Cross) => 1.0
    case Some(Player.Circle) => -1.0
  }

}
