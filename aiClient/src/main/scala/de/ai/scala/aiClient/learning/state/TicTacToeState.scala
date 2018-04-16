package de.ai.scala.aiClient.learning.state

import de.ai.scala.aiClient.model.Playground
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j

case class TicTacToeState(
    playground: Playground
) extends State {
  override def getStateAsVector: INDArray = {
    val values = playground.values.toArray.map(_.asDoubleVal())
    val shape = Array(playground.dimensions, playground.dimensions)
    val order = 'c'
    Nd4j.create(values, shape, order)
  }
}
