package de.ai.scala.tictactoe.learning.state

import de.ai.scala.tictactoe.model.Playground
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j

case class TicTacToeState(
    playground: Playground
) extends State {
  override def getStateAsVector: INDArray = {
    val values = playground.values.toArray.map(_.asDoubleVal())
    val shape = Array(playground.size._1, playground.size._2)
    val order = 'c'
    Nd4j.create(values, shape, order)
  }
}
