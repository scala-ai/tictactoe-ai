package de.ai.scala.tictactoe.learning.action

import org.nd4j.linalg.api.ndarray.INDArray

trait Action {
  def getStateAsVector: INDArray
}
