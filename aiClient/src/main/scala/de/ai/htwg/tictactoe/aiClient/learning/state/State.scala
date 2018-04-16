package de.ai.htwg.tictactoe.aiClient.learning.state

import org.nd4j.linalg.api.ndarray.INDArray

trait State {
  def getStateAsVector: INDArray
}
