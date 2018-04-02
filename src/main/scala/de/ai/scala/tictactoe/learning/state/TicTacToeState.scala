package de.ai.scala.tictactoe.learning.state

import de.ai.scala.tictactoe.model.TicTacToePlayground
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j

case class TicTacToeState(
    playground: TicTacToePlayground
) extends State {
  override def getStateAsVector: INDArray =
    Nd4j.create(playground.values.toArray, Array(playground.size._1, playground.size._2), 'c')
}
