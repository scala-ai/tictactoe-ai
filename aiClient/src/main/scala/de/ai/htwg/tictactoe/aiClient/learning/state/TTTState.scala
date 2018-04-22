package de.ai.htwg.tictactoe.aiClient.learning.state

import de.ai.htwg.tictactoe.aiClient.model.GameFieldToVectorConverter
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j

case class TTTState(
    field: GameField
) extends State {
  override def asVector: INDArray = {
    val values = GameFieldToVectorConverter.convertToVector(field)
    val shape = Array(field.dimensions, field.dimensions)
    val order = 'c'
    Nd4j.create(values.toArray, shape, order)
  }
}
