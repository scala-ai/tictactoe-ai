package de.ai.htwg.tictactoe.aiClient.learning.core.net

import org.nd4j.linalg.api.ndarray.INDArray

trait NeuralNet {
  def calc(input: INDArray): INDArray

  def train(input: INDArray, output: INDArray): Unit

  def serialize(path: String): Unit
}

object NeuralNet {

  trait Factory {
    def apply(configuration: NeuralNetConfiguration): NeuralNet

    def deserialize(string: String): NeuralNet

    def serialize(path: String, neuralNet: NeuralNet): Unit = neuralNet.serialize(path)
  }
}
