package de.ai.htwg.tictactoe.aiClient.learning.core.action

import org.nd4j.linalg.api.ndarray.INDArray

trait Action {
  def asVector: INDArray
}
