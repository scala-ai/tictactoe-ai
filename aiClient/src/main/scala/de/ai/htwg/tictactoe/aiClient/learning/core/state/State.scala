package de.ai.htwg.tictactoe.aiClient.learning.core.state

import org.nd4j.linalg.api.ndarray.INDArray

trait State {
  def asVector: INDArray
}
