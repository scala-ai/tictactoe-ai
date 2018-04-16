package de.ai.scala.tictactoe.learning.action

import de.ai.scala.tictactoe.model.Coordinate
import org.nd4j.linalg.api.ndarray.INDArray

case class TicTacToeAction(
    coordinate: Coordinate
) extends Action {
  override def getStateAsVector: INDArray = ???
}