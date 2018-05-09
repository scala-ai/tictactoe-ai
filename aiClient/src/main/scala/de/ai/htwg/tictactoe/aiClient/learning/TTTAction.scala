package de.ai.htwg.tictactoe.aiClient.learning

import de.ai.htwg.tictactoe.aiClient.learning.core.action.Action
import de.ai.htwg.tictactoe.clientConnection.model.GridPositionOLD
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j

case class TTTAction(
    coordinate: GridPositionOLD,
    dimensions: Int
) extends Action {
  override def asVector: INDArray =
    Nd4j.zeros(dimensions, dimensions)
      .putScalar(Array(coordinate.x, coordinate.y), 1)

  override def toString: String = "[" + coordinate.x + "," + coordinate.y + "] in " + dimensions + "x" + dimensions
}