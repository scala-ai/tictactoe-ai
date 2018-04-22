package de.ai.htwg.tictactoe.aiClient.learning.action

import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j

case class TicTacToeAction(
    coordinate: GridPosition,
    dimensions: Int
) extends Action {
  override def getStateAsVector: INDArray =
    Nd4j.zeros(dimensions, dimensions)
      .putScalar(Array(coordinate.x, coordinate.y), 1)
}