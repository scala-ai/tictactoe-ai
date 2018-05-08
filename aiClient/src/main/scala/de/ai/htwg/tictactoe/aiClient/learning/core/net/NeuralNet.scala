package de.ai.htwg.tictactoe.aiClient.learning.core.net

import org.nd4j.linalg.api.ndarray.INDArray

trait NeuralNet {
  def calc(input: INDArray): INDArray

  def train(input: INDArray, output: INDArray): Unit

  def serialize(): String
}

object NeuralNet {

  trait Factory {
    def apply(): NeuralNet

    def deserialize(string: String): NeuralNet

    def serialize(neuralNet: NeuralNet): String = neuralNet.serialize()
  }
}
