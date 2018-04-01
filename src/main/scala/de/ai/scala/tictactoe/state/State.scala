package de.ai.scala.tictactoe.state

import org.nd4j.linalg.api.ndarray.INDArray

trait State {
  def getStateAsVector: INDArray
}
