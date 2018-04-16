package de.ai.scala.aiClient.learning.action

import org.nd4j.linalg.api.ndarray.INDArray

trait Action {
  def getStateAsVector: INDArray
}
